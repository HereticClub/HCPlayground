package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.model.menu.RecipeMenuPanel;

import java.lang.reflect.Type;
import java.util.Arrays;

public class RecipeTypeSerialization implements JsonDeserializer<RecipeType> {

    public RecipeTypeSerialization() {

    }

    @Override
    public RecipeType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return valueOf(jsonElement.getAsString());
    }

    public static RecipeType valueOf(String name) {
        RecipeType[] types = RecipeType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(RecipeType.UNDEFINED);
    }
}
