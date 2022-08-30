package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.MMOType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MMOTypeSerialization implements JsonDeserializer<MMOType> {

    public MMOTypeSerialization() {

    }

    @Override
    public MMOType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return parse(jsonElement.getAsString());
    }

    @NotNull
    public static MMOType parse(@NotNull String name) {
        MMOType[] types = MMOType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(MMOType.UNDEFINED);
    }
}
