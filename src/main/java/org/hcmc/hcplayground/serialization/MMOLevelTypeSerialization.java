package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.MMOSkillType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MMOLevelTypeSerialization implements JsonDeserializer<MMOSkillType> {

    public MMOLevelTypeSerialization() {

    }

    @Override
    public MMOSkillType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return getType(jsonElement.getAsString());
    }

    public static MMOSkillType getType(String name) {
        MMOSkillType[] types = MMOSkillType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
