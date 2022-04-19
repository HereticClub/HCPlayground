package org.hcmc.hcplayground.manager;

import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.hcmc.hcplayground.utility.Global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClearLagManager {

    public static int Interval;
    public static String ClearMessage;
    public static List<EntityType> Types;
    public static Map<Integer, String> Remind = new HashMap<>();

    public static void Load(YamlConfiguration yaml) {
        Interval = yaml.getInt("interval");
        String data = Global.GsonObject.toJson(yaml.getList("types"));
        Types = Global.GsonObject.fromJson(data, new TypeToken<List<EntityType>>() {
        }.getType());
        ClearMessage = yaml.getString("clearMessage");
        if (ClearMessage != null) ClearMessage = ClearMessage.replace('&', '§');

        List<String> reminds = yaml.getStringList("remind");
        for (String s : reminds) {
            String[] keys = s.split(",");
            if (keys.length <= 1) continue;

            Remind.put(Integer.valueOf(keys[1]), keys[0].replace('&', '§'));
        }
    }
}
