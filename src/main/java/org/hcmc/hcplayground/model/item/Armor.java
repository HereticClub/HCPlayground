package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.hcmc.hcplayground.utility.Global;

import java.util.*;

public class Armor extends CraftItemBase {

    @Expose
    @SerializedName(value = PERSISTENT_ARMOR_KEY)
    private float armor = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_HEALTH_KEY)
    private float health = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_RECOVER_KEY)
    private float recover = 0.0F;
    @Expose
    @SerializedName(value = PERSISTENT_ARMOR_TOUGHNESS_KEY)
    private float armorToughness = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_KNOCKBACK_RESISTANCE_KEY)
    private float knockBackResistance = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_MOVEMENT_SPEED_KEY)
    private float movementSpeed = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_EQUIPMENT_SLOT_KEY)
    private EquipmentSlot equipmentSlot;

    public Armor() {

    }

    @Override
    public void updateAttributeLore() {
        /*
         添加装备属性说明
         */
        attributeLore.clear();
        switch (this.equipmentSlot) {
            case CHEST -> attributeLore.add("§7穿在身上时:");
            case HEAD -> attributeLore.add("§7戴在头上时:");
            case LEGS -> attributeLore.add("§7穿在腿上时:");
            case FEET -> attributeLore.add("§7穿在脚上时:");
        }

        if (this.health != 0) attributeLore.add(String.format("%s 生命", this.setWeaponLore(this.health, false, false)));
        if (this.armor != 0) attributeLore.add(String.format("%s 护甲值", this.setWeaponLore(this.armor, false, false)));
        if (this.armorToughness != 0)
            attributeLore.add(String.format("%s 盔甲韧性", this.setWeaponLore(this.armorToughness, false, false)));
        if (this.knockBackResistance != 0)
            attributeLore.add(String.format("%s 击退抗性", this.setWeaponLore(this.knockBackResistance, false, true)));
        if (this.movementSpeed != 0)
            attributeLore.add(String.format("%s 速度", this.setWeaponLore(this.movementSpeed, false, true)));
        if (this.recover != 0)
            attributeLore.add(String.format("%s 恢复生命", this.setWeaponLore(this.recover, false, false)));
    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(material.value, amount);
        ItemMeta im = this.setBaseItemMeta(is);
        // 为了复原该死的护甲条效果
        double originalArmor = getOriginalArmor(material.value);
        double originalArmorToughness = getOriginalArmorToughness(material.value);

        if (im != null) {
            /*
            添加AttributeModifier
            */
            AttributeModifier amArmor = new AttributeModifier(UUID.randomUUID(), Global.getPluginName(), originalArmor, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amArmorToughness = new AttributeModifier(UUID.randomUUID(), Global.getPluginName(), originalArmorToughness, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amHealth = new AttributeModifier(UUID.randomUUID(), Global.getPluginName(), this.health, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amKnockBackResistance = new AttributeModifier(UUID.randomUUID(), Global.getPluginName(), this.knockBackResistance, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amMovementSpeed = new AttributeModifier(UUID.randomUUID(), Global.getPluginName(), this.movementSpeed, AttributeModifier.Operation.ADD_SCALAR, this.equipmentSlot);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR, amArmor);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, amArmorToughness);
            im.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, amHealth);
            im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, amKnockBackResistance);
            im.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, amMovementSpeed);
            //强制添加隐藏属性标记
            if (!flags.contains(ItemFlag.HIDE_ATTRIBUTES)) im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            // 设置永久性数据
            setPersistentData(im);
            is.setItemMeta(im);
        }
        return is;
    }

    private void setPersistentData(ItemMeta im) {
        // 设置NamespaceKey，比如暴击等MC没有的特性的命名空间名称
        NamespacedKey subKey = new NamespacedKey(plugin, PERSISTENT_SUB_KEY);
        NamespacedKey healthKey = new NamespacedKey(plugin, PERSISTENT_HEALTH_KEY);
        NamespacedKey recoverKey = new NamespacedKey(plugin, PERSISTENT_RECOVER_KEY);
        NamespacedKey armorKey = new NamespacedKey(plugin, PERSISTENT_ARMOR_KEY);
        NamespacedKey armorToughnessKey = new NamespacedKey(plugin, PERSISTENT_ARMOR_TOUGHNESS_KEY);
        NamespacedKey knockBackResistanceKey = new NamespacedKey(plugin, PERSISTENT_KNOCKBACK_RESISTANCE_KEY);
        NamespacedKey movementSpeedKey = new NamespacedKey(plugin, PERSISTENT_MOVEMENT_SPEED_KEY);
        // 获取当前物品的主要PersistentDataContainer实例，包含当前物品的自定义Id
        // 主要PersistentDataContainer实例是一个PersistentDataContainer容器
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        // 可以多次创建当前物品的次要PersistentDataContainer实例
        // 每个次要PersistentDataContainer实例都可以包含一个额外信息
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();
        // 在次要PersistentDataContainer实例里面设置任何想要的额外信息，比如暴击，生命值，攻击距离等等
        subContainer.set(healthKey, PersistentDataType.FLOAT, this.health);
        subContainer.set(recoverKey, PersistentDataType.FLOAT, this.recover);
        subContainer.set(armorKey, PersistentDataType.FLOAT, this.armor);
        subContainer.set(armorToughnessKey, PersistentDataType.FLOAT, this.armorToughness);
        subContainer.set(knockBackResistanceKey, PersistentDataType.FLOAT, knockBackResistance);
        subContainer.set(movementSpeedKey, PersistentDataType.FLOAT, movementSpeed);
        // 最后将次要PersistentDataContainer实例包含在主要PersistentDataContainer实例里面
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);
    }

    /**
     * 还原玩家护甲条效果，ItemStack没有任何获取护甲值的方法
     * @param material 物品的材质
     * @return 护甲值
     */
    private double getOriginalArmor(Material material){
        return switch (material) {
            case LEATHER_HELMET, LEATHER_BOOTS, CHAINMAIL_BOOTS, GOLDEN_BOOTS -> 1;
            case LEATHER_CHESTPLATE, GOLDEN_LEGGINGS, DIAMOND_HELMET, DIAMOND_BOOTS, NETHERITE_HELMET, NETHERITE_BOOTS -> 3;
            case LEATHER_LEGGINGS, CHAINMAIL_HELMET, IRON_HELMET, IRON_BOOTS, GOLDEN_HELMET -> 2;
            case CHAINMAIL_CHESTPLATE, IRON_LEGGINGS, GOLDEN_CHESTPLATE -> 5;
            case CHAINMAIL_LEGGINGS -> 4;
            case IRON_CHESTPLATE, DIAMOND_LEGGINGS, NETHERITE_LEGGINGS -> 6;
            case DIAMOND_CHESTPLATE, NETHERITE_CHESTPLATE -> 8;
            default -> 0;
        };
    }

    /**
     * 还原玩家盔甲韧性效果，ItemStack没有任何获取盔甲韧性值的方法
     * @param material 物品的材质
     * @return 盔甲韧性值
     */
    private double getOriginalArmorToughness(Material material) {
        return switch (material) {
            case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> 2;
            case NETHERITE_HELMET, NETHERITE_CHESTPLATE, NETHERITE_LEGGINGS, NETHERITE_BOOTS -> 3;
            default -> 0;
        };
    }
}
