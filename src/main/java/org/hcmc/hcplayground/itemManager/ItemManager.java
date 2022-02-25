package org.hcmc.hcplayground.itemManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.itemManager.armor.Armor;
import org.hcmc.hcplayground.itemManager.join.Join;
import org.hcmc.hcplayground.itemManager.offhand.OffHand;
import org.hcmc.hcplayground.itemManager.weapon.Weapon;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ItemManager {

    private static final JavaPlugin plugin;
    private static final List<ItemBase> ItemEntire;
    private static List<Weapon> ItemWeapons;
    private static List<Armor> ItemArmors;
    private static List<OffHand> ItemOffHands;
    private static List<Join> ItemJoins;
    private static ItemManager instance = null;


    static {
        ItemWeapons = new ArrayList<>();
        ItemArmors = new ArrayList<>();
        ItemOffHands = new ArrayList<>();
        ItemJoins = new ArrayList<>();
        ItemEntire = new ArrayList<>();
        plugin = HCPlayground.getPlugin();
    }

    public ItemManager() {
        instance = this;
    }

    public ItemManager getInstance() {
        return instance;
    }

    public static List<ItemBase> getItemEntire() {
        return ItemEntire;
    }

    public static void Load(YamlConfiguration yaml) {
        /*
         在items.yml文档中
         weapons 节段内所有item为武器
         armors 节段内所有item为盔甲
         accessories 节段内所有item为饰品
        */
        ConfigurationSection section;

        try {
            section = yaml.getConfigurationSection("weapons");
            if (section != null) ItemWeapons = Global.SetItemList(section, Weapon.class);
            section = yaml.getConfigurationSection("armors");
            if (section != null) ItemArmors = Global.SetItemList(section, Armor.class);
            section = yaml.getConfigurationSection("offHands");
            if (section != null) ItemOffHands = Global.SetItemList(section, OffHand.class);
            section = yaml.getConfigurationSection("joins");
            if (section != null) ItemJoins = Global.SetItemList(section, Join.class);

            ItemEntire.clear();
            ItemEntire.addAll(ItemArmors);
            ItemEntire.addAll(ItemOffHands);
            ItemEntire.addAll(ItemWeapons);
            ItemEntire.addAll(ItemJoins);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static ItemBase FindItemById(String id) {
        return ItemEntire.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public static void Give(CommandSender sender, String playerName, String itemId, int amount) {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        Player player = Arrays.stream(players).filter(x->x.getName().equalsIgnoreCase(playerName)).findAny().orElse(null);

        if (player == null) {
            sender.sendMessage(Localization.Messages.get("playerNotExist").replace("%player%", playerName));
            return;
        }
        // 可能这个判断没有用
        if (!player.isOnline()) {
            sender.sendMessage(Localization.Messages.get("playerOffLine").replace("%player%", playerName));
            return;
        }

        ItemBase ib = FindItemById(itemId);
        if (ib == null) {
            sender.sendMessage(Localization.Messages.get("noSuchItem").replace("%item%", itemId));
            return;
        }
        ItemStack is = ib.toItemStack();
        is.setAmount(amount);
        player.getInventory().setItem(player.getInventory().firstEmpty(), is);
    }
}

