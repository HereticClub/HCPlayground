package org.hcmc.hcplayground.model.command;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.menu.*;
import org.hcmc.hcplayground.model.parkour.CourseInfo;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.serialization.MaterialSerialization;
import org.hcmc.hcplayground.serialization.SkillTypeSerialization;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RomanNumber;
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
import java.util.regex.Pattern;

public class CommandItem extends Command {

    private final static Pattern patternNumber = Pattern.compile("-?\\d+(\\.\\d+)?");
    public static final String COMMAND_SKILL_MANAGER = "skillmanager";
    public static final String COMMAND_SKILL_MANAGER_INCREASE = "increase";
    public static final String COMMAND_SKILL_MANAGER_DECREASE = "decrease";
    public static final String COMMAND_REGISTER = "register";
    public static final String COMMAND_UNREGISTER = "unregister";
    public static final String COMMAND_LOGIN = "login";
    public static final String COMMAND_LOGOUT = "logout";
    public static final String COMMAND_CHANGE_PASSWORD = "changepassword";
    public static final String COMMAND_SEEN = "seen";
    public static final String COMMAND_BAN_PLAYER = "banplayer";
    public static final String COMMAND_UNBAN_PLAYER = "unbanplayer";
    public static final String COMMAND_PLAYGROUND_MENUS = "playgroundmenus";
    public static final String COMMAND_PLAYGROUND_MENUS_OPEN = "open";
    public static final String COMMAND_PLAYGROUND_MENUS_CLOSE = "close";
    public static final String COMMAND_RECIPE_MANAGER = "recipemanager";
    public static final String COMMAND_RECIPE_MANAGER_GIVE = "give";
    public static final String COMMAND_RECIPE_MANAGER_REMOVE = "remove";
    public static final String COMMAND_REWARD_MANAGER = "rewardmanager";
    public static final String COMMAND_REWARD_MANAGER_CLAIM = "claim";
    public static final String COMMAND_REWARD_MANAGER_RESET = "reset";
    public static final String COMMAND_QUARTERMASTER = "quartermaster";
    public static final String COMMAND_QM_GIVE = "give";
    public static final String COMMAND_QM_HELP = "help";
    public static final String COMMAND_QM_GUI = "gui";
    public static final String COMMAND_HCPLAYGROUND = "hcplayground";
    public static final String COMMAND_HC_RELOAD = "reload";
    public static final String COMMAND_HC_HELP = "help";
    public static final String COMMAND_RECORD_MANAGER = "recordmanager";
    public static final String COMMAND_RECORD_MANAGER_LOAD = "load";
    public static final String COMMAND_RECORD_MANAGER_SAVE = "save";
    public static final String COMMAND_SCALE = "scale";
    public static final String COMMAND_CRAZY = "crazy";
    public static final String COMMAND_CRAZY_CRAFTING = "crafting";
    public static final String COMMAND_CRAZY_ENCHANTING = "enchanting";
    public static final String COMMAND_CRAZY_ANVIL = "anvil";
    public static final String COMMAND_BROADCAST_PLUS = "broadcastplus";
    public static final String COMMAND_COURSE = "course";
    public static final String COMMAND_COURSE_CREATE = "create";
    public static final String COMMAND_COURSE_MODIFY = "modify";
    public static final String COMMAND_COURSE_DELETE = "delete";
    public static final String COMMAND_COURSE_LEAVE = "leave";
    public static final String COMMAND_COURSE_LIST = "list";
    public static final String COMMAND_COURSE_ABANDONS = "abandons";
    public static final String COMMAND_COURSE_CLAIM = "claim";
    public static final String COMMAND_COURSE_START_POINT = "startpoint";
    public static final String COMMAND_COURSE_CHECKPOINT = "checkpoint";
    public static final String COMMAND_COURSE_READY = "ready";
    public static final String COMMAND_COURSE_DISPLAY = "display";
    public static final String COMMAND_COURSE_CHECKPOINT_REMOVE = "remove";
    public static final String COMMAND_COURSE_TELEPORT = "tp";
    public static final String COMMAND_COURSE_PARKOUR_KIT = "parkourkit";
    public static final String COMMAND_COURSE_LINK_LOBBY = "linklobby";
    public static final String COMMAND_COURSE_HELP = "help";
    public static final String COMMAND_MINION = "minion";
    public static final String COMMAND_MINION_GIVE = "give";


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
    @Expose(serialize = false, deserialize = false)
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
        if (plugin == null) plugin = HCPlayground.getInstance();

        setCommandMessage();
        commandMap.register(plugin.getName(), this);
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        List<String> tabs;
        // 获取系统返回的tabComplete
        List<String> org = super.tabComplete(sender, alias, args);
        int index = args.length;
        // 检测当前命令如果不是由玩家在聊天栏输入，忽略TabComplete列表设置
        if (!(sender instanceof Player player)) return org;
        tabs = getDeclaredArguments(player, args);
        PlayerData data = PlayerManager.getPlayerData(player);

