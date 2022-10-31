package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.hcmc.hcplayground.manager.CommandManager;
import org.hcmc.hcplayground.utility.MaterialData;
import org.hcmc.hcplayground.utility.PlayerHeader;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 箱子界面插槽模板类<br>
 * 所有定义的插槽号{@code slots}显示相同的物品材质、数量、说明，包括显示相同的player-head<br>
 * 但不同的插槽号可以执行不同的指令组{@code leftCommands}和{@code rightCommands}<br>
 */
public class MenuPanelSlot implements Cloneable {

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
    /**
     * 不同的插槽号可以执行不同的指令组<br>
     * Integer - 插槽号，由1开始，最大值54<br>
     * {@code List<String>} - 左键点击的指令组
     */
    @Expose
    @SerializedName(value = "left_click")
    private Map<Integer, List<String>> leftCommands = new HashMap<>();
    /**
     * 不同的插槽号可以执行不同的指令组<br>
     * Integer - 插槽号，由1开始，最大值54<br>
     * {@code List<String>} - 右键点击的指令组
     */@Expose
    @SerializedName(value = "right_click")
    private Map<Integer, List<String>> rightCommands = new HashMap<>();
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

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    public void setLore(List<String> lore) {
        this.lore = new ArrayList<>(lore);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public void setSlots(List<Integer> slots) {
        this.slots =new ArrayList<>(slots);
    }

    public List<ItemFlag> getFlags() {
        return flags;
    }

    public void setFlags(List<ItemFlag> flags) {
        this.flags = flags;
    }

    public Map<Integer, List<String>> getLeftCommands() {
        return new HashMap<>(leftCommands);
    }

    public void setLeftCommands(Map<Integer, List<String>> leftCommands) {
        this.leftCommands = new HashMap<>(leftCommands);
    }

    public void addLeftCommands(Integer slotIndex, String... commands) {
        if (!slots.contains(slotIndex)) return;
        List<String> _commands = leftCommands.containsKey(slotIndex) ? leftCommands.get(slotIndex) : new ArrayList<>();
        leftCommands.put(slotIndex, _commands);

        for (String s : commands) {
            if (_commands.contains(s)) continue;
            _commands.add(s);
        }
    }

    public Map<Integer, List<String>> getRightCommands() {
        return new HashMap<>(rightCommands);
    }

    public void setRightCommands(Map<Integer, List<String>> rightCommands) {
        this.rightCommands = new HashMap<>(rightCommands);
    }

    public void addRightCommands(Integer slotIndex, String... commands) {
        if (!slots.contains(slotIndex)) return;
        List<String> _commands = rightCommands.containsKey(slotIndex) ? rightCommands.get(slotIndex) : new ArrayList<>();
        rightCommands.put(slotIndex, _commands);

        for (String s : commands) {
            if (_commands.contains(s)) continue;
            _commands.add(s);
        }
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

    public void setCustomSkull(String customSkull) {
        this.customSkull = customSkull;
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
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

    /**
     * 执行左键点击箱子界面插槽物品的指令
     * @param player 点击箱子界面的玩家实例
     * @param slotIndex 点击箱子界面所在的插槽号，由1开始，最大值54
     */
    public void runLeftClickCommands(Player player, int slotIndex) {
        List<String> commands = leftCommands.getOrDefault(slotIndex, new ArrayList<>());
        runCommands(player, commands, leftConditions, leftDenyMessage);
    }

    /**
     * 执行右键点击箱子界面插槽物品的指令
     * @param player 点击箱子界面的玩家实例
     * @param slotIndex 点击箱子界面所在的插槽号，由1开始，最大值54
     */
    public void runRightClickCommands(Player player, int slotIndex) {
        List<String> commands = rightCommands.getOrDefault(slotIndex, new ArrayList<>());
        runCommands(player, commands, rightConditions, rightDenyMessage);
    }

    public Map<Integer, ItemStack> createItemStacks(Player player) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();

        for (int index : slots) {
            ItemStack is;
            if (material == null) material = new MaterialData(Material.AIR, "");
            if (material.value == null) material = new MaterialData(Material.STONE, "");
            if (!material.value.equals(Material.PLAYER_HEAD)) {
                is = new ItemStack(material.value, amount);
            } else {
                is = StringUtils.isBlank(customSkull) ? ResolvePlayerHead(player, material) : ResolveCustomHead(customSkull);
            }

            ItemMeta im = is.getItemMeta();
            if (im == null) continue;

            im.addItemFlags(flags.toArray(new ItemFlag[0]));
            im.setLore(lore);
            if (!StringUtils.isBlank(display))
                im.setDisplayName(display.replace("%player%", player.getName()));

            if (glowing) {
                im.addEnchant(Enchantment.MENDING, 1, true);
                Set<ItemFlag> flags = im.getItemFlags();
                if (!flags.contains(ItemFlag.HIDE_ENCHANTS)) {
                    im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }
            }

            is.setItemMeta(im);
            itemStacks.put(index, is);
        }

        return itemStacks;
    }

    @Override
    public Object clone() {
        try {
            MenuPanelSlot object = (MenuPanelSlot) super.clone();
            object.setLeftConditions(this.getLeftConditions());
            object.setRightConditions(this.getRightConditions());
            return object;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
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

    private @NotNull ItemStack ResolvePlayerHead(@NotNull Player player, @NotNull MaterialData data) {
        ItemStack is = new ItemStack(data.value, 1);
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player.getUniqueId());
        if (!StringUtils.equals("head_%player%", data.name.toLowerCase())) return is;

        SkullMeta sm = (SkullMeta) is.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(offlinePlayer);
            is.setItemMeta(sm);
        }
        return is;
    }

    private @NotNull ItemStack ResolveCustomHead(String base64data) {
        ItemStack is = new ItemStack(Material.PLAYER_HEAD, 1);
        ItemMeta im = PlayerHeader.setTextures(is, base64data);
        if (!(im instanceof SkullMeta meta)) return is;

        is.setItemMeta(meta);
        return is;
    }
}
