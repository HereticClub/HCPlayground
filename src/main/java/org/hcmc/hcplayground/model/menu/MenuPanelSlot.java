package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.hcmc.hcplayground.manager.CommandManager;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.ArrayList;
import java.util.List;

public class MenuPanelSlot {

    private final static String COMMAND_PERFORM_CONSOLE = "[console]";
    private final static String COMMAND_PERFORM_PLAYER = "[player]";

    @Expose
    @SerializedName(value = "display")
    private String display;
    @Expose
    @SerializedName(value = "slots")
    private List<Integer> slots = new ArrayList<>();
    @Expose
    @SerializedName(value = "amount")
    private int amount = 1;
    @Expose
    @SerializedName(value = "material")
    private MaterialData material;
    @Expose
    @SerializedName(value = "customSkull")
    private String customSkull = "";
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();
    @Expose
    @SerializedName(value = "left_click")
    private List<String> leftCommands = new ArrayList<>();
    @Expose
    @SerializedName(value = "right_click")
    private List<String> rightCommands = new ArrayList<>();
    @Expose
    @SerializedName(value = "flags")
    private List<ItemFlag> flags = new ArrayList<>();
    @Expose
    @SerializedName(value = "glowing")
    private boolean glowing = false;

    @Expose(serialize = false, deserialize = false)
    private String id;

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getAmount() {
        return amount;
    }

    public String getDisplay() {
        return display;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public List<ItemFlag> getFlags() {
        return flags;
    }

    public List<String> getLeftCommands() {
        return leftCommands;
    }

    public List<String> getRightCommands() {
        return rightCommands;
    }

    public MaterialData getMaterial() {
        return material;
    }

    public void setMaterial(MaterialData material) {
        this.material = material;
    }

    public String getCustomSkull() {
        return customSkull;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public MenuPanelSlot() {

    }

    @Override
    public String toString() {
        return String.format("%s, %s", id, display);
    }

    public void runLeftClickCommands(Player player) {
        runCommands(player, leftCommands);
    }

    public void runRightClickCommands(Player player) {
        runCommands(player, rightCommands);
    }

    private void runCommands(Player player, List<String> commands) {
        for (String command : commands) {

            int location = command.indexOf(" ");
            if (location <= -1) {
                CommandManager.runPlayerCommand(command, player);
                return;
            }

            String prefix = command.substring(0, location);
            String _command = command.substring(location + 1);
            if (prefix.equalsIgnoreCase(COMMAND_PERFORM_CONSOLE)) {
                CommandManager.runConsoleCommand(_command, player);
            }
            if (prefix.equalsIgnoreCase(COMMAND_PERFORM_PLAYER)) {
                CommandManager.runPlayerCommand(_command, player);
            }
            if (!prefix.equalsIgnoreCase(COMMAND_PERFORM_PLAYER) && !prefix.equalsIgnoreCase(COMMAND_PERFORM_CONSOLE)) {
                CommandManager.runPlayerCommand(command, player);
            }
        }
    }
}
