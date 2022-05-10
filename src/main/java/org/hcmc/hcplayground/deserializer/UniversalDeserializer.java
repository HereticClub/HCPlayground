package org.hcmc.hcplayground.deserializer;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.hcmc.hcplayground.model.player.CrazyBlockRecord;
import org.hcmc.hcplayground.serializer.UniversalSerializable;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class UniversalDeserializer implements JsonDeserializer<ConfigurationSerializable>, JsonSerializer<ConfigurationSerializable> {
    @Override
    public ConfigurationSerializable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        final Map<String, Object> map = new LinkedHashMap<>();
        Set<Map.Entry<String, JsonElement>> entries = jsonElement.getAsJsonObject().entrySet();
        for (Map.Entry<String, JsonElement> entry : entries) {
            String name = entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject() && value.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                map.put(name, this.deserialize(value, value.getClass(), context));
            } else {
                map.put(name, context.deserialize(value, Object.class));
            }
        }

        return ConfigurationSerialization.deserializeObject(map);
    }

    @Override
    public JsonElement serialize(ConfigurationSerializable source, Type type, JsonSerializationContext context) {
        final Type objectStringMapType = new TypeToken<Map<String, Object>>() {}.getType();
        final Map<String, Object> map = new LinkedHashMap<>();

        map.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(source.getClass()));
        map.putAll(source.serialize());

        return context.serialize(map, objectStringMapType);
    }
}
