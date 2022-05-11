package org.hcmc.hcplayground.model.command;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.ItemManager;
import org.hcmc.hcplayground.manager.LocalizationManager;
import org.hcmc.hcplayground.manager.MenuManager;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;

public class CommandItem extends Command {

    public static final String COMMAND_REGISTER = "register";
    public static final String COMMAND_UNREGISTER = "unregister";
    public static final String COMMAND_LOGIN = "login";
    public static final String COMMAND_LOGOUT = "logout";
    public static final String COMMAND_CHANGE_PASSWORD = "changepassword";
    public static final String COMMAND_SEEN = "seen";
    public static final String COMMAND_BAN_PLAYER = "banplayer";
    public static final String COMMAND_PROFILE = "profile";
    public static final String COMMAND_MENU = "menu";
    public static final String COMMAND_RECIPE_BOOK = "recipebook";
    public static final String COMMAND_QUARTERMASTER = "quartermaster";
    public static final String COMMAND_QM_GIVE = "give";
    public static final String COMMAND_QM_HELP = "help";
    public static final String COMMAND_QM_GUI = "gui";
    public static final String COMMAND_HCPLAYGROUND = "hcplayground";
    public static final String COMMAND_HC_RELOAD = "reload";
    public static final String COMMAND_HC_HELP = "help";
    public static final String COMMAND_SCALE = "scale";

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
    private JavaPlugin plugin;

    public CommandItem(String name) {
        super(name);
    }

