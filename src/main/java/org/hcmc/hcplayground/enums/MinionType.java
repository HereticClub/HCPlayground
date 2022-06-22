package org.hcmc.hcplayground.enums;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.MinionManager;

import java.util.List;

/**
 * 自动采集伙伴类型
 */
public enum MinionType {
    /**
     * 圆石
     */
    COBBLESTONE("COBBLESTONE"),
    /**
     * 橡木
     */
    OAK_LOG("OAK_LOG"),


    ;

    private final String name;
    private int level;
    private final Plugin plugin;

    MinionType(String name) {
        this.name = name;
        plugin = HCPlayground.getInstance();
    }

    public ItemStack getMinion(int level, int amount) {
        this.level = level;

        ItemStack is = new ItemStack(Material.PLAYER_HEAD, amount);
        SkullMeta meta = setBaseMeta(is);

        is.setItemMeta(meta);
        return is;
    }

    private SkullMeta setBaseMeta(ItemStack item) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return null;

        NamespacedKey mainKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_MAIN_KEY);
        NamespacedKey subKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_SUB_KEY);
        NamespacedKey levelKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_LEVEL_KEY);
        PersistentDataContainer mainContainer = meta.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, name);

        subContainer.set(levelKey, PersistentDataType.INTEGER, level);
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);

        String displayKey = String.format("minion_type.%s.%s.display", name, level);
        String loreKey = String.format("minion_type.%s.%s.lore", name, level);
        String display = LanguageManager.getString(displayKey);
        List<String> lore = LanguageManager.getStringList(loreKey);

        meta.setDisplayName(display);
        meta.setLore(lore);

        return meta;
    }
}
