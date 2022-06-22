package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionType;

import java.util.Arrays;

public class MinionManager {

    public final static String PERSISTENT_MAIN_KEY = "minion";
    public final static String PERSISTENT_SUB_KEY = "content";
    public final static String PERSISTENT_LEVEL_KEY = "level";

    private final static Plugin plugin = HCPlayground.getInstance();

    public MinionManager() {

    }

    public static ItemStack createMinion() {

        return MinionType.COBBLESTONE.getMinion(1, 1);
    }

    public static boolean isMinionType(String type) {
        MinionType[] values = MinionType.values();
        MinionType value = Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(type)).findAny().orElse(null);
        return value != null;
    }

    public static boolean isMinion(ItemStack item) {
        NamespacedKey mainKey = new NamespacedKey(plugin, MinionManager.PERSISTENT_MAIN_KEY);
        ItemMeta im = item.getItemMeta();
        if (!(im instanceof SkullMeta meta)) return false;

        PersistentDataContainer mainContainer = meta.getPersistentDataContainer();
        String minionType = mainContainer.get(mainKey, PersistentDataType.STRING);
        if (StringUtils.isBlank(minionType)) return false;

        MinionType[] types = MinionType.values();
        MinionType type = Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(minionType)).findAny().orElse(null);
        return type != null;
    }
}