        try {
            // 获取指令字符串
            if (index == 2 && getName().equalsIgnoreCase(COMMAND_REWARD_MANAGER)) {
                if (args[0].equalsIgnoreCase(COMMAND_REWARD_MANAGER_CLAIM)) {
                    tabs = new ArrayList<>(MMOManager.getSkillRewardNames());
                    tabs.addAll(MMOManager.getCollectionRewardNames());
                }
                if (args[0].equalsIgnoreCase(COMMAND_REWARD_MANAGER_RESET)) {
                    tabs = new ArrayList<>(Collections.singleton("*"));
                    tabs.addAll(MMOManager.getSkillRewardNames());
                    tabs.addAll(MMOManager.getCollectionRewardNames());
                }
            }
            if (index == 2 && getName().equalsIgnoreCase(COMMAND_RECIPE_MANAGER)) {
                if (args[0].equalsIgnoreCase(COMMAND_RECIPE_MANAGER_REMOVE)) {
                    tabs = new ArrayList<>(Collections.singleton("*"));
                    tabs.addAll(data.getRecipes());
                }
                if (args[0].equalsIgnoreCase(COMMAND_RECIPE_MANAGER_GIVE)) {
                    tabs = new ArrayList<>(Collections.singleton("*"));
                    tabs.addAll(RecipeManager.getIdList());
                }
            }
            if (index == 2 && getName().equalsIgnoreCase(COMMAND_COURSE)) {
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_MODIFY)) {
                    tabs = getCourseModifyList(player);
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_DELETE)) {
                    tabs = getCourseModifyList(player);
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_CLAIM)) {
                    tabs = getCourseAbandonList();
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_TELEPORT)) {
                    tabs = CourseManager.getIdList();
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_READY)) {
                    tabs = getTrueFalseAsList();
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_DISPLAY)) {
                    tabs = getCourseModifyList(player);
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_CHECKPOINT)) {
                    tabs = getCheckpointList(player);
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_PARKOUR_KIT)) {
                    tabs = ParkourApiManager.getParkourKitNameList();
                }
                if (args[0].equalsIgnoreCase(COMMAND_COURSE_LINK_LOBBY)) {
                    tabs = ParkourApiManager.getLobbyNames();
                }
            }
            if (index == 2 && getName().equalsIgnoreCase(COMMAND_SKILL_MANAGER)) {
                tabs = MMOManager.getSkillRewardNames();
            }
            if (index == 2 && getName().equalsIgnoreCase(COMMAND_PLAYGROUND_MENUS)) {
                if (args[0].equalsIgnoreCase(COMMAND_PLAYGROUND_MENUS_OPEN)) {
                    tabs = MenuManager.getIdList();
                }
                if (args[0].equalsIgnoreCase(COMMAND_PLAYGROUND_MENUS_CLOSE)) {
                    tabs = MenuManager.getIdList();
                }
            }
            if (index == 3 && getName().equalsIgnoreCase(COMMAND_MINION)) {
                if (args[0].equalsIgnoreCase(COMMAND_MINION_GIVE)) {
                    tabs = getMinionTypeList();
                }
            }
            if (index == 4 && getName().equalsIgnoreCase(COMMAND_MINION)) {
                if (args[0].equalsIgnoreCase(COMMAND_MINION_GIVE)) {
                    MinionType _type = MinionManager.getMinionType(args[2]);

                    tabs = _type == null ? org : getMinionLevelList(_type);
                }
            }
            if (index == 3 && getName().equalsIgnoreCase(COMMAND_QUARTERMASTER)) {
                if (args[0].equalsIgnoreCase(COMMAND_QM_GIVE)) {
                    tabs = ItemManager.getIdList();
                }
            }
            if (index == 4 && getName().equalsIgnoreCase(COMMAND_QUARTERMASTER)) {
                if (args[0].equalsIgnoreCase(COMMAND_QM_GIVE)) {
                    tabs = getRegulaNumberList();
                }
            }
            if (index == 1 && getName().equalsIgnoreCase(COMMAND_BROADCAST_PLUS)) {
                tabs = BroadcastManager.Messages;
            }
        } catch (IOException | IllegalAccessException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        return tabs.size() == 0 ? org : tabs;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        String commandText = getName();
        if (!ValidateCommand(sender, args)) return false;

        try {
            // 打开主菜单指令 - /playgroundmenus
            if (commandText.equalsIgnoreCase(COMMAND_PLAYGROUND_MENUS)) {
                return RunPlaygroundMenusCommand(sender, args);
            }
            // 奖励指令 /rewardmanager
            if (commandText.equalsIgnoreCase(COMMAND_REWARD_MANAGER)) {
                return RunRewardManagerCommand(sender, args);
            }
            if (commandText.equalsIgnoreCase(COMMAND_RECIPE_MANAGER)) {
                return RunRecipeManagerCommand(sender, args);
            }
            if (commandText.equalsIgnoreCase(COMMAND_SKILL_MANAGER)) {
                return RunSkillManagerCommand(sender, args);
            }
            // 军需官指令 - /quartermaster
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
                return RunLogoutCommand(sender);
            }
            // 玩家修改密码指令 - /changepassword
            if (commandText.equalsIgnoreCase(COMMAND_CHANGE_PASSWORD)) {
                return RunChangePasswordCommand(sender, args);
            }
            // 玩家查看另一个玩家的在线状态和时长 /seen
            if (commandText.equalsIgnoreCase(COMMAND_SEEN)) {
                return RunSeenCommand(sender, args);
            }
            // 禁止玩家进入服务器指令 - /banplayer
            if (commandText.equalsIgnoreCase(COMMAND_BAN_PLAYER)) {
                return RunBanPlayerCommand(sender, args);
            }
            // 解禁玩家指令 /unbanplayer
            if (commandText.equalsIgnoreCase(COMMAND_UNBAN_PLAYER)) {
                return RunUnBanPlayerCommand(sender, args);
            }
            // 执行hc指令 - /hcplayground
            if (commandText.equalsIgnoreCase(COMMAND_HCPLAYGROUND)) {
                return RunHCPlaygroundCommand(sender, args);
            }
            // 执行scale指令 /scale
            if (commandText.equalsIgnoreCase(COMMAND_SCALE)) {
                return RunScaleCommand(sender, args);
            }
            // 执行course指令 /course
            if (commandText.equalsIgnoreCase(COMMAND_COURSE)) {
                return RunCourseCommand(sender, args);
            }
            // /crazy
            if (commandText.equalsIgnoreCase((COMMAND_CRAZY))) {
                return RunCrazyCommand(sender, args);
            }
            // 执行broadcastplus指令 /broadcastplus /bp
            if (commandText.equalsIgnoreCase(COMMAND_BROADCAST_PLUS)) {
                return RunBroadcastPlusCommand(sender, args);
            }
            // 执行minion指令 /minion
            if (commandText.equalsIgnoreCase(COMMAND_MINION)) {
                return RunMinionCommand(sender, args);
            }
            if (commandText.equalsIgnoreCase(COMMAND_RECORD_MANAGER)) {
                return RunRecordManagerCommand(sender, args);
            }
        } catch (SQLException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                 IllegalBlockSizeException | NoSuchAlgorithmException | InvalidKeySpecException | BadPaddingException |
                 InvalidKeyException | NoSuchFieldException | IllegalAccessException | IOException |
                 InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private boolean RunRecipeManagerCommand(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return ShowCommandHelp(sender, 3);
        }

        if (args[0].equalsIgnoreCase(COMMAND_RECIPE_MANAGER_GIVE)) {
            return RunRecipeManagerGiveCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase(COMMAND_RECIPE_MANAGER_REMOVE)) {
            return RunRecipeManagerRemoveCommand(sender, args);
        }

        return false;
    }

    private boolean RunRecipeManagerGiveCommand(CommandSender sender, String[] args) {
        String playerName = args[2];
        String recipeId = args[1];
        Player target = Bukkit.getPlayer(playerName);
        List<String> definedRecipes = RecipeManager.getIdList();

        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", playerName));
            return false;
        }
        if (definedRecipes.stream().noneMatch(x -> x.equalsIgnoreCase(recipeId)) && !recipeId.equalsIgnoreCase("*")) {
            sender.sendMessage(LanguageManager.getString("recipeNotExist").replace("%recipe%", recipeId));
            return false;
        }

        PlayerData targetData = PlayerManager.getPlayerData(target);
        List<String> givenRecipes = new ArrayList<>(recipeId.equalsIgnoreCase("*") ? definedRecipes : Collections.singleton(recipeId));

        for (String recipe : givenRecipes) {
            if (!targetData.unlockRecipe(recipe)) continue;
            sender.sendMessage(LanguageManager.getString("recipeGivenToPlayer").replace("%recipe%", recipe).replace("%player%", playerName));
            target.sendMessage(LanguageManager.getString("recipePlayerReceived").replace("%recipe%", recipe));
        }
        return true;
    }

    private boolean RunRecipeManagerRemoveCommand(CommandSender sender, String[] args) {
        String playerName = args[2];
        String recipeId = args[1];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", playerName));
            return false;
        }

        PlayerData targetData = PlayerManager.getPlayerData(target);
        List<String> targetRecipes = targetData.getRecipes();
        List<String> removedRecipes = new ArrayList<>(recipeId.equalsIgnoreCase("*") ? targetRecipes : Collections.singleton(recipeId));

        if (!recipeId.equalsIgnoreCase("*") && targetRecipes.stream().noneMatch(x -> x.equalsIgnoreCase(recipeId))) {
            sender.sendMessage(LanguageManager.getString("recipeNotExist").replace("%recipe%", recipeId));
            return false;
        }

        for (String recipe : removedRecipes) {
            if (targetData.removeRecipe(recipe)) {
                sender.sendMessage(LanguageManager.getString("recipeRemoveFromPlayer").replace("%recipe%", recipe).replace("%player%", playerName));
                target.sendMessage(LanguageManager.getString("recipePlayerRemoved").replace("%recipe%", recipe));
            }
        }

        return true;
    }

    private boolean RunRewardManagerCommand(CommandSender sender, String[] args) {
        if (args.length <= 2) {
            return ShowCommandHelp(sender, 3);
        }

        if (args[0].equalsIgnoreCase(COMMAND_REWARD_MANAGER_CLAIM)) {
            return RunRewardManagerClaimCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase(COMMAND_REWARD_MANAGER_RESET)) {
            return RunRewardManagerResetCommand(sender, args);
        }

        return false;
    }

    private boolean RunRewardManagerResetCommand(CommandSender sender, String[] args) {
        // args[1] - Skill Type
        // args[2] - Player Name
        Player target = Bukkit.getPlayer(args[2]);
        SkillMenuPanel.SkillType type = SkillTypeSerialization.valueOf(args[1]);
        Material material = MaterialSerialization.valueOf(args[1]);
        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", args[2]));
            return false;
        }

        if (args[1].equalsIgnoreCase("*")) {
            SkillMenuPanel.SkillType[] types = SkillMenuPanel.SkillType.values();
            Material[] materials = Material.values();
            for (SkillMenuPanel.SkillType t : types) {
                RewardManager.reset(t, target);
            }
            for (Material m : materials) {
                RewardManager.reset(m, target);
            }

            target.sendMessage(LanguageManager.getString("rewardLevelResetAll"));
            return true;
        }

        if (type.equals(SkillMenuPanel.SkillType.UNDEFINED) && material.equals(Material.AIR)) {
            sender.sendMessage(LanguageManager.getString("rewardTypeInvalid").replace("%reward%", args[1]));
            return false;
        }

        if (!type.equals(SkillMenuPanel.SkillType.UNDEFINED)) RewardManager.reset(type, target);
        if (!material.equals(Material.AIR)) RewardManager.reset(material, target);

        target.sendMessage(LanguageManager.getString("rewardLevelReset").replace("%skill%", type.name()));
        return true;
    }

    private boolean RunRewardManagerClaimCommand(CommandSender sender, String[] args) {
        // args[1] - Skill Type or material
        // args[2] - Player Name
        Player target = Bukkit.getPlayer(args[2]);
        SkillMenuPanel.SkillType type = SkillTypeSerialization.valueOf(args[1]);
        Material material = MaterialSerialization.valueOf(args[1]);
        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", args[2]));
            return false;
        }
        if (type.equals(SkillMenuPanel.SkillType.UNDEFINED) && material.equals(Material.AIR)) {
            sender.sendMessage(LanguageManager.getString("rewardTypeInvalid").replace("%reward%", args[1]));
            return false;
        }

        if (!material.equals(Material.AIR)) RewardManager.claim(target, material);
        if (!type.equals(SkillMenuPanel.SkillType.UNDEFINED)) RewardManager.claim(target, type);

        return true;
    }

    private boolean RunSkillManagerCommand(CommandSender sender, String[] args) {
        if (args.length <= 3) {
            return ShowCommandHelp(sender, 4);
        }
        if (args[0].equalsIgnoreCase(COMMAND_SKILL_MANAGER_INCREASE)) {
            return RunSkillManagerIncreaseCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase(COMMAND_SKILL_MANAGER_DECREASE)) {
            return RunSkillManagerDecreaseCommand(sender, args);
        }
        return false;
    }

    private boolean RunSkillManagerIncreaseCommand(CommandSender sender, String[] args) {
        String playerName = args[2];
        String points = args[3];
        Player target = Bukkit.getPlayer(playerName);
        SkillMenuPanel.SkillType type = SkillTypeSerialization.valueOf(args[1]);
        if (type.equals(SkillMenuPanel.SkillType.UNDEFINED)) {
            sender.sendMessage(LanguageManager.getString("skillTypeInvalid").replace("%skill%", args[1]));
            return false;
        }
        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", playerName));
            return false;
        }
        if (!StringUtils.isNumeric(points)) {
            sender.sendMessage(LanguageManager.getString("numberFormatInvalid"));
            return false;
        }

        int p = Integer.parseInt(points);
        MMOManager.incrementStatisticPoints(target, type, p);

        return true;
    }

    private boolean RunSkillManagerDecreaseCommand(CommandSender sender, String[] args) {
        String playerName = args[2];
        String points = args[3];
        Player target = Bukkit.getPlayer(playerName);
        SkillMenuPanel.SkillType type = SkillTypeSerialization.valueOf(args[1]);
        if (type.equals(SkillMenuPanel.SkillType.UNDEFINED)) {
            sender.sendMessage(LanguageManager.getString("skillTypeInvalid").replace("%skill%", args[1]));
            return false;
        }
        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", playerName));
            return false;
        }
        if (!StringUtils.isNumeric(points)) {
            sender.sendMessage(LanguageManager.getString("numberFormatInvalid"));
            return false;
        }

        int p = Integer.parseInt(points);
        MMOManager.decreaseStatisticPoints(target, type, p);

        return true;
    }

    private boolean RunRecordManagerCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException {
        // 该指令参数长度不能为0
        if (args.length == 0) {
            return ShowCommandHelp(sender, 1);
        }
        // /recordmanager load
        if (args[0].equalsIgnoreCase(COMMAND_RECORD_MANAGER_LOAD)) {
            return RunRecordManagerLoadCommand();
        }
        // /recordmanager save
        if (args[0].equalsIgnoreCase(COMMAND_RECORD_MANAGER_SAVE)) {
            return RunRecordManagerSaveCommand();
        }
        return false;
    }

    private boolean RunRecordManagerLoadCommand() throws IOException {
        RecordManager.Load();
        return true;
    }

    private boolean RunRecordManagerSaveCommand() throws IOException {
        RecordManager.Save();
        return true;
    }

    private boolean RunMinionCommand(CommandSender sender, String[] args) {
        // 该指令参数长度不能为0
        if (args.length == 0) {
            return ShowCommandHelp(sender, 1);
        }
        // /minion give
        if (args[0].equalsIgnoreCase(COMMAND_MINION_GIVE)) {
            return RunMinionGiveCommand(sender, args);
        }

        return false;
    }

    private boolean RunMinionGiveCommand(CommandSender sender, String[] args) {
        if (args.length <= 4) return ShowCommandHelp(sender, 5);

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", args[1]));
            return false;
        }
        if (!target.isOnline()) {
            sender.sendMessage(LanguageManager.getString("playerOffLine").replace("%player%", args[1]));
            return false;
        }

        if (!MinionManager.isMinionType(args[2])) {
            sender.sendMessage(LanguageManager.getString("minionUnknownType").replace("%minion_type%", args[2]));
            return false;
        }
        if (!StringUtils.isNumeric(args[4])) {
            sender.sendMessage(LanguageManager.getString("numberFormatInvalid"));
            return false;
        }

        int level = RomanNumber.toInteger(args[3]);
        if (level <= 0) level = 1;
        int amount = Integer.parseInt(args[4]);

        MinionType minion = MinionManager.getMinionType(args[2]);
        ItemStack is = MinionManager.getMinionStack(minion, level, amount);

        target.getInventory().addItem(is);

        return true;
    }

    private List<String> getRegulaNumberList() {
        List<String> tabs = new ArrayList<>();

        tabs.add("1");
        tabs.add("16");
        tabs.add("32");
        tabs.add("64");

        return tabs;
    }

    private List<String> getMinionTypeList() {
        return MinionManager.getDeclaredTypes();
    }

    private List<String> getMinionLevelList(MinionType type) {
        return MinionManager.getLevels(type);
    }

    private List<String> getCheckpointList(Player player) throws IOException, IllegalAccessException, InvalidConfigurationException {
        PlayerData data = PlayerManager.getPlayerData(player);
        String courseId = data.getDesigner().getCurrentCourseId();

        CourseInfo course = CourseManager.getCourse(courseId);
        if (course == null) return new ArrayList<>();

        return CourseManager.getCheckPointList(course.getName());
    }


    private List<String> getTrueFalseAsList() {
        List<String> tabs = new ArrayList<>();

        tabs.add("true");
        tabs.add("false");

        return tabs;
    }

    private List<String> getCourseAbandonList() throws IOException, IllegalAccessException, InvalidConfigurationException {
        return CourseManager.getAbandonIdList();
    }

    /**
     * op玩家获得整个跑道列表名册<br>非op玩家获得属于自己的跑道列表名册
     * @param player 玩家实例
     */
    private List<String> getCourseModifyList(Player player) throws IOException, IllegalAccessException, InvalidConfigurationException {
        PlayerData data = PlayerManager.getPlayerData(player);
        return data.getDesigner().list();
    }

    private List<String> getDeclaredArguments(@NotNull Player player, @NotNull String[] args) {
        List<String> tabs = new ArrayList<>();
        // 获取玩家在聊天栏输入指令时的当前参数的位置
        int index = args.length;
        // 根据已输入参数个数获取当前指令的参数列表
        List<CommandArgument> ca = this.args.stream().filter(x -> x.index == index).toList();
        // 循环每个参数，设置该参数的使用权限
        for (CommandArgument c : ca) {
            // 测试玩家没有权限
            // 条件: 权限不为空，玩家没有权限，玩家非op
            if (!StringUtils.isBlank(c.permission) && !player.hasPermission(c.permission) && !player.isOp()) continue;
            // 添加参数到tabComplete列表
            tabs.add(c.name);
        }

        return tabs;
    }

    private boolean RunCourseCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException, ClassNotFoundException {
        // 该指令参数长度不能为0
        if (args.length == 0) {
            return ShowCommandHelp(sender, 1);
        }
        // /course create
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_CREATE)) {
            return RunCourseCreateCommand(sender, args);
        }
        // /course leave
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_LEAVE)) {
            return RunCourseLeaveCommand(sender);
        }
        // /course modify
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_MODIFY)) {
            return RunCourseModifyCommand(sender, args);
        }
        // /course list
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_LIST)) {
            return RunCourseListCommand(sender);
        }
        // /course list
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_ABANDONS)) {
            return RunCourseAbandonsCommand(sender);
        }
        // /course delete
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_DELETE)) {
            return RunCourseDeleteCommand(sender, args);
        }
        // /course claim
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_CLAIM)) {
            return RunCourseClaimCommand(sender, args);
        }
        // /course startpoint
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_START_POINT)) {
            return RunCourseStartPointCommand(sender);
        }
        // /course checkpoint xxx
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_CHECKPOINT)) {
            return RunCourseCheckpointCommand(sender, args);
        }
        // /course ready
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_READY)) {
            return RunCourseReadyCommand(sender, args);
        }
        // /course display
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_DISPLAY)) {
            return RunCourseDisplayCommand(sender, args);
        }
        // /course tp
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_TELEPORT)) {
            return RunCourseTeleportCommand(sender, args);
        }
        // /course parkourkit
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_PARKOUR_KIT)) {
            return RunCourseParkourKitCommand(sender);
        }
        // /course linklobby
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_LINK_LOBBY)) {
            return RunCourseLinkLobbyCommand(sender, args);
        }
        // /course help
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_HELP)) {
            return ShowCommandHelp(sender, 0);
        }

        return false;
    }

    private boolean RunBroadcastPlusCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return ShowCommandHelp(sender, 1);
        }

        List<String> msg = BroadcastManager.getMessage(args[0]);
        for (String s : msg) {
            plugin.getServer().broadcastMessage(s);
        }

        return true;
    }

    private boolean RunCourseLinkLobbyCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        if (args.length <= 1) return ShowCommandHelp(sender, 2);

        // 非op玩家必须在跑道设计状态
        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        String lobby = args[1];
        boolean exist = ParkourApiManager.isLobbyExist(lobby);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("courseLobbyNotExist", player).replace("%lobby%", lobby));
            return false;
        }

        data.getDesigner().setLinkLobby(lobby);
        return true;
    }

    private boolean RunCourseParkourKitCommand(CommandSender sender) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 非op玩家必须在跑道设计状态
        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        data.getDesigner().getParkourKit();
        return true;
    }

    private boolean RunCourseTeleportCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 非op玩家必须在跑道设计状态
        if (data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        if (args.length <= 1) {
            player.sendMessage(LanguageManager.getString("courseTeleportEmpty", player));
            return false;
        }

        String courseName = args[1];
        if (!data.getDesigner().teleport(courseName)) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseName));
            return false;
        }

        return true;
    }

    private boolean RunCourseDisplayCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 非op玩家必须在跑道设计状态
        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        if (args.length <= 1) {
            player.sendMessage(LanguageManager.getString("courseDisplayEmpty", player));
            return false;
        }

        data.getDesigner().setDisplayName(args[1]);
        return true;
    }

    private boolean RunCourseReadyCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 非op玩家必须在跑道设计状态
        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        // 当参数不是true或者false时，判断参数无效
        if (args.length <= 1 || (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false"))) {
            player.sendMessage(LanguageManager.getString("courseReadyArgInvalid", player));
            return false;
        }

        boolean ready = Boolean.parseBoolean(args[1]);
        data.getDesigner().setReady(ready);
        return true;
    }

    private boolean RunCourseCheckpointCommand(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 非op玩家必须在跑道设计状态
        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        if (args.length >= 2 && StringUtils.isNumeric(args[1])) {
            int index = Integer.parseInt(args[1]);
            if (!data.getDesigner().addCheckpoint(index)) {
                player.sendMessage(LanguageManager.getString("courseNoStartPoint", player));
            }
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase(COMMAND_COURSE_CHECKPOINT_REMOVE)) {
            data.getDesigner().deleteCheckpoint();
        }
        if (args.length == 1) {
            int index = 0;
            if (!data.getDesigner().addCheckpoint(index)) {
                player.sendMessage(LanguageManager.getString("courseNoStartPoint", player));
                return false;
            }
        }

        return true;
    }

    private boolean RunCourseClaimCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 判断指令的参数数量
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // 非op玩家不能在设计跑道时同时领取另一条跑道
        if (data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        // op玩家不需要领取任何跑道
        if (player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseOpNoNeedClaim", player));
            return false;
        }
        // 获取跑酷赛道实例
        String courseId = args[1];
        boolean exist= CourseManager.existCourse(courseId);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseId));
            return false;
        }
        // 检测跑道是否被弃置
        boolean abandon = CourseManager.isAbandoned(courseId);
        if (!abandon) {
            player.sendMessage(LanguageManager.getString("courseNonAbandoned", player).replace("%course%", courseId));
            return false;
        }

        data.getDesigner().claim(courseId);
        player.sendMessage(LanguageManager.getString("courseClaimOK", player).replace("%course%", courseId));
        return true;
    }

    // /course list
    private boolean RunCourseListCommand(CommandSender sender) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        List<String> values = data.getDesigner().list();

        if (values.size() == 0) {
            player.sendMessage(LanguageManager.getString("courseNoAssetInName", player));
        } else {
            player.sendMessage(LanguageManager.getString("courseHasAssetInName", player));
        }

        for (String s : values) {
            player.sendMessage(s);
        }

        return true;
    }

    private boolean RunCourseAbandonsCommand(CommandSender sender) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        List<String> values = data.getDesigner().abandons();
        for (String s : values) {
            player.sendMessage(s);
        }

        return true;
    }

    // /course create
    private boolean RunCourseCreateCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // 判断指令的参数数量
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // 非op玩家不能在设计跑道时同时再创建另一条跑道
        if (data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        // 创建跑酷赛道，如果该跑酷的名称存在，则停止创建
        String courseId = args[1];
        if (CourseManager.existCourse(courseId)) {
            player.sendMessage(LanguageManager.getString("courseExist", player).replace("%course%", courseId));
            return false;
        }
        // 获取新创建的跑酷赛道实例，并且搭建赛道的初始平台
        CourseInfo course = CourseManager.createCourse(courseId);
        if (!player.isOp()) course.setAbandon(false);
        data.getDesigner().design(courseId, true);
        // 保存跑酷赛道信息到course.yml
        CourseManager.save();

        return true;
    }

    // /course modify
    private boolean RunCourseModifyCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        if (data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        String courseId = args[1];
        // 检测非op玩家是否拥有该赛道
        if (!data.getCourseOwned().contains(courseId) && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseNotOwned", player).replace("%course%", courseId));
            return false;
        }
        // 当赛道Id不存在于列表中
        boolean exist = CourseManager.existCourse(courseId);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseId));
            return false;
        }
        // 非op玩家不能修改已经被舍弃的赛道
        boolean abandon = CourseManager.isAbandoned(courseId);
        if (abandon && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseWasAbandoned", player).replace("%course%", courseId));
            return false;
        }

        data.getDesigner().design(courseId, false);
        return true;
    }

    // /course delete
    private boolean RunCourseDeleteCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        if (data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }

        String courseId = args[1];
        if (!data.getCourseOwned().contains(courseId) && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseNotOwned", player).replace("%course%", courseId));
            return false;
        }
        boolean exist = CourseManager.existCourse(courseId);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseId));
            return false;
        }
        boolean abandon = CourseManager.isAbandoned(courseId);
        if (abandon) {
            player.sendMessage(LanguageManager.getString("courseWasAbandoned", player).replace("%course%", courseId));
            return false;
        }

        data.getDesigner().abandon(courseId);
        player.sendMessage(LanguageManager.getString("courseHasAbandoned", player).replace("%course%", courseId));
        return true;
    }

    // /course leave
    private boolean RunCourseLeaveCommand(CommandSender sender) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        data.getDesigner().leave();
        return false;
    }

    private boolean RunCourseStartPointCommand(CommandSender sender) {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        if (!data.isDesignMode() && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        if (data.getDesigner().isOutOfRange(player.getLocation())) {
            player.sendMessage(LanguageManager.getString("courseOutOfRange", player));
            return false;
        }

        String courseId = data.getDesigner().getCurrentCourseId();
        if (StringUtils.isBlank(courseId)) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        data.getDesigner().startPoint(courseId);
        player.sendMessage(LanguageManager.getString("courseStartPoint", player).replace("%course%", courseId));
        return true;
    }

    private boolean RunCrazyCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        // 当前hc指令需要至少1个参数
        if (args.length == 0) {
            return ShowCommandHelp(sender, 1);
        }

        if (args[0].equalsIgnoreCase(COMMAND_CRAZY_CRAFTING)) {
            return RunCrazyCraftingCommand(sender);
        }
        if (args[0].equalsIgnoreCase(COMMAND_CRAZY_ENCHANTING)) {
            return RunCrazyEnchantingCommand();
        }
        if (args[0].equalsIgnoreCase(COMMAND_CRAZY_ANVIL)) {
            return RunCrazyAnvilCommand();
        }

        return false;
    }

    private boolean RunCrazyCraftingCommand(CommandSender sender) {
        if (!(sender instanceof Player player)) return false;
        Inventory inventory = RecipeManager.createCraftPanel();
        if (inventory == null) return false;

        player.openInventory(inventory);
        return true;
    }

    private boolean RunCrazyEnchantingCommand() {
        return false;
    }

    private boolean RunCrazyAnvilCommand() {
        return false;
    }

    private boolean RunScaleCommand(CommandSender sender, String[] args) {
        // 当前hc指令需要至少2个参数
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
        if (args.length == 0) {
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

        if (args.length == 0) {
            return ShowCommandHelp(sender, 1);
        }

        if (args.length >= 4 && args[3] != null && patternNumber.matcher(args[3]).matches()) {
            amount = Integer.parseInt(args[3]);
        }

        if (args[0].equalsIgnoreCase(COMMAND_QM_HELP)) {
            return ShowCommandHelp(sender, 0);
        }

        if (args[0].equalsIgnoreCase(COMMAND_QM_GIVE)) {
            return RunQMGiveCommand(sender, args, amount);
        }

        if (args[0].equalsIgnoreCase(COMMAND_QM_GUI)) {
            return RunQMGuiCommand((Player) sender);
        }

        return false;
    }

    private boolean RunPlaygroundMenusCommand(@NotNull CommandSender sender, String @NotNull [] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        if (args.length <= 1) {
            return ShowCommandHelp(sender, 2);
        }
        if (!(sender instanceof Player) && args.length == 2) {
            return false;
        }

        if (args[0].equalsIgnoreCase(COMMAND_PLAYGROUND_MENUS_OPEN)) {
            return RunPlaygroundMenusOpenCommand(sender, args);
        }
        if (args[0].equalsIgnoreCase(COMMAND_PLAYGROUND_MENUS_CLOSE)) {
            return RunPlaygroundMenusCloseCommand(sender, args);
        }

        return true;
    }

    private boolean RunPlaygroundMenusOpenCommand(@NotNull CommandSender sender, String @NotNull [] args) throws InvalidConfigurationException {
        String target = args.length >= 3 ? args[2] : sender.getName();
        Player targetPlayer = Bukkit.getPlayer(target);
        // 显示目标玩家不存在信息
        if (targetPlayer == null) {
            sender.sendMessage(LanguageManager.getString("playerNotExist").replace("%player%", target));
            return false;
        }
        // 获取菜单名称和实例
        String menuName = args[1].toLowerCase();
        String menuId = MenuManager.getTemplateName(menuName);
        MenuPanel menu = MenuManager.getMenuPanel(menuId, targetPlayer);
        // 显示菜单实例不存在信息
        if (menu == null) {
            targetPlayer.sendMessage(LanguageManager.getString("menuNotExist").replace("%menu%", menuName));
            return false;
        }
        // 显示菜单在当前世界不可使用信息
        if (menu.isDisabled(targetPlayer)) {
            targetPlayer.sendMessage(LanguageManager.getString("menuWorldProhibited", targetPlayer)
                    .replace("%world%", targetPlayer.getWorld().getName())
                    .replace("%menu%", menu.getTitle())
            );
            return false;
        }

        menu.OnConfigured(MMOManager.getYaml());
        Inventory inventory = menu.OnOpening(targetPlayer, menuName);
        if (inventory != null) targetPlayer.openInventory(inventory);
        return true;
    }

    private boolean RunPlaygroundMenusCloseCommand(@NotNull CommandSender sender, String @NotNull [] args) throws InvalidConfigurationException {
        String menuId = args[1];
        String target = args.length >= 3 ? args[2] : sender.getName();
        Player targetPlayer = Bukkit.getPlayer(target);
        if (targetPlayer == null) return false;

        MenuPanel menu = MenuManager.getMenuPanel(menuId, targetPlayer);
        if (menu == null) {
            targetPlayer.sendMessage(LanguageManager.getString("menuNotExist").replace("%menu%", menuId));
            return false;
        }

        menu.OnClosed(targetPlayer, menuId);
        targetPlayer.closeInventory();
        return true;
    }


    private boolean RunHCReloadCommand(CommandSender sender) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFieldException, IllegalAccessException, IOException {
        long a = new Date().getTime();
        RecordManager.Save();
        Global.ReloadConfiguration();

        long b = new Date().getTime();
        long c = b - a;
        sender.sendMessage(LanguageManager.getString("reload", sender).replace("%time_escaped%", String.valueOf(c)));

        return true;
    }

    private boolean RunSeenCommand(CommandSender sender, String[] args) {
        if (args.length == 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        Date lastLogin = new Date();
        boolean foundPlayer = false;
        String status = "";

        OfflinePlayer[] offlinePlayers = plugin.getServer().getOfflinePlayers();
        for (OfflinePlayer o : offlinePlayers) {
            if (!Objects.requireNonNull(o.getName()).equalsIgnoreCase(args[0])) continue;

            status = LanguageManager.getString(String.format("onlineStatusMessage.%s", o.isOnline()), o.getPlayer());
            lastLogin.setTime(o.getLastPlayed());
            foundPlayer = true;
            break;
        }

        if (!foundPlayer) {
            sender.sendMessage(LanguageManager.getString("playerNotExist", sender).replace("%player%", args[0]));
        } else {
            String message = LanguageManager.getString("playerLastLoginTime", sender);
            String loginFormat = Global.getDateFormat(lastLogin, DateFormat.FULL, Locale.CHINA);
            sender.sendMessage(message.replace("%onlineStatusMessage%", status).replace("%player%", args[0]).replace("%logintime%", loginFormat));
        }
        return true;
    }

    private boolean RunUnRegisterCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少1个参数
        if (args.length == 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Unregister(args[0]);
    }

    private boolean RunLoginCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少1个参数
        if (args.length == 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Login(args[0]);
    }

    private boolean RunLogoutCommand(CommandSender sender) {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }

        player.kickPlayer(LanguageManager.getString("playerLogout", sender).replace("%player%", player.getName()));
        return true;
    }

    private boolean RunRegisterCommand(CommandSender sender, String[] args) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少2个参数
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // 检查2个参数(密码)是否一样，不区分大小写
        if (!args[0].equalsIgnoreCase(args[1])) {
            sender.sendMessage(LanguageManager.getString("playerPasswordNotMatch", sender));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Register(args[0]);
    }

    private boolean RunChangePasswordCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }
        // 检查参数数量，必须至少3个参数
        if (args.length <= 2) {
            ShowCommandHelp(sender, 3);
            return false;
        }
        if (!args[1].equalsIgnoreCase(args[2])) {
            sender.sendMessage(LanguageManager.getString("playerPasswordNotMatch", sender));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.ChangePassword(args[0], args[1]);
    }

    private boolean RunBanPlayerCommand(CommandSender sender, String[] args) throws SQLException, IllegalAccessException, IOException, InvalidConfigurationException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
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

    private boolean RunUnBanPlayerCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException, SQLException {
        // 该指令必须玩家执行
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        playerData.UnBanPlayer(args[0]);

        return true;
    }

    private boolean RunQMGuiCommand(Player player) {
        // TODO: 需要实施/quartermaster gui指令
        player.sendMessage(LanguageManager.getString("UnderConstruction", player));
        return true;
    }

    private boolean RunQMGiveCommand(CommandSender sender, String[] args, int amount) {
        if (args.length <= 2) {
            ShowCommandHelp(sender, 3);
            return false;
        }

        ItemManager.give(sender, args[1], args[2], amount);
        return true;
    }

    private boolean ValidateCommand(CommandSender sender, String[] args) {
        // 如果指令被定义为不能在控制台执行
        // 并且使用了控制台发送当前指令，验证失败
        if (!this.isConsole && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(LanguageManager.getString("no-console-command", sender).replace("%command%", id));
            return false;
        }
        // 如果指令被定义为不能通过玩家执行
        // 并且通过了玩家发送当前指令，验证失败
        if (!this.isPlayer && sender instanceof Player) {
            sender.sendMessage(LanguageManager.getString("no-player-command", sender).replace("%command%", id));
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
                sender.sendMessage(LanguageManager.getString("no-world-command", sender).replace("%command%", id).replace("%world%", worldName));
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
                    continue;
                }
                // 如果当前指令参数不在参数列表的位置定义，则指令语法错误，提示/<command> usage
                if (arg.index - 1 != Arrays.asList(args).indexOf(s)) {
                    continue;
                }
                // 如当玩家不拥有前指令参数的权限，提示no-permission
                if (!player.hasPermission(arg.permission) && !arg.permission.equalsIgnoreCase("")) {
                    return false;
                }
            }
            /*
             测试当前用户是否拥有执行当前指令的权限
             如果没有，则显示no-permission，并且返回False
            */
            return this.testPermission(sender);
        }
        // 验证通过
        return true;
    }

    private boolean ShowCommandHelp(CommandSender sender, int argLength) {
        if (argLength >= 1) {
            sender.sendMessage(LanguageManager.getString("parameterInCorrect", sender).replace("%length%", String.valueOf(argLength)));
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
        this.setPermissionMessage(LanguageManager.getString("no-permission").replace("%permission%", permission));
        this.setUsage(LanguageManager.getString(String.format("%s.usage", id)));
        this.setDescription(LanguageManager.getString(String.format("%s.description", id)));
    }
}
