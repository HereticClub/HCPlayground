package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class EntityTypeDeserializer implements JsonDeserializer<EntityType> {

    @Override
    public EntityType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        EntityType[] Types = EntityType.values();
        return Arrays.stream(Types).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
