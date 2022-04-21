package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;
import org.hcmc.hcplayground.utility.MaterialData;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class MaterialDeserializer implements JsonDeserializer<MaterialData> {

    public MaterialDeserializer() {

    }

    @Override
    public MaterialData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<Material> materials = Arrays.stream(Material.values()).toList();
        MaterialData data = new MaterialData();
        Material material;
        String element = jsonElement.getAsString();

        // 判断Material的名称，如果以"head_"为开头，则Material表示为PLAYER_HEAD
        String[] keys = element.split("_");
        if (keys.length >= 2 && keys[0].equalsIgnoreCase("head")) {
            material = Material.PLAYER_HEAD;
        } else {
            material = materials.stream().filter(x -> x.name().equalsIgnoreCase(element)).findFirst().orElse(null);
        }
        data.value = material;
        data.name = element;

        return data;
    }
}

