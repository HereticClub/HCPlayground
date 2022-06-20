package org.hcmc.hcplayground.manager;

import com.mojang.authlib.GameProfile;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.menu.MenuDetail;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.MaterialData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class MenuManager {

    public static List<MenuDetail> Items = new ArrayList<>();
    public static Plugin plugin = HCPlayground.getPlugin();

    private final static String SKULLMETA_FIELD_NAME_PROFILE = "profile";

    public MenuManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        Items = Global.SetItemList(yaml, MenuDetail.class);
        for (MenuDetail menu : Items) {
            String path = String.format("%s.decorates", menu.id);
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;

            menu.decorates = Global.SetItemList(section, MenuItem.class);
        }
    }

    public static Inventory CreateMenu(String MenuId, Player player) throws IOException, IllegalAccessException, InvalidConfigurationException {
        MenuDetail item = Items.stream().filter(x -> x.id.equalsIgnoreCase(MenuId)).findAny().orElse(null);
        MenuDetail detail = setPlaceholders(item, player);
        if (detail == null) return null;

        String title = detail.title.replace("%player%", player.getName());
        String worldName = player.getWorld().getName();
        boolean isEnabledWorld = detail.enableWorlds.stream().anyMatch(x -> x.equalsIgnoreCase(worldName));
        if (detail.enableWorlds.size() >= 1 && !isEnabledWorld && !player.isOp()) {
            player.sendMessage(LanguageManager.getMessage("menuWorldProhibited", player)
                    .replace("%world%", worldName)
                    .replace("%menu%", title)
            );
            return null;
        }

        return switch (detail.type) {
            case ANVIL -> createAnvilInventory(detail, title);
            case CHEST -> createChestInventory(player, detail, title);
            default -> Bukkit.createInventory(detail, detail.type, title);
        };
    }

    private static Inventory createAnvilInventory(MenuDetail item, String title) {
        return Bukkit.createInventory(item, item.type, title);
    }

    private static Inventory createChestInventory(Player player, MenuDetail item, String title) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Inventory inv = Bukkit.createInventory(item, item.size, title);

        for (MenuItem d : item.decorates) {
            for (int number : d.numbers) {
                ItemStack is;
                if (d.material == null) {
                    d.material = new MaterialData();
                    d.material.setData(Material.AIR, "");
                }
                if (!d.material.value.equals(Material.PLAYER_HEAD)) {
                    is = new ItemStack(d.material.value, d.amount);
                } else {
                    is = d.customSkull.isEmpty() ? ResolvePlayerHead(player, d.material) : ResolveCustomHead(player, d.customSkull);
                }

                ItemMeta im = is.getItemMeta();
                if (im == null) continue;

                im.addItemFlags(d.flags.toArray(new ItemFlag[0]));
                im.setLore(d.lore);
                im.setDisplayName(d.text.replace("%player%", player.getName()));

                if (d.glowing) {
                    im.addEnchant(Enchantment.MENDING, 1, true);
                    Set<ItemFlag> flags = im.getItemFlags();
                    if (!flags.contains(ItemFlag.HIDE_ENCHANTS)) {
                        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    }
                }

                is.setItemMeta(im);
                inv.setItem(number - 1, is);
            }
        }

        return inv;
    }

    private static ItemStack ResolveCustomHead(Player player, String base64data) throws IOException, IllegalAccessException, InvalidConfigurationException {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) is.getItemMeta();
        if (sm == null) return is;

        PlayerData data = PlayerManager.getPlayerData(player);
        GameProfile profile = data.setHeadTextures(base64data);

        try {
            Field field;
            field = sm.getClass().getDeclaredField(SKULLMETA_FIELD_NAME_PROFILE);
            field.setAccessible(true);
            field.set(sm, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        is.setItemMeta(sm);
        return is;
    }

    private static ItemStack ResolvePlayerHead(Player player, MaterialData data) {
        String[] keys = data.name.split("_");
        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        ItemStack is = new ItemStack(data.value);

        if (keys.length >= 2 && keys[0].equalsIgnoreCase("head")) {
            String playerName = keys[1].replace("%player%", player.getName());
            OfflinePlayer offlinePlayer = Arrays.stream(offlinePlayers).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(playerName)).findAny().orElse(null);

            SkullMeta sm = (SkullMeta) is.getItemMeta();
            if (sm != null) sm.setOwningPlayer(offlinePlayer);
            is.setItemMeta(sm);
        }

        return is;
    }

    private static MenuDetail setPlaceholders(MenuDetail menu, @NotNull Player player) throws InvalidConfigurationException, IllegalAccessException {
        if (menu == null) return null;
        Map<String, MenuDetail> map = new HashMap<>();
        map.put(menu.id, menu);

        String value = Global.GsonObject.toJson(map).replace('&', '§').replace("%player%", player.getName());
        value = PlaceholderAPI.setPlaceholders(player, value);

        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString(value);

        List<MenuDetail> details = Global.SetItemList(yaml, MenuDetail.class);
        for (MenuDetail md : details) {
            String path = String.format("%s.decorates", md.id);
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;
            md.decorates = Global.SetItemList(section, MenuItem.class);
        }

        if (details.size() >= 1) return details.get(0);
        return null;
    }
}
