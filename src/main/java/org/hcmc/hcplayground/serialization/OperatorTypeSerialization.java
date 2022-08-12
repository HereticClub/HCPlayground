package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.OperatorType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class OperatorTypeSerialization implements JsonDeserializer<OperatorType> {

    public OperatorTypeSerialization() {

    }

    @Override
    public OperatorType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return getValue(jsonElement.getAsString());
    }

    public static OperatorType getValue(String name) {
        OperatorType[] types = OperatorType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
