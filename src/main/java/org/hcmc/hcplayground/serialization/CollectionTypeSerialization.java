package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.model.menu.CollectionMenuPanel;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.Arrays;

public class CollectionTypeSerialization implements JsonDeserializer<CollectionMenuPanel.CollectionType> {

    public CollectionTypeSerialization() {

    }

    @Override
    public CollectionMenuPanel.CollectionType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return valueOf(jsonElement.getAsString());
    }

    @NotNull
    public static CollectionMenuPanel.CollectionType valueOf(String name) {
        CollectionMenuPanel.CollectionType[] types = CollectionMenuPanel.CollectionType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(CollectionMenuPanel.CollectionType.UNDEFINED);
    }
}
