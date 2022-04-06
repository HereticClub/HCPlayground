package org.hcmc.hcplayground.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class LevelInfo {

    @Expose
    @SerializedName(value = "name")
    public String name = "";
    @Expose
    @SerializedName(value = "levels")
    public List<LevelItem> items = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    public String id;

    public LevelInfo() {

    }
}
