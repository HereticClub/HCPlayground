package org.hcmc.hcplayground.Items;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.Deserializer.EquipmentSlotDeserializer;
import org.hcmc.hcplayground.Deserializer.ItemFlagsDeserializer;
import org.hcmc.hcplayground.Deserializer.MaterialDeserializer;
import org.hcmc.hcplayground.Handler.PermissionHandler;
import org.hcmc.hcplayground.Items.Weapon.ItemWeapon;
import org.hcmc.hcplayground.Model.Global;
import org.hcmc.hcplayground.HCPlayground;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.TypeDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ItemManager implements CommandExecutor {

    private static final JavaPlugin plugin;
    private static List<ItemWeapon> ItemWeapons;
    private static ItemManager instance;


    static {
        ItemWeapons = new ArrayList<>();
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
    }

    public ItemManager() {
        instance = this;
    }

    public static void Load(YamlConfiguration yaml) {
        /*
         在items.yml文档中
         weapons 节段内所有item为武器
         armors 节段内所有item为盔甲
         accessories 节段内所有item为饰品
        */
        Set<String> keys = yaml.getKeys(false);
        for (String s : keys) {
            System.out.println(s);
        }

        ConfigurationSection section = yaml.getConfigurationSection("weapons");
        ItemWeapons = SetItemList(section);
        /*
        Set<String> itemKeys = section.getKeys(false);

        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .registerTypeAdapter(Material.class, new MaterialDeserializer())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsDeserializer())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
                .create();

        ItemWeapons.clear();
        for (String s : itemKeys) {
            ConfigurationSection itemSection = section.getConfigurationSection(s);
            String Value = gson.toJson(itemSection.getValues(false)).replace('&', '§');
            ItemWeapon ie = gson.fromJson(Value, ItemWeapon.class);
            ie.id = s;
            ItemWeapons.add(ie);
        }
        */
    }

    public ItemStack ToItemStack(ItemWeapon weapon) {
        ItemStack is;
        String color, sign;
        if (weapon == null) return null;

        is = new ItemStack(weapon.material, 1);
        ItemMeta im = is.getItemMeta();
        if (im != null) {

            List<String> lores = new ArrayList<>(Arrays.stream(weapon.lore).toList());
            lores.add("");
            if (weapon.attackDamage != 0) lores.add(String.format("%s 攻击伤害", float2string(weapon.attackDamage)));
            if (weapon.attackSpeed != 0) lores.add(String.format("%s 攻击速度", float2string(weapon.attackSpeed)));
            if (weapon.crit != 0) lores.add(String.format("%s 暴击", float2string(weapon.crit)));

            im.setDisplayName(weapon.name);
            im.setLore(lores);
            im.setUnbreakable(weapon.unbreakable);
            im.addItemFlags(weapon.itemFlags);

            AttributeModifier amAttackDamage = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), weapon.attackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier amAttackSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), weapon.attackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, amAttackDamage);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, amAttackSpeed);

            is.setItemMeta(im);
        }

        return is;
    }

    private static <T> List<T> SetItemList(ConfigurationSection section) {

        Class<T> entityClass = (Class<T>) (ItemManager.class.getGenericSuperclass());
        Set<String> keys = section.getKeys(false);
        List<T> list = new ArrayList<>();
        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .registerTypeAdapter(Material.class, new MaterialDeserializer())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsDeserializer())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
                .create();

        for (String s : keys) {
            ConfigurationSection itemSection = section.getConfigurationSection(s);
            String value = gson.toJson(itemSection.getValues(false)).replace('&', '§');
            T item = gson.fromJson(value, entityClass);
            list.add(item);
        }

        return list;
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
        for (ItemWeapon iw : ItemWeapons) {
            inv.addItem(ToItemStack(iw));
        }
        p.openInventory(inv);
    }

    private String float2string(float value) {
        String result;
        String color, sign;

        color = value >= 0 ? "§9" : "§c";
        sign = value >= 0 ? "+" : "";
        result = String.format("%s%s%.1f", color, sign, value);

        return result;
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

