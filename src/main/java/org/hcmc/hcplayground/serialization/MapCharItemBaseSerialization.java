package org.hcmc.hcplayground.serialization;

import com.google.gson.*;
import org.bukkit.Material;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.model.item.ItemBase;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MapCharItemBaseSerialization implements JsonDeserializer<Map<Character, ItemBase>> {

    public MapCharItemBaseSerialization() {

    }

    @Override
    public Map<Character, ItemBase> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Map<Character, ItemBase> map = new HashMap<>();
        JsonArray elements = jsonElement.getAsJsonArray();
        for (int i = 0; i < elements.size(); i++) {
            String[] keys = elements.get(i).getAsString().split(",");
            ItemBase ib = ItemManager.findItemById(keys[1].trim());
            if (ib == null)
                ib = ItemManager.createItemBase(Material.valueOf(keys[1].toUpperCase().trim()), 1);

            map.put(keys[0].charAt(0), ib);
        }

        return map;
    }
}
