package org.hcmc.hcplayground.utility;

import org.apache.commons.lang.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.ItemBase;

public class NameBinaryTag {
    private PersistentDataContainer subContainer = null;
    private JavaPlugin plugin = null;

    public NameBinaryTag(ItemStack itemStack) {
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return;

        plugin = HCPlayground.getInstance();
        PersistentDataContainer mainContainer = im.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, ItemBase.PERSISTENT_SUB_KEY);
        subContainer = mainContainer.get(key, PersistentDataType.TAG_CONTAINER);
    }

    public float getFloatValue(String key) {
        if (subContainer == null) return 0.0F;
        NamespacedKey subKey = new NamespacedKey(plugin, key);
        Float value = subContainer.get(subKey, PersistentDataType.FLOAT);

        return value == null ? 0.0F : value;
    }

    public String getStringValue(String key) {
        if (subContainer == null) return "";
        NamespacedKey subKey = new NamespacedKey(plugin, key);
        String value = subContainer.get(subKey, PersistentDataType.STRING);

        return StringUtils.isBlank(value) ? "" : value;
    }
}
