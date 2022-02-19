package org.hcmc.hcplayground.playerManager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.Global;

import java.io.File;
import java.io.IOException;

public class PlayerManager {

    public PlayerManager() {

    }

    public static void Load(File file) {
        if (!file.exists()) {
            Global.yamlPlayer = new YamlConfiguration();
        } else {
            Global.yamlPlayer = YamlConfiguration.loadConfiguration(file);
        }
    }

    public static void Save(File file) {
        try {
            if (Global.yamlPlayer != null) Global.yamlPlayer.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
