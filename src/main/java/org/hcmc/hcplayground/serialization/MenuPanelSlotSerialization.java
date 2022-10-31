package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.hcmc.hcplayground.model.menu.MenuPanelSlot;
import org.hcmc.hcplayground.utility.Global;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuPanelSlotSerialization implements JsonSerializer<List<MenuPanelSlot>> {

    public MenuPanelSlotSerialization() {

    }

    @Override
    public JsonElement serialize(List<MenuPanelSlot> slots, Type type, JsonSerializationContext jsonSerializationContext) {

        Map<String, MenuPanelSlot> map = new HashMap<>();
        for (MenuPanelSlot m : slots) {
            String[] keys = m.getId().split("\\.");
            map.put(keys[1], m);
        }

        String value = Global.GsonObject.toJson(map);
        return JsonParser.parseString(value);
    }
}
