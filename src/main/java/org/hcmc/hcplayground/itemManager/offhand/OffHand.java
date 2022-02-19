package org.hcmc.hcplayground.itemManager.offhand;

import com.google.gson.annotations.Expose;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
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

public class OffHand extends ItemBase {

    @Expose
    public float luck = 0.0f;
    @Expose
    public PotionEffect[] potions;

    public OffHand() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(this.material, 1);
        ItemMeta im = SetBaseItemMeta(is);

        if (im != null) {
            List<String> lores = im.getLore();
            if (lores == null) lores = new ArrayList<>();
            /*
            添加AttributeModifier
            */
            AttributeModifier amLuck = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.luck, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            im.addAttributeModifier(Attribute.GENERIC_LUCK, amLuck);
            /*
            添加附魔效果
            */
            if (this.glowing) {
                im.addEnchant(Enchantment.DURABILITY, 10, true);
                if (!Arrays.asList(this.flags).contains(ItemFlag.HIDE_ENCHANTS))
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            /*
            如果副手物品有任何效果，移除隐藏属性标记
            */
            if (this.luck != 0) {
                im.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lores);
            is.setItemMeta(im);
        }

        return is;
    }
}