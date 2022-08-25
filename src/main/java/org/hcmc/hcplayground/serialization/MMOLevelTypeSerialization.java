package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.MMOType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MMOLevelTypeSerialization implements JsonDeserializer<MMOType> {

    public MMOLevelTypeSerialization() {

    }

    @Override
    public MMOType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return getType(jsonElement.getAsString());
    }

    @NotNull
    public static MMOType getType(@NotNull String name) {
        MMOType[] types = MMOType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(MMOType.UNDEFINED);
    }
}
