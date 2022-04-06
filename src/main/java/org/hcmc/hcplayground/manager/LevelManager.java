package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.model.LevelInfo;

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
