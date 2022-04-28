package org.hcmc.hcplayground.model.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PotionConfiguration {

    @Expose
    @SerializedName(value = "refreshInterval")
    public int refreshInterval;

    public PotionConfiguration() {

    }
}
