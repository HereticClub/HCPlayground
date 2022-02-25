package org.hcmc.hcplayground.template;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.event.inventory.InventoryType;

import java.util.ArrayList;
import java.util.List;

public class TemplateItem {

    @Expose
    @SerializedName(value = "title")
    public String title;
    @Expose
    @SerializedName(value = "type")
    public InventoryType type = InventoryType.CHEST;
    @Expose
    @SerializedName(value = "size")
    public int size = 54;
    @Expose
    @SerializedName(value = "decorates")
    public List<TemplateSlot> decorates = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    public String id;

    public TemplateItem() {

    }
}
