package org.hcmc.hcplayground.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class CommandArgument {

    @Expose
    @SerializedName(value = "index")
    public int index = 1;
    @Expose
    @SerializedName(value = "name")
    public String name = "";
    @Expose
    @SerializedName(value = "permission")
    public String permission = "";
    @Expose
    @SerializedName(value = "parent")
    public List<String> parent = new ArrayList<>();

    public CommandArgument() {

    }
}
