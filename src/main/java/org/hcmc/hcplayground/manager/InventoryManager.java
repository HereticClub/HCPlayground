package org.hcmc.hcplayground.manager;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.inventory.InventoryDetail;
import org.hcmc.hcplayground.model.inventory.InventorySlot;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.MaterialData;

import java.lang.reflect.Field;
import java.util.*;

public class InventoryManager {

    public static List<InventoryDetail> Items = new ArrayList<>();
    public static Plugin plugin = HCPlayground.getPlugin();
    private static Player player;

    private final static String GAMEPROFILE_PROPERTY_NAME_TEXTURES = "textures";
    private final static String SKULLMETA_FIELD_NAME_PROFILE = "profile";

    public InventoryManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        Items = Global.SetItemList(yaml, InventoryDetail.class);
    }

    public static Inventory CreateInventory(String TemplateId, Player sender) {
        InventoryDetail item = Items.stream().filter(x -> x.id.equalsIgnoreCase(TemplateId)).findAny().orElse(null);
        if (item == null) return Bukkit.createInventory(null, InventoryType.CHEST, "Error Inventory");

        player = sender;

        return switch (item.type) {
            case ANVIL -> createAnvilInventory(item);
            case CHEST -> createChestInventory(item);
            default -> Bukkit.createInventory(item, item.type, item.title);
        };
    }

    private static Inventory createAnvilInventory(InventoryDetail item) {
        return Bukkit.createInventory(item, item.type, item.title);
    }

    private static Inventory createChestInventory(InventoryDetail item) {
        Inventory inv = Bukkit.createInventory(item, item.size, item.title);
        for (InventorySlot d : item.decorates) {
            ItemStack is = d.material.value.equals(Material.PLAYER_HEAD) && d.customSkull.isEmpty() ? ResolvePlayerHead(d.material) : ResolveCustomHead(d.customSkull);
            ItemMeta im = is.getItemMeta();
            if (im == null) continue;

            im.setLore(d.lore);
            im.setDisplayName(d.text);
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
