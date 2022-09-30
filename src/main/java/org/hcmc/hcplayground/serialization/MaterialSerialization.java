package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MaterialSerialization implements JsonDeserializer<Material> {

    public MaterialSerialization() {

    }

    @Override
    @NotNull
    public Material deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return valueOf(jsonElement.getAsString());
    }

    @NotNull
    public static Material valueOf(String name) {
        Material[] values = Material.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(Material.AIR);
    }
}
