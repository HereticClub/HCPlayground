package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.event.inventory.InventoryType;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class InventoryTypeSerialization implements JsonDeserializer<InventoryType> {

    public InventoryTypeSerialization() {

    }

    @Override
    public InventoryType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<InventoryType> types = Arrays.stream(InventoryType.values()).toList();
        String value = jsonElement.getAsString();

        return types.stream().filter(x -> x.name().equalsIgnoreCase(value)).findAny().orElse(null);
    }
}
