package org.hcmc.hcplayground.items;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.deserializer.EquipmentSlotDeserializer;
import org.hcmc.hcplayground.deserializer.ItemFlagsDeserializer;
import org.hcmc.hcplayground.deserializer.MaterialDeserializer;
import org.hcmc.hcplayground.deserializer.PotionEffectDeserializer;
import org.hcmc.hcplayground.handler.PermissionHandler;
import org.hcmc.hcplayground.items.armor.Armor;
import org.hcmc.hcplayground.items.offhand.OffHand;
import org.hcmc.hcplayground.items.weapon.Weapon;
import org.hcmc.hcplayground.model.Global;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
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
        plugin = HCPlayground.getPlugin();
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

        try {
            section = yaml.getConfigurationSection("weapons");
            if (section != null) ItemWeapons = SetItemList(section, Weapon.class);
            section = yaml.getConfigurationSection("armors");
            if (section != null) ItemArmors = SetItemList(section, Armor.class);
            section = yaml.getConfigurationSection("offHands");
            if (section != null) ItemOffHand = SetItemList(section, OffHand.class);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }

    public ItemStack ToItemStack(Weapon weapon) {
        if (weapon == null) return null;

        /*
        将Item Model转换为ItemStack对象，并且为ItemStack添加的新命名空间和新的物品ID
        */
        ItemStack is = new ItemStack(weapon.material, 1);
        ItemMeta im = SetBaseItemMeta(is, weapon);

        if (im != null) {
            /*
            为物品添加额外特性信息，比如暴击等MC本身没有的特性
            */
            SetPersistentData(im, weapon);
            /*
            获取已设置的lores
            */
            List<String> lores = im.getLore();
            if (lores == null) lores = new ArrayList<>();
            lores.add("");
            /*
            玩家原始攻击伤害: 1.0
            玩家原始攻击速度: 4.0
            actualAttackDamage - 武器的实际的攻击伤害，需要减去玩家默认攻击伤害值1
            actualAttackSpeed - 武器的实际攻击速度，需要减去玩家默认攻击伤害值4
            actualCrit - 武器暴击率，MC本身没有的特性，但需要按百分比显示
            */
            float actualAttackDamage = weapon.attackDamage - 1;
            float actualAttackSpeed = weapon.attackSpeed - 4;
            float actualCrit = weapon.crit * 100;
            lores.add("§7在主手时:");
            if (weapon.attackDamage != 0)
                lores.add(String.format("%s 攻击伤害", setColorString(weapon.attackDamage, true, false)));
            if (weapon.attackSpeed != 0)
                lores.add(String.format("%s 攻击速度", setColorString(weapon.attackSpeed, true, false)));
            if (weapon.crit != 0) lores.add(String.format("%s 暴击", setColorString(actualCrit, true, true)));
            /*
            添加AttributeModifier
            GENERIC_ATTACK_REACH 为实验性内容，当前版本暂不支持，临时注释以下代码
            if (weapon.attackReach != 0) lores.add(String.format("%s 攻击距离", setColorString(weapon.attackReach, true)));
            AttributeModifier amAttackReach = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackSpeed, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_REACH, amAttackReach);
            */
            AttributeModifier amAttackDamage = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier amAttackSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, amAttackDamage);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, amAttackSpeed);
            /*
            强制添加隐藏属性标记
            */
            if (!Arrays.asList(weapon.flags).contains(ItemFlag.HIDE_ATTRIBUTES)) {
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lores);
            is.setItemMeta(im);
        }

        return is;
    }

    public ItemStack ToItemStack(Armor armor) {
        float actualKnockbackResistance;
        float actualMovementSpeed;
        if (armor == null) return null;

        ItemStack is = new ItemStack(armor.material, 1);
        ItemMeta im = SetBaseItemMeta(is, armor);
        if (im != null) {
            List<String> lores = im.getLore();
            if (lores == null) lores = new ArrayList<>();
            lores.add("");
            /*
            添加装备属性说明
            */
            switch (armor.equipmentSlot) {
                case CHEST -> lores.add("§7穿在身上时:");
                case HAND -> lores.add("§7戴在头上时:");
                case LEGS -> lores.add("§7穿在腿上时:");
                case FEET -> lores.add("§7穿在脚上时:");
            }
            actualKnockbackResistance = armor.knockbackResistance * 100;
            actualMovementSpeed = armor.movementSpeed * 100;
            if (armor.armor != 0) lores.add(String.format("%s 盔甲", setColorString(armor.armor, false, false)));
            if (armor.armorToughness != 0)
                lores.add(String.format("%s 盔甲韧性", setColorString(armor.armorToughness, false, false)));
            if (armor.knockbackResistance != 0)
                lores.add(String.format("%s 击退抗性", setColorString(actualKnockbackResistance, false, true)));
            if (armor.movementSpeed != 0)
                lores.add(String.format("%s 速度", setColorString(actualMovementSpeed, false, true)));
            /*
            添加AttributeModifier
            */
            AttributeModifier amArmor = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.armor, AttributeModifier.Operation.ADD_NUMBER, armor.equipmentSlot);
            AttributeModifier amArmorToughness = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.armorToughness, AttributeModifier.Operation.ADD_NUMBER, armor.equipmentSlot);
            AttributeModifier amKnockbackResistance = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.knockbackResistance, AttributeModifier.Operation.ADD_SCALAR, armor.equipmentSlot);
            AttributeModifier amMovementSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), armor.movementSpeed, AttributeModifier.Operation.ADD_SCALAR, armor.equipmentSlot);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR, amArmor);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, amArmorToughness);
            im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, amKnockbackResistance);
            im.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, amMovementSpeed);
            /*
            强制添加隐藏属性标记
            */
            if (!Arrays.asList(armor.flags).contains(ItemFlag.HIDE_ATTRIBUTES)) {
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lores);
            is.setItemMeta(im);
        }

        return is;
    }

    public ItemStack ToItemStack(OffHand offHand) {
        if (offHand == null) return null;

        ItemStack is = new ItemStack(offHand.material, 1);
        ItemMeta im = SetBaseItemMeta(is, offHand);

        if (im != null) {
            List<String> lores = im.getLore();
            if (lores == null) lores = new ArrayList<>();
            /*
            添加AttributeModifier
            */
            AttributeModifier amLuck = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), offHand.luck, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            im.addAttributeModifier(Attribute.GENERIC_LUCK, amLuck);
            /*
            添加附魔效果
            */
            if (offHand.glowing) {
                im.addEnchant(Enchantment.DURABILITY, 10, true);
                if (!Arrays.asList(offHand.flags).contains(ItemFlag.HIDE_ENCHANTS))
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            /*
            如果副手物品有任何效果，移除隐藏属性标记
            */
            if (offHand.luck != 0) {
                im.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lores);
            is.setItemMeta(im);
        }

        return is;
    }

    public static <T> Object FindItemById(String id, Class<T> cls) {
        if (cls.isAssignableFrom(Weapon.class)) {
            return ItemWeapons.stream().filter(x -> x.id.equalsIgnoreCase(id)).findAny().orElse(null);
        }
        if (cls.isAssignableFrom(Armor.class)) {
            return ItemArmors.stream().filter(x -> x.id.equalsIgnoreCase(id)).findAny().orElse(null);
        }
        if (cls.isAssignableFrom(OffHand.class)) {
            return ItemOffHand.stream().filter(x -> x.id.equalsIgnoreCase(id)).findAny().orElse(null);
        }

        return null;
    }

    private static <T> List<T> SetItemList(ConfigurationSection section, Class<T> tClass) throws NoSuchFieldException, IllegalAccessException {
        Set<String> keys = section.getKeys(false);
        List<T> list = new ArrayList<>();

        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .registerTypeAdapter(Material.class, new MaterialDeserializer())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsDeserializer())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectDeserializer())
                .create();

        for (String s : keys) {
            ConfigurationSection itemSection = section.getConfigurationSection(s);
            if (itemSection == null) continue;
            String value = gson.toJson(itemSection.getValues(false)).replace('&', '§');

            T item = gson.fromJson(value, tClass);
            Field field = tClass.getField("id");
            field.set(item, s);

            list.add(item);
        }

        return list;
    }

    private ItemMeta SetBaseItemMeta(ItemStack is, ItemBase ib) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return null;

        NamespacedKey mainKey = new NamespacedKey(plugin, Global.PERSISTENT_MAIN_KEY);
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, ib.id);

        List<String> lores = new ArrayList<>(Arrays.stream(ib.lore).toList());
        im.setDisplayName(ib.name);
        im.setLore(lores);
        im.setUnbreakable(ib.unbreakable);
        im.addItemFlags(ib.flags);
        is.setItemMeta(im);

        return im;
    }

    private void SetPersistentData(ItemMeta im, Weapon weapon) {
        /*
        设置NamespaceKey，比如暴击等MC没有的特性的命名空间名称
        */
        NamespacedKey subKey = new NamespacedKey(plugin, Global.PERSISTENT_SUB_KEY);
        NamespacedKey critKey = new NamespacedKey(plugin, Global.PERSISTENT_CRIT_KEY);

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();

        subContainer.set(critKey, PersistentDataType.FLOAT, weapon.crit);
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);
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

