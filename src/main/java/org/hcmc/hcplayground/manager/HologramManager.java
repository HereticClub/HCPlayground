package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.hologram.HologramItem;
import org.hcmc.hcplayground.utility.Global;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HologramManager {

    private static List<HologramItem> holograms = new ArrayList<>();

    public HologramManager() {

    }

    public static List<HologramItem> getHolograms() {
        return holograms;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException, SQLException {
        holograms = Global.deserializeList(yaml, HologramItem.class);
        /*
        TODO: 实施利用盔甲架创建漂浮字体
        for (HologramItem hologram : holograms) {
            hologram.create();
        }
         */
    }
}
