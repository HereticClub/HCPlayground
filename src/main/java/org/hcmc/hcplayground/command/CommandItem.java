package org.hcmc.hcplayground.command;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.itemManager.ItemManager;
import org.hcmc.hcplayground.localization.Localization;
import org.hcmc.hcplayground.model.Global;
import org.hcmc.hcplayground.template.TemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandItem extends Command {
    /**
     * 当前指令的使用权限，设置为null或者空字符串，表示当前命令不需要权限
     */
    @Expose
    @SerializedName(value = "permission")
    public String permission = "";
    /**
     * 当前指令的是否可以让玩家执行
     */
    @Expose
    @SerializedName(value = "runOnPlayer")
    public boolean isPlayer = true;
    /**
     * 当前指令的是否可以使用控制台执行
     */
    @Expose
    @SerializedName(value = "runOnConsole")
    public boolean isConsole = false;
    /**
     * 当前指令的可用世界列表，不设置该属性则当前指令可以在任何世界使用
     */
    @Expose
    @SerializedName(value = "worlds")
    public List<String> worlds = new ArrayList<>();
    /**
     * 当前指令的别名，或者简短指令
     */
    @Expose
    @SerializedName(value = "aliases")
    public List<String> aliases = new ArrayList<>();
    /**
     * 当前指令的参数列表，用于在聊天栏内自动输入
     */
    @Expose
    @SerializedName(value = "args")
    public List<CommandArgument> args = new ArrayList<>();
    /**
     * 当前指令的ID，也是命令的名称，值必须唯一
     */
    @Expose(serialize = false, deserialize = false)
    public String id;

    @Expose(serialize = false, deserialize = false)
    private JavaPlugin plugin = HCPlayground.getPlugin();

    public CommandItem(String name) {
        super(name);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> tabs = new ArrayList<>();
        List<String> org = super.tabComplete(sender, alias, args);
        // 检测当前命令如果不是由玩家在聊天栏输入，忽略TabComplete列表设置
        if (!(sender instanceof Player player)) return org;
        // 获取玩家在聊天栏输入指令时的当前参数的位置
        int index = args.length;
        // 根据已输入参数个数获取当前指令的参数列表
        List<CommandArgument> ca = this.args.stream().filter(x -> x.index == index).toList();
        // 循环每个参数，设置该参数的使用权限
        for (CommandArgument c : ca) {
            // 如果当前设置的参数没有设置权限，直接添加该参数到TabComplete列表设置
            if (c.permission.equalsIgnoreCase("")) {
                tabs.add(c.name);
                continue;
            }

            String permission = c.permission;
            // 第一个参数时不检测参数权限的占位符
            if (index >= 2) {
                // 获取当前参数的上一个参数的名称
                String arg = args[index - 2];
                // 取代%parent%占位符为上一个参数的名称
                permission = c.permission.replace("%parent%", arg);
                // 如果玩家输入当前参数不在相应位置的参数列表中
                // 忽略本次循环，即不将该参数添加到TabComplete列表
                if (!c.parent.contains(arg)) continue;
            }
            // 如果玩家没有该参数的使用权限，将不会把当前位置的参数添加到TabComplete列表
            if (!player.hasPermission(permission) && !player.isOp()) continue;
            tabs.add(c.name);
        }
        return tabs.size() == 0 ? org : tabs;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        String commandText = getName();
        if (!ValidateCommand(sender, args)) return false;

        if (commandText.equalsIgnoreCase("menu")) RunMenuCommand((Player) sender);
        if (commandText.equalsIgnoreCase("quartermaster")) RunQuartermasterCommand(sender, args);

        return false;
    }

    public void Enroll(@NotNull CommandMap commandMap) {
        setCommandMessage();
        commandMap.register(id, this);
    }

    private void RunMenuCommand(Player player) {
        Inventory inv = TemplateManager.CreateInventory("Template1", null);
        if (inv == null) return;
        player.openInventory(inv);
    }

    private void RunQuartermasterCommand(CommandSender sender, String[] args) {
        int amount = 1;
        CommandArgument ca;

        if (args.length <= 2) {
            sender.sendMessage(Localization.Messages.get("parameterInCorrect"));
            return;
        }

        if (args.length >= 4 && args[3] != null && Global.patternNumber.matcher(args[3]).matches()) {
            amount = Integer.parseInt(args[3]);
        }

        if (args[0].equalsIgnoreCase("give")) ItemManager.Give(sender, args[1], args[2], amount);
    }

    private boolean ValidateCommand(CommandSender sender, String[] args) {
        // 如果指令被定义为不能在控制台执行
        // 并且使用了控制台发送当前指令，验证失败
        if (!this.isConsole && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(Localization.Messages.get("console-message").replace("%command%", id));
            return false;
        }
        // 如果指令被定义为不能通过玩家执行
        // 并且通过了玩家发送当前指令，验证失败
        if (!this.isPlayer && sender instanceof Player) {
            sender.sendMessage(Localization.Messages.get("player-message").replace("%command%", id));
            return false;
        }
        // 验证参数名称
        for (String s : args) {
            int index = Arrays.asList(args).indexOf(s);
            List<CommandArgument> keys = this.args.stream().filter(x -> x.index == index + 1).toList();
            if (keys.size() <= 0) continue;
            if (keys.stream().noneMatch(x -> x.name.equalsIgnoreCase(s))) return false;
        }
        // 如果指令时可以通过控制台发送，则不验证权限
        // 反之如果通过玩家发送当前指令，需要验证权限
        if (sender instanceof Player player) {
            // 如果玩家是op，忽略任何权限限制
            if (player.isOp()) return true;
            // 验证指令是否在可执行的世界
            World world = player.getWorld();
            String worldName = world.getName();
            if (!worlds.contains(worldName) && worlds.size() != 0) {
                sender.sendMessage(Localization.Messages.get("world-message").replace("%command%", id).replace("%world%", worldName));
                return false;
            }
            // 验证指令的参数的权限
            for (String s : args) {
                // 获取每个参数实例在指令参数列表的位置
                CommandArgument arg = this.args.stream().filter(x -> x.name.equalsIgnoreCase(s)).findAny().orElse(null);
                // 如果当前指令参数不在参数集合内，则指令语法错误，提示/<command> usage
                if (arg == null) {
                    sender.sendMessage(Localization.Messages.get(String.format("%s.usage", id)));
                    return false;
                }
                // 如果当前指令参数不在参数列表的位置定义，则指令语法错误，提示/<command> usage
                if (arg.index - 1 != Arrays.asList(args).indexOf(s)) {
                    sender.sendMessage(Localization.Messages.get(String.format("%s.usage", id)));
                    return false;
                }
                // 如当玩家不拥有前指令参数的权限，提示permission-message
                if (!player.hasPermission(arg.permission) && !arg.permission.equalsIgnoreCase("")) {
                    //sender.sendMessage(this.permissionMessage);
                    sender.sendMessage(Localization.Messages.get("permission-message").replace("%permission%", arg.permission));
                    return false;
                }
            }
            /*
             测试当前用户是否拥有执行当前指令的权限
             如果没有，则显示permission-message，并且返回False
            */
            return this.testPermission(sender);
        }
        // 验证通过
        return true;
    }

    private void setCommandMessage() {
        // 以下两个属性必须设置为指令的名称
        this.setLabel(this.id);
        this.setName(this.id);
        // 以下所有属性值都不能为null
        if (aliases == null) aliases = new ArrayList<>();
        if (args == null) args = new ArrayList<>();
        if (permission == null) permission = "";
        if (worlds == null) worlds = new ArrayList<>();

        if (!this.permission.equalsIgnoreCase("")) this.setPermission(this.permission);
        this.setAliases(this.aliases);
        this.setPermissionMessage(Localization.Messages.get("permission-message").replace("%permission%", permission));
        this.setUsage(Localization.Messages.get(String.format("%s.usage", id)));
        this.setDescription(Localization.Messages.get(String.format("%s.description", id)));
    }
}
