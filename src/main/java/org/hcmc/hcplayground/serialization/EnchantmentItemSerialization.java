package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.hcmc.hcplayground.model.enchantment.EnchantmentItem;

import java.lang.reflect.Type;

public class EnchantmentItemSerialization implements JsonDeserializer<EnchantmentItem> {

    public EnchantmentItemSerialization() {

    }

    @Override
    public EnchantmentItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String[] keys = jsonElement.getAsString().split(",");
        if (keys.length <= 1) return null;
        int level = Integer.parseInt(keys[1].trim());

        return new EnchantmentItem(keys[0], level);
    }
}
