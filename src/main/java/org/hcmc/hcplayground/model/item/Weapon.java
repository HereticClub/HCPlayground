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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Weapon extends ItemBaseA {
    /**
     * 攻击伤害
     */
    @Expose
    @SerializedName(value = PERSISTENT_ATTACK_DAMAGE_KEY)
    public float attackDamage = 0.0F;
    /**
     * 攻击距离，实验性内容，该版本暂不支持，保留属性
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_ATTACK_REACH_KEY)
    public float attackReach = 0;
    /**
     * 攻击速度
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_ATTACK_SPEED_KEY)
    public float attackSpeed = 0.0F;
    /**
     * 暴击
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_CRITICAL_KEY)
    public float critical = 0.0F;
    /**
     * 爆伤
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_CRITICAL_DAMAGE_KEY)
    public float criticalDamage = 1.50F;
    /**
     * 吸血
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_BLOOD_SUCKING_KEY)
    public float bloodSucking = 0.0F;
    /**
     * 生命值
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_HEALTH_KEY)
    public float health = 0.0F;
    /**
     * 附加在武器上的药水效果
     */
    @Expose
    @SerializedName(value = ItemBase.PERSISTENT_POTIONS_KEY)
    public PotionEffect[] potions;

    public Weapon() {

    }

    public ItemStack toItemStack() {
        /*
        将Item Model转换为ItemStack对象，并且为ItemStack添加的新命名空间和新的物品ID
        */
        ItemStack is = new ItemStack(this.getMaterial().value, 1);
        ItemMeta im = this.setBaseItemMeta(is);

        if (im != null) {
            // 获取已设置的lore
            List<String> lore = im.getLore();
            if (lore == null) lore = new ArrayList<>();
            lore.add("");
            /*
            玩家原始攻击伤害: 1.0
            玩家原始攻击速度: 4.0
            actualAttackDamage - 武器的实际的攻击伤害，需要减去玩家默认攻击伤害值1
            actualAttackSpeed - 武器的实际攻击速度，需要减去玩家默认攻击伤害值4
            actualCrit - 武器暴击率，MC本身没有的特性，但需要按百分比显示
            */

            float actualAttackDamage = this.attackDamage - 1;
            float actualAttackSpeed = this.attackSpeed - 4;
            this.attackReach = 3 * (1 + this.attackReach);

            lore.add("§7在主手时:");
            if (this.attackDamage != 0)
                lore.add(String.format("%s 攻击伤害", setLoreString(this.attackDamage, true, false)));
            if (this.attackSpeed != 0)
                lore.add(String.format("%s 攻击速度", setLoreString(this.attackSpeed, true, false)));
            if (this.attackReach != 0)
                lore.add(String.format("%s 攻击距离", setLoreString(this.attackReach, true, false)));
            if (this.critical != 0) lore.add(String.format("%s 暴击", setLoreString(this.critical, true, true)));
            if (this.criticalDamage != 0)
                lore.add(String.format("%s 爆伤", setLoreString(this.criticalDamage, true, true)));
            if (this.health != 0) lore.add(String.format("%s 生命", setLoreString(this.health, true, false)));
            if (this.bloodSucking != 0) lore.add(String.format("%s 吸血", setLoreString(this.bloodSucking, true, true)));
            /*
            添加AttributeModifier
            GENERIC_ATTACK_REACH 为实验性内容，当前版本暂不支持，临时注释以下代码
            if (weapon.attackReach != 0) lore.add(String.format("%s 攻击距离", setColorString(weapon.attackReach, true)));
            AttributeModifier amAttackReach = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackSpeed, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlot.HAND);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_REACH, amAttackReach);
            */
            AttributeModifier amHealth = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.health, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier amAttackDamage = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackDamage, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            AttributeModifier amAttackSpeed = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), actualAttackSpeed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
            im.addAttributeModifier(Attribute.GENERIC_MAX_HEALTH, amHealth);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, amAttackDamage);
            im.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, amAttackSpeed);
            /*
            强制添加隐藏属性标记
            */
            if (!Arrays.asList(this.getFlags()).contains(ItemFlag.HIDE_ATTRIBUTES)) {
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lore);
            // 为物品添加额外特性信息，比如暴击等MC本身没有的特性
            SetPersistentData(im);
            is.setItemMeta(im);
        }

        return is;
    }

    private void SetPersistentData(ItemMeta im) {
        /*
        设置NamespaceKey，比如暴击等MC没有的特性的命名空间名称
        */
        NamespacedKey subKey = new NamespacedKey(plugin, PERSISTENT_SUB_KEY);
        NamespacedKey healthKey = new NamespacedKey(plugin, PERSISTENT_HEALTH_KEY);
        NamespacedKey bloodSuckingKey = new NamespacedKey(plugin, PERSISTENT_BLOOD_SUCKING_KEY);
        NamespacedKey criticalKey = new NamespacedKey(plugin, PERSISTENT_CRITICAL_KEY);
        NamespacedKey criticalDamageKey = new NamespacedKey(plugin, PERSISTENT_CRITICAL_DAMAGE_KEY);
        NamespacedKey attackReachKey = new NamespacedKey(plugin, PERSISTENT_ATTACK_REACH_KEY);
        NamespacedKey attackDamageKey = new NamespacedKey(plugin, PERSISTENT_ATTACK_DAMAGE_KEY);
        NamespacedKey attackSpeedKey = new NamespacedKey(plugin, PERSISTENT_ATTACK_SPEED_KEY);

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();

        subContainer.set(healthKey, PersistentDataType.FLOAT, this.health);
        subContainer.set(bloodSuckingKey, PersistentDataType.FLOAT, this.bloodSucking);
        subContainer.set(criticalKey, PersistentDataType.FLOAT, this.critical);
        subContainer.set(criticalDamageKey, PersistentDataType.FLOAT, this.criticalDamage);
        subContainer.set(attackReachKey, PersistentDataType.FLOAT, this.attackReach);
        subContainer.set(attackDamageKey, PersistentDataType.FLOAT, this.attackDamage);
        subContainer.set(attackSpeedKey, PersistentDataType.FLOAT, this.attackSpeed);

        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);
    }
}
