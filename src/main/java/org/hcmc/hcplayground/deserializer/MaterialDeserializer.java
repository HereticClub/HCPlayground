package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class MaterialDeserializer implements JsonDeserializer<Material> {

    public MaterialDeserializer() {

    }

    @Override
    public Material deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<Material> materials = Arrays.stream(Material.values()).toList();
        String value = jsonElement.getAsString();

        return materials.stream().filter(x -> x.name().equalsIgnoreCase(value)).findFirst().orElse(null);
    }
}

