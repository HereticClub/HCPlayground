package org.hcmc.hcplayground.items;

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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.deserializer.EquipmentSlotDeserializer;
import org.hcmc.hcplayground.deserializer.ItemFlagsDeserializer;
import org.hcmc.hcplayground.deserializer.MaterialDeserializer;
import org.hcmc.hcplayground.handler.PermissionHandler;
import org.hcmc.hcplayground.items.armor.Armor;
import org.hcmc.hcplayground.items.offhand.OffHand;
import org.hcmc.hcplayground.items.weapon.Weapon;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.HCPlayground;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ItemManager implements CommandExecutor {

    private static final JavaPlugin plugin;
    private static List<Weapon> ItemWeapons;
    private static List<Armor> ItemArmors;
    private static List<OffHand> ItemOffHand;
    private static ItemManager instance = null;


    static {
        ItemWeapons = new ArrayList<>();
        ItemArmors = new ArrayList<>();
        ItemOffHand = new ArrayList<>();
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
    }

    public ItemManager() {
        instance = this;
    }

    public ItemManager getInstance() {
        return instance;
    }

    public static void Load(YamlConfiguration yaml) {
        /*
         在items.yml文档中
         weapons 节段内所有item为武器
         armors 节段内所有item为盔甲
         accessories 节段内所有item为饰品
        */

        ConfigurationSection section;

        section = yaml.getConfigurationSection("weapons");
        if (section != null) ItemWeapons = SetItemList(section, Weapon.class);
        section = yaml.getConfigurationSection("armors");
        if (section != null) ItemArmors = SetItemList(section, Armor.class);
        section = yaml.getConfigurationSection("offHands");
        if (section != null) ItemOffHand = SetItemList(section, OffHand.class);
    }

    public ItemStack ToItemStack(Weapon weapon) {
        ItemStack is;
        float actualAttackDamage, actualAttackSpeed, actualCrit;

        if (weapon == null) return null;
        is = new ItemStack(weapon.material, 1);
        ItemMeta im = is.getItemMeta();
        if (im != null) {

            List<String> lores = new ArrayList<>(Arrays.stream(weapon.lore).toList());
            lores.add("");
            /*
             玩家原始攻击伤害: 1.0
             玩家原始攻击速度: 4.0
            */
            actualAttackDamage = weapon.attackDamage - 1;
            actualAttackSpeed = weapon.attackSpeed - 4;
            actualCrit = weapon.crit * 100;
            if (weapon.attackDamage != 0)
                lores.add(String.format("%s 攻击伤害", setColorString(weapon.attackDamage, true, false)));
            if (weapon.attackSpeed != 0)
                lores.add(String.format("%s 攻击速度", setColorString(weapon.attackSpeed, true, false)));
            if (weapon.crit != 0) lores.add(String.format("%s 暴击", setColorString(actualCrit, true, true)));
            /*
             Generic.ATTACH_REACH 为实验性内容，当前版本暂不支持
             if (weapon.attackReach != 0) lores.add(String.format("%s 攻击距离", setColorString(weapon.attackReach, true)));
            */

            im.setDisplayName(weapon.name);
            im.setLore(lores);
            im.setUnbreakable(weapon.unbreakable);
            im.addItemFlags(weapon.flags);

            AttributeModifier amAttackDamage = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier amAttackSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, amAttackDamage);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, amAttackSpeed);
            /*
             GENERIC_ATTACK_REACH 为实验性内容，当前版本暂不支持
             AttributeModifier amAttackReach = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackSpeed, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND);
             im.addAttributeModifier(Attribute.GENERIC_ATTACK_REACH, amAttackReach);
            */
            is.setItemMeta(im);
        }

        return is;
    }

    public ItemStack ToItemStack(Armor armor) {
        ItemStack is;
        float actualKnockbackResistance;
        float actualMovementSpeed;
        if (armor == null) return null;

        is = new ItemStack(armor.material, 1);
        ItemMeta im = is.getItemMeta();
        if (im != null) {
            List<String> lores = new ArrayList<>(Arrays.stream(armor.lore).toList());
            lores.add("");

            actualKnockbackResistance = armor.knockbackResistance * 100;
            actualMovementSpeed = armor.movementSpeed * 100;
            if (armor.armor != 0) lores.add(String.format("%s 盔甲", setColorString(armor.armor, false, false)));
            if (armor.armorToughness != 0)
                lores.add(String.format("%s 盔甲韧性", setColorString(armor.armorToughness, false, false)));
            if (armor.knockbackResistance != 0)
                lores.add(String.format("%s 击退抗性", setColorString(actualKnockbackResistance, false, true)));
            if (armor.movementSpeed != 0)
                lores.add(String.format("%s 速度", setColorString(actualMovementSpeed, false, true)));
            AttributeModifier amArmor = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.armor, AttributeModifier.Operation.ADD_NUMBER, armor.equipmentSlot);
            AttributeModifier amArmorToughness = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.armorToughness, AttributeModifier.Operation.ADD_NUMBER, armor.equipmentSlot);
            AttributeModifier amKnockbackResistance = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.knockbackResistance, AttributeModifier.Operation.ADD_SCALAR, armor.equipmentSlot);
            AttributeModifier amMovementSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.movementSpeed, AttributeModifier.Operation.ADD_SCALAR, armor.equipmentSlot);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR, amArmor);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, amArmorToughness);
            im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, amKnockbackResistance);
            im.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, amMovementSpeed);

            im.setDisplayName(armor.name);
            im.setLore(lores);
            im.setUnbreakable(armor.unbreakable);
            im.addItemFlags(armor.flags);
            is.setItemMeta(im);
        }

        return is;
    }

    public ItemStack ToItemStack(OffHand offHand) {
        ItemStack is;
        if (offHand == null) return null;

        is = new ItemStack(offHand.material, 1);
        ItemMeta im = is.getItemMeta();
        if (im != null) {
            List<String> lores = new ArrayList<>(Arrays.stream(offHand.lore).toList());
            lores.add("");

            if (offHand.luck != 0) lores.add(String.format("%s 幸运", setColorString(offHand.luck, false, false)));
            AttributeModifier amLuck = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), offHand.luck, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            im.addAttributeModifier(Attribute.GENERIC_LUCK, amLuck);

            if (offHand.glowing) {
                im.addEnchant(Enchantment.DURABILITY, 10, true);
            }

            im.setDisplayName(offHand.name);
            im.setLore(lores);
            im.setUnbreakable(offHand.unbreakable);
            im.addItemFlags(offHand.flags);
            is.setItemMeta(im);
        }

        return is;
    }

    private static <T> List<T> SetItemList(ConfigurationSection section, Class<T> tClass) {
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
            if (itemSection == null) continue;
            String value = gson.toJson(itemSection.getValues(false)).replace('&', '§');
            T item = gson.fromJson(value, tClass);
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
        for (Weapon iw : ItemWeapons) {
            inv.addItem(ToItemStack(iw));
        }
        for (Armor a : ItemArmors) {
            inv.addItem(ToItemStack(a));
        }
        for (OffHand o : ItemOffHand) {
            inv.addItem(ToItemStack(o));
        }

        p.openInventory(inv);
    }

    private String setColorString(float value, boolean isWeapon, boolean isPercentage) {
        String result;
        String colorCode;
        String sign;
        String percentage;

        colorCode = isWeapon ? "§2" : "§9";
        sign = isWeapon ? "" : "+";
        percentage = isPercentage ? "%" : "";
        result = String.format("%s%s%.1f%s", colorCode, sign, value, percentage);

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

