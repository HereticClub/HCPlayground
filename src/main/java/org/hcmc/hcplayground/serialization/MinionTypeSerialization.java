package org.hcmc.hcplayground.serialization;

import com.google.gson.*;
import org.hcmc.hcplayground.enums.MinionType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MinionTypeSerialization implements JsonDeserializer<MinionType>, JsonSerializer<MinionType> {

    public MinionTypeSerialization() {

    }

    @Override
    public MinionType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        MinionType[] types = MinionType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }

    @Override
    public JsonElement serialize(MinionType minionType, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(minionType.name());
    }
}
