package org.hcmc.hcplayground.model.config;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Authme {

    @Expose
    @SerializedName(value = "remainInterval")
    public int remainInterval = 5;
    @Expose
    @SerializedName(value = "timeout")
    public int timeout = 120;

    public Authme() {

    }
}
