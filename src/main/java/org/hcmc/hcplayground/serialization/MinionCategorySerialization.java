package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.MinionCategory;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MinionCategorySerialization implements JsonDeserializer<MinionCategory> {

    public MinionCategorySerialization() {

    }

    @Override
    public MinionCategory deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        MinionCategory[] categories = MinionCategory.values();
        String element = jsonElement.getAsString();
        return Arrays.stream(categories).filter(x -> x.name().equalsIgnoreCase(element)).findAny().orElse(null);
    }
}
