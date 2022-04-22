package org.hcmc.hcplayground.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.*;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemManager {

    private static final JavaPlugin plugin;
    private static final List<ItemBase> ItemEntire;
    private static List<Weapon> itemWeapons;
    private static List<Armor> itemArmors;
    private static List<Hand> itemHands;
    private static List<Join> itemJoins;
    private static ItemManager instance = null;

    static {
        itemWeapons = new ArrayList<>();
        itemArmors = new ArrayList<>();
        itemHands = new ArrayList<>();
        itemJoins = new ArrayList<>();
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
            if (section != null) itemWeapons = Global.SetItemList(section, Weapon.class);
            section = yaml.getConfigurationSection("armors");
            if (section != null) itemArmors = Global.SetItemList(section, Armor.class);
            section = yaml.getConfigurationSection("hands");
            if (section != null) itemHands = Global.SetItemList(section, Hand.class);
            section = yaml.getConfigurationSection("joins");
            if (section != null) itemJoins = Global.SetItemList(section, Join.class);

            ItemEntire.clear();
            ItemEntire.addAll(itemArmors);
            ItemEntire.addAll(itemHands);
            ItemEntire.addAll(itemWeapons);
            ItemEntire.addAll(itemJoins);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static ItemBase FindItemById(String id) {
        return ItemEntire.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public static void Give(CommandSender sender, String playerName, String itemId, int amount) {
        Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        Player player = Arrays.stream(players).filter(x -> x.getName().equalsIgnoreCase(playerName)).findAny().orElse(null);

        if (player == null) {
            sender.sendMessage(LocalizationManager.getMessage("playerNotExist", null).replace("%player%", playerName));
            return;
        }
        // 可能这个判断没有用
        if (!player.isOnline()) {
            sender.sendMessage(LocalizationManager.getMessage("playerOffLine", player).replace("%player%", playerName));
            return;
        }

        ItemBase ib = FindItemById(itemId);
        if (ib == null) {
            sender.sendMessage(LocalizationManager.getMessage("noSuchItem", player).replace("%item%", itemId));
            return;
        }
        ItemStack is = ib.toItemStack();
        is.setAmount(amount);
        player.getInventory().setItem(player.getInventory().firstEmpty(), is);
    }
}

