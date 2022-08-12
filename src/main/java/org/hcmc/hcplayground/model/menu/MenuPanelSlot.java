package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.hcmc.hcplayground.manager.CommandManager;
import org.hcmc.hcplayground.utility.MaterialData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuPanelSlot {

    private final static String COMMAND_PERFORM_CONSOLE = "[console]";
    private final static String COMMAND_PERFORM_PLAYER = "[player]";
    private final static String COMMAND_PERFORM_MESSAGE = "[message]";
    private final static String COMMAND_PERFORM_CLOSE = "[close]";

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

    @Expose(deserialize = false)
    private String id;
    @Expose(deserialize = false)
    private List<String> leftDenyMessage = new ArrayList<>();
    @Expose(deserialize = false)
    private List<String> rightDenyMessage = new ArrayList<>();
    /**
     * 左键点击条件，所有条件必须判断为true，才被判断为成功
     */
    @Expose(deserialize = false)
    private List<SlotClickCondition> leftConditions = new ArrayList<>();
    /**
     * 右键点击条件，所有条件必须判断为true，才被判断为成功
     */
    @Expose(deserialize = false)
    private List<SlotClickCondition> rightConditions = new ArrayList<>();

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

    public List<String> getLeftDenyMessage() {
        return leftDenyMessage;
    }

    public void setLeftDenyMessage(List<String> leftDenyMessage) {
        this.leftDenyMessage = leftDenyMessage;
    }

    public List<String> getRightDenyMessage() {
        return rightDenyMessage;
    }

    public void setRightDenyMessage(List<String> rightDenyMessage) {
        this.rightDenyMessage = rightDenyMessage;
    }

    public List<SlotClickCondition> getLeftConditions() {
        return leftConditions;
    }

    public void setLeftConditions(List<SlotClickCondition> leftConditions) {
        this.leftConditions = leftConditions;
    }

    public List<SlotClickCondition> getRightConditions() {
        return rightConditions;
    }

    public void setRightConditions(List<SlotClickCondition> rightConditions) {
        this.rightConditions = rightConditions;
    }

    public MenuPanelSlot() {

    }

    @Override
    public String toString() {
        return String.format("%s, %s", id, display);
    }

    public void runLeftClickCommands(Player player) {
        runCommands(player, leftCommands, leftConditions, leftDenyMessage);
    }

    public void runRightClickCommands(Player player) {
        runCommands(player, rightCommands, rightConditions, rightDenyMessage);
    }

    private boolean getConditionResult(Player player, List<SlotClickCondition> conditions) {
        boolean result = true;
        for (SlotClickCondition condition : conditions) {
            result = condition.getResult(player);
            if (!result) break;
        }
        return result;
    }

    private void runCommands(Player player, List<String> commands, List<SlotClickCondition> conditions, List<String> denyMessage) {
        if (!getConditionResult(player, conditions)) {
            player.sendMessage(denyMessage.toArray(new String[0]));
            return;
        }

        for (String command : commands) {
            int location = command.indexOf(" ");
            if (location <= -1) {
                if (command.equalsIgnoreCase(COMMAND_PERFORM_CLOSE)) {
                    player.getOpenInventory().close();
                } else {
                    CommandManager.runPlayerCommand(command, player);
                }
                return;
            }

            String _prefix = command.substring(0, location);
            String _command = command.substring(location + 1);
            switch (_prefix.toLowerCase()) {
                case COMMAND_PERFORM_MESSAGE -> player.sendMessage(_command);
                case COMMAND_PERFORM_CONSOLE -> CommandManager.runConsoleCommand(_command, player);
                case COMMAND_PERFORM_PLAYER -> CommandManager.runPlayerCommand(_command, player);
                case COMMAND_PERFORM_CLOSE -> player.getOpenInventory().close();
                default -> CommandManager.runPlayerCommand(command, player);
            }
        }
    }
}
