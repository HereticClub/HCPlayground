package org.hcmc.hcplayground.serialization;

import com.google.gson.*;
import org.bukkit.GameMode;

import java.lang.reflect.Type;
import java.util.Arrays;

public class GameModeSerialization implements JsonDeserializer<GameMode>, JsonSerializer<GameMode> {

    public GameModeSerialization() {

    }

    @Override
    public GameMode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        GameMode[] values = GameMode.values();
        return Arrays.stream(values).filter(x->x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }

    @Override
    public JsonElement serialize(GameMode gameMode, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(gameMode.name());
    }
}
