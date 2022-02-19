package org.hcmc.hcplayground.itemManager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.hcmc.hcplayground.handler.PermissionHandler;
import org.hcmc.hcplayground.itemManager.armor.Armor;
import org.hcmc.hcplayground.itemManager.offhand.OffHand;
import org.hcmc.hcplayground.itemManager.weapon.Weapon;
import org.hcmc.hcplayground.model.Global;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemManager implements CommandExecutor {

    //private static final JavaPlugin plugin;
    private static final List<ItemBase> ItemEntire;
    private static List<Weapon> ItemWeapons;
    private static List<Armor> ItemArmors;
    private static List<OffHand> ItemOffHand;
    private static ItemManager instance = null;


    static {
        ItemWeapons = new ArrayList<>();
        ItemArmors = new ArrayList<>();
        ItemOffHand = new ArrayList<>();
        ItemEntire = new ArrayList<>();
        //plugin = HCPlayground.getPlugin();
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
            if (section != null) ItemOffHand = Global.SetItemList(section, OffHand.class);

            ItemEntire.clear();
            ItemEntire.addAll(ItemArmors);
            ItemEntire.addAll(ItemOffHand);
            ItemEntire.addAll(ItemWeapons);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public static ItemBase FindItemById(String id) {
        /*
        if (cls.isAssignableFrom(Weapon.class)) {
            return ItemWeapons.stream().filter(x -> x.id.equalsIgnoreCase(id)).findAny().orElse(null);
        }
        if (cls.isAssignableFrom(Armor.class)) {
            return ItemArmors.stream().filter(x -> x.id.equalsIgnoreCase(id)).findAny().orElse(null);
        }
        if (cls.isAssignableFrom(OffHand.class)) {
            return ItemOffHand.stream().filter(x -> x.id.equalsIgnoreCase(id)).findAny().orElse(null);
        }
         */
        return ItemEntire.stream().filter(x->x.id.equalsIgnoreCase(id)).findAny().orElse(null);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 0) return false;

        if (Commander.GUI.Validate(sender, args)) {
            ShowItemGui((Player) sender);
            return true;
        }

        return false;
    }

    private void ShowItemGui(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, "Item Manager");
        for (Weapon iw : ItemWeapons) {
            inv.addItem(iw.toItemStack());
        }
        for (Armor a : ItemArmors) {
            inv.addItem(a.toItemStack());
        }
        for (OffHand o : ItemOffHand) {
            inv.addItem(o.toItemStack());
        }

        p.openInventory(inv);
    }


    public enum Commander {
        //DEFAULT("", "quartermaster.use", false),
        GUI("gui", "quartermaster.gui", true);

        private final String command;
        private final String permission;
        private final boolean isPlayer;

        private Commander(String command, String permission, boolean isPlayer) {
            this.command = command;
            this.permission = permission;
            this.isPlayer = isPlayer;
        }

        public boolean Validate(CommandSender sender, String[] args) {

            if (isPlayer && sender instanceof ConsoleCommandSender) {
                Global.LogWarning("This command can't be run from console.");
                return false;
            }

            if (!PermissionHandler.HasPermission(sender, permission)) {
                sender.sendMessage("You have no permission to run this command");
                return false;
            }

            return command.equalsIgnoreCase(args[0]);
        }
    }
}

