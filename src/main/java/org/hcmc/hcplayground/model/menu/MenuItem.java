package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.inventory.ItemFlag;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {

    private final static String COMMAND_PERFORM_CONSOLE = "[console]";
    private final static String COMMAND_PERFORM_PLAYER = "[player]";
    private final static String COMMAND_PERFORM_OP = "[op]";

    @Expose
    @SerializedName(value = "canDrag")
    public boolean draggable = false;
    @Expose
    @SerializedName(value = "canDrop")
    public boolean droppable = false;
    @Expose
    @SerializedName(value = "displayText")
    public String text;
    @Expose
    @SerializedName(value = "slot")
    public int number;
    @Expose
    @SerializedName(value = "amount")
    public int amount = 1;
    @Expose
    @SerializedName(value = "material")
    public MaterialData material;
    @Expose
    @SerializedName(value = "customSkull")
    public String customSkull = "";
    @Expose
    @SerializedName(value = "lore")
    public List<String> lore = new ArrayList<>();
    @Expose
    @SerializedName(value = "left_click")
    public List<String> leftCommands = new ArrayList<>();
    @Expose
    @SerializedName(value = "right_click")
    public List<String> rightCommands = new ArrayList<>();
    @Expose
    @SerializedName(value = "flags")
    public List<ItemFlag> flags = new ArrayList<>();
    @Expose
    @SerializedName(value = "glowing")
    public boolean glowing = false;

    public MenuItem() {

    }
}
