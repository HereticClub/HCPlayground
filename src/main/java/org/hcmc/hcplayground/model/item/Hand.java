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
import org.hcmc.hcplayground.utility.Global;

import java.util.UUID;

public class Hand extends CraftItemBase {
    // 幸运值
    @Expose
    @SerializedName(value = PERSISTENT_LUCK_KEY)
    private float luck = 0.0f;

    public Hand() {

    }

    @Override
    public void updateAttributeLore() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(material.value, amount);
        ItemMeta im = this.setBaseItemMeta(is);

        if (im != null) {
            /*
            添加AttributeModifier
            */
            AttributeModifier amLuck = new AttributeModifier(UUID.randomUUID(), Global.getPluginName(), this.luck, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND);
            im.addAttributeModifier(Attribute.GENERIC_LUCK, amLuck);
            /*
            添加附魔效果
            */
            if (this.isGlowing()) {
                im.addEnchant(Enchantment.DURABILITY, 10, true);
                if (!flags.contains(ItemFlag.HIDE_ENCHANTS))
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            /*
            如果副手物品有任何效果，移除隐藏属性标记
            */
            if (this.luck != 0) {
                im.removeItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            }

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
