package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LocalizationManager {

    public static Map<String, String> Messages = new HashMap<>();

    public LocalizationManager() {

    }

    public static void Load(YamlConfiguration yaml) {
        Messages.clear();

        ConfigurationSection section = yaml.getConfigurationSection("messages");
        if (section == null) return;

        Set<String> keys = section.getKeys(true);
        for (String s : keys) {
            Object obj = section.get(s);
            if (!(obj instanceof String value)) continue;

            Messages.put(s, value.replace('&', '§'));
        }
    }
}
