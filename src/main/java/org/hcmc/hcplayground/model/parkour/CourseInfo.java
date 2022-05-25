package org.hcmc.hcplayground.model.parkour;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class CourseInfo {

    @Expose
    @SerializedName(value = "name")
    private String name;
    @Expose
    @SerializedName(value = "x")
    private double X;
    @Expose
    @SerializedName(value = "y")
    private double Y;
    @Expose
    @SerializedName(value = "z")
    private double Z;
    @Expose
    @SerializedName(value = "yaw")
    private float yaw;
    @Expose
    @SerializedName(value = "pitch")
    private float pitch;
    @Expose
    @SerializedName(value = "world")
    private String world = "";

    private String id;

    public CourseInfo() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CourseInfo(Location location, String name) {
        World w = location.getWorld();
        if (w != null) world = w.getName();

        X = location.getX();
        Y = location.getY();
        Z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();

        this.name = name;

        id = name;
        if (name.charAt(0) == '&' || name.charAt(0) == '§') id = name.substring(2);
    }

    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        if (world == null) return null;

        return new Location(w, X, Y, Z, yaw, pitch);
    }

    public String getName() {
        return name;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }

    public double getZ() {
        return Z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

}
