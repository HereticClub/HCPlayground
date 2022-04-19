package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.level.LevelInfo;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;

public class LevelManager {

    public static List<LevelInfo> levelInfos = new ArrayList<>();

    public LevelManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        levelInfos = Global.SetItemList(yaml, LevelInfo.class);
    }
}
