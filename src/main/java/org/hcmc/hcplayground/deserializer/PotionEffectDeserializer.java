package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Type;

public class PotionEffectDeserializer implements JsonDeserializer<PotionEffect> {
    @Override
    public PotionEffect deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String[] keys = jsonElement.getAsString().split(",");
        if (keys.length <= 2) return null;

        int duration = Integer.parseInt(keys[1].trim());
        int amplifier = Integer.parseInt(keys[2].trim());

        PotionEffectType[] slot = PotionEffectType.values();
        for (PotionEffectType p : slot) {
            if (p.getName().equalsIgnoreCase(keys[0])) {
                return new PotionEffect(p, duration, amplifier);
            }
        }

        return null;
    }
}