    /**
     * 向Bukkit核心注册命令
     */
    public void Enroll(@NotNull CommandMap commandMap) {
        if (plugin == null) plugin = HCPlayground.getPlugin();

        setCommandMessage();
        commandMap.register(plugin.getName(), this);
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

        try {
            // 打开主菜单指令 - /menu
            if (commandText.equalsIgnoreCase(COMMAND_MENU)) {
                return RunOpenChestMenuCommand((Player) sender, COMMAND_MENU);
            }
            // 打开玩家档案菜单指令 - /profile
            if (commandText.equalsIgnoreCase(COMMAND_PROFILE)) {
                return RunOpenChestMenuCommand((Player) sender, COMMAND_PROFILE);
            }
            // 打开玩家档案菜单指令 - /profile
            if (commandText.equalsIgnoreCase(COMMAND_RECIPE_BOOK)) {
                return RunOpenChestMenuCommand((Player) sender, COMMAND_RECIPE_BOOK);
            }
            // 军需官指令 - /quatermaster
            if (commandText.equalsIgnoreCase(COMMAND_QUARTERMASTER)) {
                return RunQuartermasterCommand(sender, args);
            }
            // 玩家注册指令 - /register
            if (commandText.equalsIgnoreCase(COMMAND_REGISTER)) {
                return RunRegisterCommand(sender, args);
            }
            // 玩家注销指令 - /unregister
            if (commandText.equalsIgnoreCase(COMMAND_UNREGISTER)) {
                return RunUnRegisterCommand(sender, args);
            }
            // 玩家登录指令 - /login
            if (commandText.equalsIgnoreCase(COMMAND_LOGIN)) {
                return RunLoginCommand(sender, args);
            }
            // 玩家登出指令 - /logout
            if (commandText.equalsIgnoreCase(COMMAND_LOGOUT)) {
                return RunLogoutCommand(sender, args);
            }
            // 玩家修改密码指令 - /changepassword
            if (commandText.equalsIgnoreCase(COMMAND_CHANGE_PASSWORD)) {
                return RunChangePasswordCommand(sender, args);
            }
            // 玩家查看另一个玩家的在线状态和时长
            if (commandText.equalsIgnoreCase(COMMAND_SEEN)) {
                return RunSeenCommand(sender, args);
            }
            // 禁止玩家进入服务器指令 - /banplayer
            if (commandText.equalsIgnoreCase(COMMAND_BAN_PLAYER)) {
                return RunBanPlayerCommand(sender, args);
            }
            // 执行hc指令 - /hcplayground
            if (commandText.equalsIgnoreCase(COMMAND_HCPLAYGROUND)) {
                return RunHCPlaygroundCommand(sender, args);
            }
            if (commandText.equalsIgnoreCase(COMMAND_SCALE)) {
                return RunScaleCommand(sender, args);
            }
        } catch (SQLException | InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException | InvalidKeyException | NoSuchFieldException | IllegalAccessException | IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean RunScaleCommand(CommandSender sender, String[] args) {
        // 当前hc指令需要至少1个参数
        if (args.length <= 1) {
            return ShowCommandHelp(sender, 2);
        }

        Player player = (Player) sender;
        if (!StringUtils.isNumeric(args[0]) || !StringUtils.isNumeric(args[1])) {
            player.sendMessage("§c请输入数字");
            return false;
        }
        int scale = Integer.parseInt(args[0]);
        int maxHealth = Integer.parseInt(args[1]);
        if (scale <= 0 || maxHealth <= 0) {
            player.sendMessage("§c不要乱来！");
            return false;
        }

        player.setHealthScale(scale);
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance == null) return false;
        instance.setBaseValue(maxHealth);

        return true;
    }

    // 执行hcplayground指令
    private boolean RunHCPlaygroundCommand(CommandSender sender, String[] args) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFieldException, IllegalAccessException, IOException {
        // 当前hc指令需要至少1个参数
        if (args.length <= 0) {
            return ShowCommandHelp(sender, 0);
        }
        // /hc reload
        if (args[0].equalsIgnoreCase(COMMAND_HC_RELOAD)) {
            return RunHCReloadCommand(sender);
        }

        // /hc help
        if (args[0].equalsIgnoreCase(COMMAND_HC_HELP)) {
            return ShowCommandHelp(sender, 0);
        }

        return false;
    }

    private boolean RunQuartermasterCommand(CommandSender sender, String[] args) {
        int amount = 1;

        if (args.length <= 0) {
            return ShowCommandHelp(sender, 1);
        }

        if (args.length >= 4 && args[3] != null && Global.patternNumber.matcher(args[3]).matches()) {
            amount = Integer.parseInt(args[3]);
        }

        if (args[0].equalsIgnoreCase(COMMAND_QM_HELP)) {
            return ShowCommandHelp(sender, 0);
        }

        if (args[0].equalsIgnoreCase(COMMAND_QM_GIVE)) {
            return RunQMGiveCommand(sender, args, amount);
        }

        if (args[0].equalsIgnoreCase(COMMAND_QM_GUI)) {
            return RunQMGuiCommand((Player) sender, args);
        }

        return false;
    }

    private boolean RunOpenChestMenuCommand(Player player, String menuId) {
        Inventory inv = MenuManager.CreateMenu(menuId, player);
        if (inv == null) return false;
        player.openInventory(inv);

        return true;
    }


    private boolean RunHCReloadCommand(CommandSender sender) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFieldException, IllegalAccessException, IOException {
        long a = new Date().getTime();
        HCPlayground.getInstance().ReloadConfiguration();

        long b = new Date().getTime();
        long c = b - a;
        sender.sendMessage(LocalizationManager.getMessage("reload", sender).replace("%time_escaped%", String.valueOf(c)));

        return true;
    }

    private boolean RunSeenCommand(CommandSender sender, String[] args) {
        if (args.length <= 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        Date lastLogin = new Date();
        boolean foundPlayer = false;
        String status = "";

        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        for (OfflinePlayer o : offlinePlayers) {
            if (!Objects.requireNonNull(o.getName()).equalsIgnoreCase(args[0])) continue;

            status = LocalizationManager.getMessage(String.format("onlineStatusMessage.%s", o.isOnline()), o.getPlayer());
            lastLogin.setTime(o.getLastPlayed());
            foundPlayer = true;
            break;
        }

        if (!foundPlayer) {
            sender.sendMessage(LocalizationManager.getMessage("playerNotExist", sender).replace("%player%", args[0]));
        } else {
            String message = LocalizationManager.getMessage("playerLastLoginTime", sender);
            String loginFormat = Global.getDateFormat(lastLogin, DateFormat.FULL, Locale.CHINA);
            sender.sendMessage(message.replace("%onlineStatusMessage%", status).replace("%player%", args[0]).replace("%logintime%", loginFormat));
        }
        return true;
    }

    private boolean RunUnRegisterCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少1个参数
        if (args.length <= 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Unregister(args[0]);
    }

    private boolean RunLoginCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少1个参数
        if (args.length <= 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Login(args[0]);
    }

    private boolean RunLogoutCommand(CommandSender sender, String[] args) {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }

        player.kickPlayer(LocalizationManager.getMessage("playerLogout", sender).replace("%player%", player.getName()));
        return true;
    }

    private boolean RunRegisterCommand(CommandSender sender, String[] args) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少2个参数
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // 检查2个参数(密码)是否一样，不区分大小写
        if (!args[0].equalsIgnoreCase(args[1])) {
            sender.sendMessage(LocalizationManager.getMessage("playerPasswordNotMatch", sender));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Register(args[0]);
    }

    private boolean RunChangePasswordCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少3个参数
        if (args.length <= 2) {
            ShowCommandHelp(sender, 3);
            return false;
        }
        if (!args[1].equalsIgnoreCase(args[2])) {
            sender.sendMessage(LocalizationManager.getMessage("playerPasswordNotMatch", sender));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.ChangePassword(args[0], args[1]);
    }

    private boolean RunBanPlayerCommand(CommandSender sender, String[] args) throws SQLException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少2个参数
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }

