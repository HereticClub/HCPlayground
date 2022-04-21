package org.hcmc.hcplayground.model.inventory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventorySlot {

    private final static String COMMAND_PERFORM_CONSOLE = "[console]";
    private final static String COMMAND_PERFORM_PLAYER = "[player]";
    private final static String COMMAND_PERFORM_OP = "[op]";

    private final static String[] CommandPerformTypes = new String[]{
            COMMAND_PERFORM_CONSOLE,
            COMMAND_PERFORM_PLAYER,
            COMMAND_PERFORM_OP,
    };

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

    public InventorySlot() {

    }

    public void runCommand(Player player, String[] commands) {
        for (String s : commands) {
            int index = s.indexOf(" ");
            if (index <= -1) {
                player.performCommand(s);
                continue;
            }
            String key = s.substring(0, index - 1);
            String command = s.substring(index);
            if (key.equalsIgnoreCase(COMMAND_PERFORM_PLAYER)) {
                player.performCommand(command);
                continue;
            }

            if (key.equalsIgnoreCase(COMMAND_PERFORM_CONSOLE)) {
                // TODO: 用控制台执行命令
            }

            if (key.equalsIgnoreCase(COMMAND_PERFORM_OP)) {
                // TODO: 以玩家作为OP身份执行命令
            }
        }
    }

    private void runCommand(CommandSender sender, String[] commands) {

    }
}
