package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.model.item.ItemBase;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ItemBaseSerialization implements JsonDeserializer<ItemBase> {

    public ItemBaseSerialization() {

    }

    @Override
    public ItemBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String jsonValue = jsonElement.getAsString();
        List<Material> materials = Arrays.stream(Material.values()).toList();
        Material m = materials.stream().filter(x -> x.name().equalsIgnoreCase(jsonValue)).findAny().orElse(null);
        if (m == null) m = Material.STONE;

        ItemBase ib = ItemManager.findItemById(jsonValue);
        if (ib == null) ib = ItemManager.createItemBase(UUID.randomUUID().toString(), m);

        return ib;
    }
}
