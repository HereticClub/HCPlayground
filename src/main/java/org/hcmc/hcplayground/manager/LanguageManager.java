package org.hcmc.hcplayground.manager;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LanguageManager {

    private static final Map<String, Object> messages = new HashMap<>();

    public LanguageManager() {

    }

    public static void Load(YamlConfiguration yaml) {
        messages.clear();

        ConfigurationSection section = yaml.getConfigurationSection("messages");
        if (section == null) return;

        Set<String> keys = section.getKeys(true);

        for (String s : keys) {
            Object obj = section.get(s);
            if (obj == null) continue;
            if (obj instanceof ArrayList<?>) {
                List<String> values = new ArrayList<>();
                for (Object o : (List<?>) obj) {
                    values.add(o.toString().replace('&', '§'));
                }
                messages.put(s, values);
            }
            if (obj instanceof String) {
                messages.put(s, obj.toString().replace('&', '§'));
            }
        }
    }

    public static String getString(String key) {
        return messages.get(key).toString();
    }

    public static String getString(String key, CommandSender sender) {
        String value = messages.get(key).toString();
        if (sender instanceof Player player) value = PlaceholderAPI.setPlaceholders(player, value);
        return value;
    }

    @NotNull
    public static List<String> getStringList(String key) {
        List<String> values = new ArrayList<>();
        Object obj = messages.get(key);
        if (!(obj instanceof List<?>)) return values;

        for (Object o : (List<?>) obj) {
            values.add(o.toString());
        }

        return values;
    }
}
