package org.hcmc.hcplayground.serialization;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Type;

public class LocationSerialization implements JsonDeserializer<Location>, JsonSerializer<Location> {

    public LocationSerialization() {

    }

    @Override
    public Location deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jo = jsonElement.getAsJsonObject();
        double x = jo.get("x").getAsDouble();
        double y = jo.get("y").getAsDouble();
        double z = jo.get("z").getAsDouble();
        float yaw = jo.get("yaw").getAsFloat();
        float pitch = jo.get("pitch").getAsFloat();
        String world = jo.get("world").getAsString();

        World w = Bukkit.getWorld(world);
        Location location = new Location(w, x, y, z, yaw, pitch);

        return location;
    }

    @Override
    public JsonElement serialize(Location location, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jo = new JsonObject();
        String world = "";
        World w = location.getWorld();
        if (w != null) world = w.getName();

        jo.addProperty("x", location.getX());
        jo.addProperty("y", location.getY());
        jo.addProperty("z", location.getZ());
        jo.addProperty("yaw", location.getYaw());
        jo.addProperty("pitch", location.getPitch());
        jo.addProperty("world", world);
        return jo;
    }
}
