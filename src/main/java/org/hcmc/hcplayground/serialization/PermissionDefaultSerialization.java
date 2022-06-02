package org.hcmc.hcplayground.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class PermissionDefaultSerialization implements JsonDeserializer<PermissionDefault> {

    public PermissionDefaultSerialization() {

    }

    @Override
    public PermissionDefault deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        List<PermissionDefault> permissionDefaults = Arrays.stream(PermissionDefault.values()).toList();
        return permissionDefaults.stream().filter(x -> x.name().equalsIgnoreCase(jsonElement.getAsString())).findAny().orElse(null);
    }
}
