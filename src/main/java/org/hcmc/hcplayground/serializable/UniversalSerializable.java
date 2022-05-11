package org.hcmc.hcplayground.serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public abstract class UniversalSerializable implements ConfigurationSerializable {

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            Expose expose = field.getDeclaredAnnotation(Expose.class);
            SerializedName serializedName = field.getDeclaredAnnotation(SerializedName.class);

            if (expose == null) continue;
            if (!expose.serialize()) continue;

            field.setAccessible(true);

            try {
                String mapName = serializedName == null ? field.getName() : serializedName.value();
                Object mapValue = field.get(this);
                map.put(mapName, mapValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return map;
    }
}
