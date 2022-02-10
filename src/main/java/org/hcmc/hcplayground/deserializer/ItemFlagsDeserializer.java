package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.inventory.ItemFlag;

import java.lang.reflect.Type;

public class ItemFlagsDeserializer implements JsonDeserializer<ItemFlag> {

    @Override
    public ItemFlag deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        ItemFlag[] flag = ItemFlag.values();
        for (ItemFlag m : flag) {
            if (m.name().equalsIgnoreCase(jsonElement.getAsString())) {
                return m;
            }
        }

        return null;
    }
}