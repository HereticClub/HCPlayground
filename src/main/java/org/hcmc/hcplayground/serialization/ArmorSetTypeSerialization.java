package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.ArmorSetType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ArmorSetTypeSerialization implements JsonDeserializer<ArmorSetType> {

    public ArmorSetTypeSerialization() {

    }

    @Override
    public ArmorSetType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return parse(jsonElement.getAsString());
    }

    public static ArmorSetType parse(String name) {
        ArmorSetType[] types = ArmorSetType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
