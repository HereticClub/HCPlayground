package org.hcmc.hcplayground.manager;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.util.*;

public class BroadcastManager {

    public static String Prefix;
    public static int Interval;
    public static List<String> Messages;
    public static Map<String, List<String>> MultilineMessages = new HashMap<>();

    public static void Load(YamlConfiguration yaml) {

        Prefix = Objects.requireNonNull(yaml.getString("prefix")).replace("&", "§");
        Interval = yaml.getInt("interval");
        Messages = yaml.getStringList("singleLineMessages");
        MultilineMessages.clear();
        ConfigurationSection section = yaml.getConfigurationSection("multilineMessages");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String s : keys) {
                List<String> multiline = section.getStringList(s);
                MultilineMessages.put(s, multiline);
            }
        }
    }

    public static List<String> randomMessage() {
        List<String> result = new ArrayList<>();

        int number = RandomNumber.getRandomInteger(Messages.size());
        String message = Messages.get(number);
        List<String> multiline = MultilineMessages.get(message);

        if (multiline != null) {
            multiline.replaceAll(x -> x.replace("&", "§"));
            result.addAll(multiline);
        } else {
            result.add(String.format("%s %s", Prefix, message.replace("&", "§")));
        }

        return result;
    }

    public static List<String> getMessage(String name) {
        List<String> result = new ArrayList<>();

        List<String> multiline = MultilineMessages.get(name);
        String message = Messages.stream().filter(x -> x.equalsIgnoreCase(name)).findAny().orElse(null);
        if (StringUtils.isBlank(message) && multiline == null) {
            result.add(String.format("%s %s", Prefix, name.replace("&", "§")));
            return result;
        }
        if (multiline != null) {
            multiline.replaceAll(x -> x.replace("&", "§"));
            result.addAll(multiline);
        } else {
            result.add(String.format("%s %s", Prefix, message.replace("&", "§")));
        }

        return result;
    }
}
