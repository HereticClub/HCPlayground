package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.ItemFeatureType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class ItemFeatureTypeSerialization implements JsonDeserializer<ItemFeatureType> {

    public ItemFeatureTypeSerialization() {

    }

    @Override
    public ItemFeatureType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        ItemFeatureType[] values = ItemFeatureType.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
