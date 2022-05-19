package org.hcmc.hcplayground.model.parkour;

import org.bukkit.Location;
import org.bukkit.World;

public class PACourse {

    private String name;
    private double X;
    private double Y;
    private double Z;
    private float yaw;
    private float pitch;
    private String world = "";

    public PACourse() {

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

    public PACourse(Location location, String name) {
        World w = location.getWorld();
        if (w != null) world = w.getName();
        X = location.getY();
        Y = location.getY();
        Z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();

        this.name = name;
    }
}
