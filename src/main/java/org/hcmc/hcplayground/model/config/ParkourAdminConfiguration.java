package org.hcmc.hcplayground.model.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ParkourAdminConfiguration {
    @Expose
    @SerializedName(value = "design_range")
    private int designRange;
    @Expose
    @SerializedName(value = "protect_range")
    private int protectRange;
    @Expose
    @SerializedName(value = "start_layer")
    private double startLayer;
    @Expose
    @SerializedName(value = "world")
    private String world;

    public int getDesignRange() {
        return designRange;
    }

    public int getProtectRange() {
        return protectRange;
    }

    public double getStartLayer() {
        return startLayer;
    }

    public String getWorld() {
        return world;
    }

    public ParkourAdminConfiguration() {

    }
}