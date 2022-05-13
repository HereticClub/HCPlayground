package org.hcmc.hcplayground.serialization;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MapCharIntegerSerialization implements JsonDeserializer<Map<Character, Integer>> {

    public MapCharIntegerSerialization() {

    }

    @Override
    public Map<Character, Integer> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Map<Character, Integer> map = new HashMap<>();
        JsonArray elements = jsonElement.getAsJsonArray();
        for (int i = 0; i < elements.size(); i++) {
            String value = elements.get(i).getAsString();
            String[] keys = value.split(",");
            map.put(keys[0].charAt(0), Integer.valueOf(keys[1].trim()));
        }

        return map;
    }
}
