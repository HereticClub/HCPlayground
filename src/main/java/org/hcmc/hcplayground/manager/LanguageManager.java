package org.hcmc.hcplayground.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class LanguageManager {

    public static Map<String, String> Messages = new HashMap<>();

    public LanguageManager() {

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

    public static String getMessage(String key) {
        return Messages.get(key);
    }

    public static String getMessage(String key, CommandSender sender) {
        String value = Messages.get(key);
        if (sender instanceof Player player) value = PlaceholderAPI.setPlaceholders(player, value);
        return value;
    }
}
