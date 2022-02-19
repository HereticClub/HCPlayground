package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;
import org.hcmc.hcplayground.itemManager.ItemBase;
import org.hcmc.hcplayground.itemManager.ItemManager;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class ItemBaseDeserializer implements JsonDeserializer<ItemBase> {

    public ItemBaseDeserializer() {

    }

    @Override
    public ItemBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String jsonValue = jsonElement.getAsString();
        List<Material> materials = Arrays.stream(Material.values()).toList();
        Material m = materials.stream().filter(x -> x.name().equalsIgnoreCase(jsonValue)).findAny().orElse(null);
        ItemBase ib = ItemManager.FindItemById(jsonValue);

        if (ib == null) {
            ib = new ItemBase();
            ib.id = null;
            ib.material = m;
        }

        return ib;
    }
}
