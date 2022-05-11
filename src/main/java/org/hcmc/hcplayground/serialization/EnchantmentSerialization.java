package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.enchantments.Enchantment;

import java.lang.reflect.Type;
import java.util.Arrays;

public class EnchantmentSerialization implements JsonDeserializer<Enchantment> {
    @Override
    public Enchantment deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        Enchantment[] enchantments = Enchantment.values();
        return Arrays.stream(enchantments).filter(x -> x.getKey().getKey().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
