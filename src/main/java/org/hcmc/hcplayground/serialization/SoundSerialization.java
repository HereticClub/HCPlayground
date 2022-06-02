package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Sound;

import java.lang.reflect.Type;
import java.util.Arrays;

public class SoundSerialization implements JsonDeserializer<Sound> {

    public SoundSerialization() {

    }

    @Override
    public Sound deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Sound[] values = Sound.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
