package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.hcmc.hcplayground.enums.CrazyBlockType;

import java.util.Arrays;

public class Crazy extends ItemBaseA {

    @Expose
    @SerializedName(value = "type")
    private CrazyBlockType type;

    @Expose
    @SerializedName(value = "block")
    private String block;

    public Crazy() {

    }

    public CrazyBlockType getType() {
        return type;
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(getMaterial().value, getAmount());
        // 判断物品只是简单的物品，即没有额外的Meta类型的数据
        // 设置基本的ItemMeta
        ItemMeta im = this.setBaseItemMeta(is);
        // 添加附魔效果
        if (this.getGlowing()) {
            im.addEnchant(Enchantment.MENDING, 1, true);
            if (!Arrays.asList(this.getFlags()).contains(ItemFlag.HIDE_ENCHANTS))
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        SetPersistentData(im);
        is.setItemMeta(im);
        return is;
    }

    private void SetPersistentData(ItemMeta im) {
        /*
        设置NamespaceKey，比如暴击等MC没有的特性的命名空间名称
        */
        NamespacedKey subKey = new NamespacedKey(plugin, PERSISTENT_SUB_KEY);
        NamespacedKey crazyKey = new NamespacedKey(plugin, PERSISTENT_CRAZY_TYPE);

        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();

        subContainer.set(crazyKey, PersistentDataType.STRING, this.type.name());
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);
    }
}
