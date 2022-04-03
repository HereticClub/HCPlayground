package org.hcmc.hcplayground.mobs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.Global;

import java.util.ArrayList;
import java.util.List;

public class MobManager {

    public static List<MobEntity> MobEntities = new ArrayList<>();

    public MobManager() {

    }

    public static void Load(YamlConfiguration yml) throws IllegalAccessException {
        MobEntities = Global.SetItemList(yml, MobEntity.class);
    }
}
