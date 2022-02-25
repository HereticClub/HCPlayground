package org.hcmc.hcplayground.level;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LevelItem {

    @Expose
    @SerializedName(value = "level")
    public int value = 0;
    @Expose
    @SerializedName(value = "lores")
    public List<String> lores = new ArrayList<>();
    @Expose
    @SerializedName(value = "reward")
    public LevelReward reward = new LevelReward();

    public LevelItem() {

    }
}
