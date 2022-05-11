package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.inventory.EquipmentSlot;

import java.lang.reflect.Type;

public class EquipmentSlotSerialization implements JsonDeserializer<EquipmentSlot> {

    @Override
    public EquipmentSlot deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        EquipmentSlot[] slot = EquipmentSlot.values();
        for (EquipmentSlot e : slot) {
            if (e.name().equalsIgnoreCase(jsonElement.getAsString())) {
                return e;
            }
        }

        return null;
    }
}