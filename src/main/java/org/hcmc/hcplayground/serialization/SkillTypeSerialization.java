package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.model.menu.SkillMenuPanel;

import java.lang.reflect.Type;
import java.util.Arrays;

public class SkillTypeSerialization implements JsonDeserializer<SkillMenuPanel.SkillType> {

    public SkillTypeSerialization() {

    }

    @Override
    public SkillMenuPanel.SkillType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return valueOf(jsonElement.getAsString());
    }

    public static SkillMenuPanel.SkillType valueOf(String name) {
        SkillMenuPanel.SkillType[] types = SkillMenuPanel.SkillType.values();
        return Arrays.stream(types).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(SkillMenuPanel.SkillType.UNDEFINED);
    }
}
