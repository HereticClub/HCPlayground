package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Material;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class PermissionDefaultDeserializer implements JsonDeserializer<PermissionDefault> {

    public PermissionDefaultDeserializer() {

    }

    @Override
    public PermissionDefault deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<PermissionDefault> permissionDefaults = Arrays.stream(PermissionDefault.values()).toList();
        PermissionDefault value = permissionDefaults.stream().filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);

        return value;
    }
}
