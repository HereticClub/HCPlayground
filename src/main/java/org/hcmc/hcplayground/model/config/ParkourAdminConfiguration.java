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
    private int startLayer;

    public int getDesignRange() {
        return designRange;
    }

    public int getProtectRange() {
        return protectRange;
    }

    public int getStartLayer() {
        return startLayer;
    }

    public ParkourAdminConfiguration() {

    }
}
