package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.hcmc.hcplayground.enums.MinionType;

import java.util.Arrays;

public class MinionRecord {

    @Expose
    @SerializedName(value = "type")
    private String type;
    @Expose
    @SerializedName(value = "level")
    private int level;
    @Expose
    @SerializedName(value = "world")
    private String world;
    @Expose
    @SerializedName(value = "x")
    private double x;
    @Expose
    @SerializedName(value = "y")
    private double y;
    @Expose
    @SerializedName(value = "z")
    private double z;
    @Expose
    @SerializedName(value = "pitch")
    private float pitch;
    @Expose
    @SerializedName(value = "yaw")
    private float yaw;

    @Expose(serialize = false, deserialize = false)
    private String id;

    public MinionRecord() {

    }

    public MinionRecord(MinionType type, int level, Location location) {
        this.type = type.name();
        this.level = level;

        x = location.getX();
        y = location.getY();
        z = location.getZ();
        pitch = location.getPitch();
        yaw = location.getYaw();

        World w = location.getWorld();
        if (w == null) return;
        world = w.getName();
    }

    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public int getLevel() {
        return level;
    }

    public MinionType getType() {
        MinionType[] values = MinionType.values();
        return Arrays.stream(values).filter(x -> x.name().equalsIgnoreCase(type)).findAny().orElse(null);
    }
}
