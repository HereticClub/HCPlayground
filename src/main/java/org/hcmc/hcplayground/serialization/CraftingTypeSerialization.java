package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.CraftingType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class CraftingTypeSerialization implements JsonDeserializer<CraftingType> {

    public CraftingTypeSerialization() {

    }

    @Override
    public CraftingType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        CraftingType[] types = CraftingType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
