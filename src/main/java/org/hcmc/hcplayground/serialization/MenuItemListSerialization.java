package org.hcmc.hcplayground.serialization;

import com.google.gson.*;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.utility.Global;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuItemListSerialization implements JsonSerializer<List<MenuItem>> {

    public MenuItemListSerialization() {

    }

    @Override
    public JsonElement serialize(List<MenuItem> menuItems, Type type, JsonSerializationContext jsonSerializationContext) {

        Map<String, MenuItem> map = new HashMap<>();
        for (MenuItem m : menuItems) {
            String[] keys = m.id.split("\\.");
            map.put(keys[1], m);
        }

        String value = Global.GsonObject.toJson(map);
        return JsonParser.parseString(value);
    }
}
