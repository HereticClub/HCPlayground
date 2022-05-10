package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.CrazyBlockType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class CrazyTypeDeserializer implements JsonDeserializer<CrazyBlockType> {

    public CrazyTypeDeserializer() {

    }

    @Override
    public CrazyBlockType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        CrazyBlockType[] types = CrazyBlockType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
