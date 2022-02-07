package org.hcmc.hcplayground.Deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;

import java.lang.reflect.Type;

public class MaterialDeserializer implements JsonDeserializer<Material> {

    @Override
    public Material deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Material[] materials = Material.values();
        for (Material m : materials) {
            if (m.name().equalsIgnoreCase(jsonElement.getAsString())) {
                return m;
            }
        }

        return null;
    }
}

