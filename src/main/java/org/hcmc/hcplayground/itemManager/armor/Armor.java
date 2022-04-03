package org.hcmc.hcplayground.itemManager.armor;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.itemManager.ItemBase;
import org.hcmc.hcplayground.model.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Armor extends ItemBase {

    @Expose
    @SerializedName(value = "armor")
    public float armor = 0.0f;
    @Expose
    @SerializedName(value = "armorToughness")
    public float armorToughness = 0.0f;
    @Expose
    @SerializedName(value = "knockbackResistance")
    public float knockbackResistance = 0.0f;
    @Expose
    @SerializedName(value = "movementSpeed")
    public float movementSpeed = 0.0f;
    @Expose
    @SerializedName(value = "equipmentSlot")
    public EquipmentSlot equipmentSlot;
    /**
     * 附加在装备上的药水效果
     */
    @Expose
    @SerializedName(value = "potions")
    public PotionEffect[] potions;

    public Armor() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(this.getMaterial(), 1);
        ItemMeta im = SetBaseItemMeta(is);

        if (im != null) {
            List<String> lores = im.getLore();
            if (lores == null) lores = new ArrayList<>();
            lores.add("");
            /*
            添加装备属性说明
            */
            switch (this.equipmentSlot) {
                case CHEST -> lores.add("§7穿在身上时:");
                case HAND -> lores.add("§7戴在头上时:");
                case LEGS -> lores.add("§7穿在腿上时:");
                case FEET -> lores.add("§7穿在脚上时:");
            }
            float actualKnockbackResistance = this.knockbackResistance * 100;
            float actualMovementSpeed = this.movementSpeed * 100;
            if (this.armor != 0) lores.add(String.format("%s 盔甲", setColorString(this.armor, false, false)));
            if (this.armorToughness != 0)
                lores.add(String.format("%s 盔甲韧性", setColorString(this.armorToughness, false, false)));
            if (this.knockbackResistance != 0)
                lores.add(String.format("%s 击退抗性", setColorString(actualKnockbackResistance, false, true)));
            if (this.movementSpeed != 0)
                lores.add(String.format("%s 速度", setColorString(actualMovementSpeed, false, true)));
            /*
            添加AttributeModifier
            */
            AttributeModifier amArmor = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.armor, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amArmorToughness = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.armorToughness, AttributeModifier.Operation.ADD_NUMBER, this.equipmentSlot);
            AttributeModifier amKnockbackResistance = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.knockbackResistance, AttributeModifier.Operation.ADD_SCALAR, this.equipmentSlot);
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

            im.setLore(lores);
            is.setItemMeta(im);
        }

        return is;
    }
}
