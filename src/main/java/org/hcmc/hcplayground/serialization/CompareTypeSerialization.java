package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.CompareType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class CompareTypeSerialization implements JsonDeserializer<CompareType> {

    public CompareTypeSerialization() {

    }

    @Override
    public CompareType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return valueOf(jsonElement.getAsString());
    }

    public static CompareType valueOf(String name) {
        CompareType[] types = CompareType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
