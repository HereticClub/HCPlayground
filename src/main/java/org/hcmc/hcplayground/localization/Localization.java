package org.hcmc.hcplayground.localization;

import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.Global;

import javax.lang.model.element.ModuleElement;
import java.lang.reflect.Type;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Localization {

    public static Map<String, String> Messages = new HashMap<>();

    public Localization() {

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
