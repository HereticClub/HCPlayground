package org.hcmc.hcplayground.model.permission;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class PermissionItem {
    @Expose(serialize = false, deserialize = false)
    public String id;

    @Expose
    @SerializedName(value = "name")
    public String name;
    @Expose
    @SerializedName(value = "default")
    public PermissionDefault defaultTo;
    @Expose
    @SerializedName(value = "description")
    public String description;
    @Expose
    @SerializedName(value = "children")
    public List<String> children = new ArrayList<>();

    public PermissionItem() {

    }
}
