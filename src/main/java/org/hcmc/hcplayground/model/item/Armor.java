package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.NameBinaryTagResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Armor extends ItemBaseA {

    @Expose
    @SerializedName(value = PERSISTENT_ARMOR_KEY)
    public float armor = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_HEALTH_KEY)
    public float health = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_RECOVER_KEY)
    public float recover = 0.0F;
    @Expose
    @SerializedName(value = PERSISTENT_ARMOR_TOUGHNESS_KEY)
    public float armorToughness = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_KNOCKBACK_RESISTANCE_KEY)
    public float knockBackResistance = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_MOVEMENT_SPEED_KEY)
    public float movementSpeed = 0.0f;
    @Expose
    @SerializedName(value = PERSISTENT_EQUIPMENT_SLOT_KEY)
    public EquipmentSlot equipmentSlot;
    /**
     * 附加在装备上的药水效果
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_POTIONS_KEY)
    public PotionEffect[] potions;

    public Armor() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(this.getMaterial().value, 1);
        ItemMeta im = this.setBaseItemMeta(is);

        if (im != null) {
            SetPersistentData(im);
            List<String> lore = im.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            /*
            添加装备属性说明
            */
            switch (this.equipmentSlot) {
                case CHEST -> lore.add("§7穿在身上时:");
                case HAND -> lore.add("§7戴在头上时:");
                case LEGS -> lore.add("§7穿在腿上时:");
                case FEET -> lore.add("§7穿在脚上时:");
            }
            /*
            float actualKnockbackResistance = this.knockBackResistance * 100;
            float actualMovementSpeed = this.movementSpeed * 100;

             */
            if (this.health != 0) lore.add(String.format("%s 生命", setLoreString(this.health, false, false)));
            if (this.recover != 0) lore.add(String.format("%s 恢复生命", setLoreString(this.recover, false, false)));
            if (this.armor != 0) lore.add(String.format("%s 盔甲", setLoreString(this.armor, false, false)));
            if (this.armorToughness != 0)
                lore.add(String.format("%s 盔甲韧性", setLoreString(this.armorToughness, false, false)));
            if (this.knockBackResistance != 0)
                lore.add(String.format("%s 击退抗性", setLoreString(this.knockBackResistance, false, true)));
            if (this.movementSpeed != 0)
                lore.add(String.format("%s 速度", setLoreString(this.movementSpeed, false, true)));
            /*
            添加AttributeModifier
            */
            AttributeModifier amArmor = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.armor, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amArmorToughness = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.armorToughness, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amKnockbackResistance = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.knockBackResistance, AttributeModifier.Operation.ADD_SCALAR, this.equipmentSlot);
            AttributeModifier amMovementSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.movementSpeed, AttributeModifier.Operation.ADD_SCALAR, this.equipmentSlot);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR, amArmor);
            im.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, amArmorToughness);
            im.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, amKnockbackResistance);
            im.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, amMovementSpeed);
            /*
            强制添加隐藏属性标记
            */
            if (!Arrays.asList(this.getFlags()).contains(ItemFlag.HIDE_ATTRIBUTES)) {
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lore);
            is.setItemMeta(im);
        }

        return is;
    }

    private void SetPersistentData(ItemMeta im) {
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
}
