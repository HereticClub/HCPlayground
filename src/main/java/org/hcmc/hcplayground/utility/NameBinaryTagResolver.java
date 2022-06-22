package org.hcmc.hcplayground.utility;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.ItemBase;

public class NameBinaryTagResolver {
    private PersistentDataContainer subContainer = null;
    private JavaPlugin plugin = null;

    public NameBinaryTagResolver(ItemStack itemStack) {
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

        if (value == null) return 0;
        return value;
    }
}
