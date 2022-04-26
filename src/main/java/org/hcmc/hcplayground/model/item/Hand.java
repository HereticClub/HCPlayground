package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
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

public class Hand extends ItemBaseA {
    // 幸运值
    @Expose
    @SerializedName(value = PERSISTENT_LUCK_KEY)
    public float luck = 0.0f;
    // 药水效果
    @Expose
    @SerializedName(value = PERSISTENT_POTIONS_KEY)
    public PotionEffect[] potions;

    public Hand() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(this.getMaterial().value, 1);
        ItemMeta im = this.setBaseItemMeta(is);

        if (im != null) {
            /*
            获取已设置的lore
            */
            List<String> lore = im.getLore();
            if (lore == null) lore = new ArrayList<>();
            /*
            添加AttributeModifier
            */
            AttributeModifier amLuck = new AttributeModifier(UUID.randomUUID(), Global.PluginName(), this.luck, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            im.addAttributeModifier(Attribute.GENERIC_LUCK, amLuck);
            /*
            添加附魔效果
            */
            if (this.getGlowing()) {
                im.addEnchant(Enchantment.DURABILITY, 10, true);
                if (!Arrays.asList(this.getFlags()).contains(ItemFlag.HIDE_ENCHANTS))
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            /*
            如果副手物品有任何效果，移除隐藏属性标记
            */
            if (this.luck != 0) {
                im.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

            im.setLore(lore);
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
        NamespacedKey luckKey = new NamespacedKey(plugin, PERSISTENT_LUCK_KEY);

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();

        subContainer.set(luckKey, PersistentDataType.FLOAT, this.luck);
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);
    }
}
