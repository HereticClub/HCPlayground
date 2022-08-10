package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.PanelSlotType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class PanelSlotTypeSerialization implements JsonDeserializer<PanelSlotType> {

    public PanelSlotTypeSerialization() {

    }

    @Override
    public PanelSlotType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return resolveType(jsonElement.getAsString());
    }

    public static PanelSlotType resolveType(String name) {
        PanelSlotType[] values = PanelSlotType.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
