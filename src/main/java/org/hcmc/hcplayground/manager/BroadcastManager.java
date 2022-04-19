package org.hcmc.hcplayground.manager;

import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.utility.Global;
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

        String data = Global.GsonObject.toJson(yaml.getStringList("singleLineMessages")).replace('&', '§');
        Messages = Global.GsonObject.fromJson(data, new TypeToken<List<String>>() {
        }.getType());

        MultilineMessages.clear();
        ConfigurationSection section = yaml.getConfigurationSection("multilineMessages");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String s : keys) {
                String value = Global.GsonObject.toJson(section.getStringList(s)).replace('&', '§');
                List<String> mm = Global.GsonObject.fromJson(value, new TypeToken<List<String>>() {
                }.getType());
                MultilineMessages.put(s, mm);
            }
        }
    }

    public static List<String> RandomMessage() {
        List<String> result = new ArrayList<>();

        int number = RandomNumber.getRandomNumber(Messages.size());
        String message = Messages.get(number);
        List<String> multiline = MultilineMessages.get(message);

        if (multiline != null) {
            result.addAll(multiline);
        } else {
            result.add(String.format("%s %s", Prefix, message));
        }

        return result;
    }
}
