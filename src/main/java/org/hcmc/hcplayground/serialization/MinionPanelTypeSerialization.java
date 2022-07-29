package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.enums.MinionPanelSlotType;

import java.lang.reflect.Type;
import java.util.Arrays;

public class MinionPanelTypeSerialization implements JsonDeserializer<MinionPanelSlotType> {

    public MinionPanelTypeSerialization() {

    }

    @Override
    public MinionPanelSlotType deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        //MinionPanelSlotType[] values = MinionPanelSlotType.values();
        //return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
        return resolvePanelType(jsonElement.getAsString());
    }

    public static MinionPanelSlotType resolvePanelType(String name) {
        MinionPanelSlotType[] values = MinionPanelSlotType.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(name)).findAny().orElse(null);
    }
}
