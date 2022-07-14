package org.hcmc.hcplayground.enums;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.manager.MinionManager;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.utility.PlayerHeaderUtil;
import org.jetbrains.annotations.NotNull;

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
        ItemMeta meta = setBaseMeta(is);

        is.setItemMeta(meta);
        return is;
    }

    @Override
    public String toString() {
        return name;
    }

    private ItemMeta setBaseMeta(ItemStack item) {
        MinionTemplate template = MinionManager.getMinionTemplate(name, level);
        if (template == null) return item.getItemMeta();
        // set player head texture
        ItemMeta meta = PlayerHeaderUtil.setTextures(item, template.getTexture());
        // set item stack display name
        String display = StringUtils.isBlank(template.getDisplay()) ? String.format("§4%s %s", name, level) : template.getDisplay();
        meta.setDisplayName(display);
        // set item stack lore
        meta.setLore(template.getLore());
        // set persistent data
        NamespacedKey mainKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_MAIN_KEY);
        NamespacedKey subKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_SUB_KEY);
        NamespacedKey levelKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_LEVEL_KEY);
        PersistentDataContainer mainContainer = meta.getPersistentDataContainer();
        PersistentDataContainer subContainer = mainContainer.getAdapterContext().newPersistentDataContainer();
        mainContainer.set(mainKey, PersistentDataType.STRING, name);
        subContainer.set(levelKey, PersistentDataType.INTEGER, level);
        mainContainer.set(subKey, PersistentDataType.TAG_CONTAINER, subContainer);

        return meta;
    }
}
