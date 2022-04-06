package org.hcmc.hcplayground.model;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Weapon extends ItemBaseA {
    /**
     * 攻击伤害
     */
    @Expose
    @SerializedName(value = "attackDamage")
    public float attackDamage = 0.0F;
    /**
     * 攻击距离，实验性内容，该版本暂不支持，保留属性
     */
    @Expose
    @SerializedName(value = "attackReach")
    public float attackReach = 0;
    /**
     * 攻击速度
     */
    @Expose
    @SerializedName(value = "attackSpeed")
    public float attackSpeed = 0.0F;
    /**
     * 暴击
     */
    @Expose
    @SerializedName(value = "crit")
    public float crit = 0.0F;
    /**
     * 附加在武器上的药水效果
     */
    @Expose
    @SerializedName(value = "potions")
    public PotionEffect[] potions;

    public Weapon() {

    }

    public ItemStack toItemStack() {
        /*
        将Item Model转换为ItemStack对象，并且为ItemStack添加的新命名空间和新的物品ID
        */
        ItemStack is = new ItemStack(this.getMaterial(), 1);
        ItemMeta im = SetBaseItemMeta(is);

        if (im != null) {
            /*
            为物品添加额外特性信息，比如暴击等MC本身没有的特性
            */
            SetPersistentData(im);
            /*
            获取已设置的lore
            */
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
            float actualCrit = this.crit * 100;
            lore.add("§7在主手时:");
            if (this.attackDamage != 0)
                lore.add(String.format("%s 攻击伤害", setColorString(this.attackDamage, true, false)));
            if (this.attackSpeed != 0)
                lore.add(String.format("%s 攻击速度", setColorString(this.attackSpeed, true, false)));
            if (this.crit != 0) lore.add(String.format("%s 暴击", setColorString(actualCrit, true, true)));
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
            if (!Arrays.asList(this.getFlags()).contains(ItemFlag.HIDE_ATTRIBUTES)) {
                im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lore);
            is.setItemMeta(im);
        }

        return is;
    }

    private void SetPersistentData(ItemMeta im) {
        /*
        设置NamespaceKey，比如暴击等MC没有的特性的命名空间名称
        */
        NamespacedKey subKey = new NamespacedKey(plugin, Global.PERSISTENT_SUB_KEY);
        NamespacedKey critKey = new NamespacedKey(plugin, Global.PERSISTENT_CRIT_KEY);

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();

        subContainer.set(critKey, PersistentDataType.FLOAT, this.crit);
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);
    }
}
