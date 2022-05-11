package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;

import java.lang.reflect.Type;

public class NamespacedKeySerialization implements JsonDeserializer<NamespacedKey> {

    public NamespacedKeySerialization() {

    }

    @Override
    public NamespacedKey deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JavaPlugin plugin = HCPlayground.getPlugin();
        String value = jsonElement.getAsString();
        return new NamespacedKey(plugin, value);
    }
}
