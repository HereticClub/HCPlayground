package org.hcmc.hcplayground.model.parkour;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public class CourseInfo implements Comparable<CourseInfo> {

    @Expose
    @SerializedName(value = "name")
    private String name;
    @Expose
    @SerializedName(value = "abandon")
    private boolean abandon=false;
    @Expose
    @SerializedName(value = "world")
    private String world = "";
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

    private String id;

    public CourseInfo() {

    }

    /**
     *
     * @param location 赛道的位置
     * @param name 赛道的名称，可以包含颜色代码，同时被转化成为Id
     */
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
        if ((name.charAt(0) == '&' || name.charAt(0) == '§') && name.length() >= 3) id = name.substring(2);
    }

    public boolean isAbandon() {
        return abandon;
    }

    public void setAbandon(boolean abandon) {
        this.abandon = abandon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public int compareTo(@NotNull CourseInfo o) {
        return this.id.compareTo(o.id);
    }
}
