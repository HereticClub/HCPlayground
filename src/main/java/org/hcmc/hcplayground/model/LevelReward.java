package org.hcmc.hcplayground.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LevelReward {

    @Expose
    @SerializedName(value = "command")
    public String command = "";
    @Expose
    @SerializedName(value = "recipe")
    public String recipe = "";
    @Expose
    @SerializedName(value = "item")
    public String item = "";
    @Expose
    @SerializedName(value = "amount")
    public int amount = 0;

    public LevelReward() {

    }
}
