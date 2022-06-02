package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.CcmdActionType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class CcmdActionTypeSerialization implements JsonDeserializer<CcmdActionType> {

    public CcmdActionTypeSerialization() {

    }

    @Override
    public CcmdActionType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        CcmdActionType[] types = CcmdActionType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
