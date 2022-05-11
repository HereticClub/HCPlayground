package org.hcmc.hcplayground.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

// 自定义可放置方块的摆放记录类
public class CrazyRecord {

    @Expose
    @SerializedName(value = "name")
    private String name;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CrazyRecord() {

    }

    public CrazyRecord(String name, Location location) {
        this.name = name;

        x = location.getX();
        y = location.getY();
        z = location.getZ();
        pitch = location.getPitch();
        yaw = location.getYaw();

        World w = location.getWorld();
        if (w == null) return;
        world = w.getName();
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(world);
        return new Location(w, x, y, z, yaw, pitch);
    }
}
