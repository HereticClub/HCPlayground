package org.hcmc.hcplayground.level;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.Global;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {

    public static List<Level> Levels = new ArrayList<>();

    public LevelManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        Levels = Global.SetItemList(yaml, Level.class);
    }
}
