package org.hcmc.hcplayground.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import org.hcmc.hcplayground.model.menu.MenuSlot;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.MaterialData;

import java.lang.reflect.Field;
import java.util.*;

public class MenuManager {

    public static List<MenuDetail> Items = new ArrayList<>();
    public static Plugin plugin = HCPlayground.getPlugin();
    private static Player player;

    private final static String GAMEPROFILE_PROPERTY_NAME_TEXTURES = "textures";
    private final static String SKULLMETA_FIELD_NAME_PROFILE = "profile";

    public MenuManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        Items = Global.SetItemList(yaml, MenuDetail.class);
    }

    public static Inventory CreateMenu(String MenuId, Player sender) {
        player = sender;
        MenuDetail item = Items.stream().filter(x -> x.id.equalsIgnoreCase(MenuId)).findAny().orElse(null);
        if (item == null) return null;
        String value = Global.GsonObject.toJson(item, MenuDetail.class);
        value = PlaceholderAPI.setPlaceholders(player, value);
        MenuDetail detail = Global.GsonObject.fromJson(value, MenuDetail.class);

        String title = detail.title.replace("%player%", player.getName());
        String worldName = player.getWorld().getName();
        boolean isCurentWorld = detail.enableWorlds.stream().anyMatch(x -> x.equalsIgnoreCase(worldName));
        if (detail.enableWorlds.size() >= 1 && !isCurentWorld && !player.isOp()) {
            player.sendMessage(LocalizationManager.getMessage("menuWorldProhibited", player)
                    .replace("%world%", worldName)
                    .replace("%menu%", title)
            );
            return null;
        }

        return switch (detail.type) {
            case ANVIL -> createAnvilInventory(detail, title);
            case CHEST -> createChestInventory(detail, title);
            default -> Bukkit.createInventory(detail, detail.type, title);
        };
    }

    private static Inventory createAnvilInventory(MenuDetail item, String title) {
        return Bukkit.createInventory(item, item.type, title);
    }

    private static Inventory createChestInventory(MenuDetail item, String title) {
        Inventory inv = Bukkit.createInventory(item, item.size, title);

        for (MenuSlot d : item.decorates) {
            ItemStack is;

            if (!d.material.value.equals(Material.PLAYER_HEAD)) {
                is = new ItemStack(d.material.value, d.amount);
            } else {
                is = d.customSkull.isEmpty() ? ResolvePlayerHead(d.material) : ResolveCustomHead(d.customSkull);
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
            inv.setItem(d.number - 1, is);
        }

        return inv;
    }

    private static ItemStack ResolveCustomHead(String data) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm = (SkullMeta) is.getItemMeta();
        if (sm == null) return is;

        UUID uuid = UUID.randomUUID();
        GameProfile profile = new GameProfile(uuid, null);
        PropertyMap pm = profile.getProperties();

        // 猜想new Property(String, String)参数名称调换了
        Property pp = new Property(GAMEPROFILE_PROPERTY_NAME_TEXTURES, data);
        pm.put(GAMEPROFILE_PROPERTY_NAME_TEXTURES, pp);

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

    private static ItemStack ResolvePlayerHead(MaterialData data) {
        String[] keys = data.name.split("_");
        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        ItemStack is = new ItemStack(data.value);

        if (keys.length >= 2 && keys[0].equalsIgnoreCase("head")) {
            String playerName = keys[1].replace("%player%", player.getName());
            OfflinePlayer player = Arrays.stream(offlinePlayers).filter(x -> Objects.requireNonNull(x.getName()).equalsIgnoreCase(playerName)).findAny().orElse(null);

            SkullMeta sm = (SkullMeta) is.getItemMeta();
            if (sm != null) sm.setOwningPlayer(player);
            is.setItemMeta(sm);
        }

        return is;
    }
}