        StringBuilder reason = new StringBuilder();
        for (String s : args) {
            if (Arrays.asList(args).indexOf(s) == 0) continue;
            reason.append(s).append(" ");
        }
        PlayerData playerData = PlayerManager.getPlayerData(player);
        playerData.BanPlayer(args[0], reason.toString().trim());

        return true;
    }

    private boolean RunQMGuiCommand(Player player, String[] args) {
        // TODO: 需要实施/quartermaster gui指令
        player.sendMessage(LocalizationManager.getMessage("UnderConstruction", player));
        return true;
    }

    private boolean RunQMGiveCommand(CommandSender sender, String[] args, int amount) {
        if (args.length <= 2) {
            ShowCommandHelp(sender, 3);
            return false;
        }

        ItemManager.Give(sender, args[1], args[2], amount);
        return true;
    }

    private boolean ValidateCommand(CommandSender sender, String[] args) {
        // 如果指令被定义为不能在控制台执行
        // 并且使用了控制台发送当前指令，验证失败
        if (!this.isConsole && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(LocalizationManager.getMessage("console-message", sender).replace("%command%", id));
            return false;
        }
        // 如果指令被定义为不能通过玩家执行
        // 并且通过了玩家发送当前指令，验证失败
        if (!this.isPlayer && sender instanceof Player) {
            sender.sendMessage(LocalizationManager.getMessage("player-message", sender).replace("%command%", id));
            return false;
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
                sender.sendMessage(LocalizationManager.getMessage("world-message", sender).replace("%command%", id).replace("%world%", worldName));
                return false;
            }
            // 验证指令的参数的权限
            for (String s : args) {
                // 定义的参数个数为0，表示当前指令不需要参数验证，即使当前指令需要带有参数
                if (this.args.size() == 0) continue;
                // 获取每个参数实例在指令参数列表的位置
                CommandArgument arg = this.args.stream().filter(x -> x.name.equalsIgnoreCase(s)).findAny().orElse(null);
                // 如果当前指令参数不在参数集合内，则指令语法错误，提示/<command> usage
                if (arg == null) {
                    sender.sendMessage(LocalizationManager.getMessage(String.format("%s.usage", id), sender));
                    return false;
                }
                // 如果当前指令参数不在参数列表的位置定义，则指令语法错误，提示/<command> usage
                if (arg.index - 1 != Arrays.asList(args).indexOf(s)) {
                    sender.sendMessage(LocalizationManager.getMessage(String.format("%s.usage", id), sender));
                    return false;
                }
                // 如当玩家不拥有前指令参数的权限，提示permission-message
                if (!player.hasPermission(arg.permission) && !arg.permission.equalsIgnoreCase("")) {
                    //sender.sendMessage(this.permissionMessage);
                    sender.sendMessage(LocalizationManager.getMessage("permission-message", sender).replace("%permission%", arg.permission));
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

    private boolean ShowCommandHelp(CommandSender sender, int argLength) {
        if (argLength >= 1) {
            sender.sendMessage(LocalizationManager.getMessage("parameterInCorrect", sender).replace("%length%", String.valueOf(argLength)));
        }
        sender.sendMessage(getDescription());
        sender.sendMessage(getUsage().replace("<command>", getName()));

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
        this.setPermissionMessage(LocalizationManager.getMessage("permission-message", null).replace("%permission%", permission));
        this.setUsage(LocalizationManager.getMessage(String.format("%s.usage", id), null));
        this.setDescription(LocalizationManager.getMessage(String.format("%s.description", id), null));
    }
}
