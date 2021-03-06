package org.hcmc.hcplayground.model.command;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.model.parkour.CourseInfo;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RomanNumber;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
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
    public static final String COMMAND_REGISTER = "register";
    public static final String COMMAND_UNREGISTER = "unregister";
    public static final String COMMAND_LOGIN = "login";
    public static final String COMMAND_LOGOUT = "logout";
    public static final String COMMAND_CHANGE_PASSWORD = "changepassword";
    public static final String COMMAND_SEEN = "seen";
    public static final String COMMAND_BAN_PLAYER = "banplayer";
    public static final String COMMAND_UNBAN_PLAYER = "unbanplayer";
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
     * ???????????????????????????????????????null??????????????????????????????????????????????????????
     */
    @Expose
    @SerializedName(value = "permission")
    public String permission = "";
    /**
     * ??????????????????????????????????????????
     */
    @Expose
    @SerializedName(value = "runOnPlayer")
    public boolean isPlayer = true;
    /**
     * ????????????????????????????????????????????????
     */
    @Expose
    @SerializedName(value = "runOnConsole")
    public boolean isConsole = false;
    /**
     * ????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Expose
    @SerializedName(value = "worlds")
    public List<String> worlds = new ArrayList<>();
    /**
     * ??????????????????????????????????????????
     */
    @Expose
    @SerializedName(value = "aliases")
    public List<String> aliases = new ArrayList<>();
    /**
     * ???????????????????????????????????????????????????????????????
     */
    @Expose(serialize = false, deserialize = false)
    public List<CommandArgument> args = new ArrayList<>();
    /**
     * ???????????????ID??????????????????????????????????????????
     */
    @Expose(serialize = false, deserialize = false)
    public String id;

    @Expose(serialize = false, deserialize = false)
    private JavaPlugin plugin;

    public CommandItem(String name) {
        super(name);
    }

    /**
     * ???Bukkit??????????????????
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
        // ?????????????????????tabComplete
        List<String> org = super.tabComplete(sender, alias, args);
        int index = args.length;
        // ??????????????????????????????????????????????????????????????????TabComplete????????????
        if (!(sender instanceof Player player)) return org;
        tabs = getDeclaredArguments(player, args);

        try {
            // ?????????????????????
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
            // ????????????????????? - /menu
            if (commandText.equalsIgnoreCase(COMMAND_MENU)) {
                return RunOpenChestMenuCommand((Player) sender, COMMAND_MENU);
            }
            // ?????????????????????????????? - /profile
            if (commandText.equalsIgnoreCase(COMMAND_PROFILE)) {
                return RunOpenChestMenuCommand((Player) sender, COMMAND_PROFILE);
            }
            // ?????????????????????????????? - /profile
            if (commandText.equalsIgnoreCase(COMMAND_RECIPE_BOOK)) {
                return RunOpenChestMenuCommand((Player) sender, COMMAND_RECIPE_BOOK);
            }
            if (commandText.equalsIgnoreCase(COMMAND_CRAZY)) {
                return RunCrazyCommand(sender, args);
            }
            // ??????????????? - /quatermaster
            if (commandText.equalsIgnoreCase(COMMAND_QUARTERMASTER)) {
                return RunQuartermasterCommand(sender, args);
            }
            // ?????????????????? - /register
            if (commandText.equalsIgnoreCase(COMMAND_REGISTER)) {
                return RunRegisterCommand(sender, args);
            }
            // ?????????????????? - /unregister
            if (commandText.equalsIgnoreCase(COMMAND_UNREGISTER)) {
                return RunUnRegisterCommand(sender, args);
            }
            // ?????????????????? - /login
            if (commandText.equalsIgnoreCase(COMMAND_LOGIN)) {
                return RunLoginCommand(sender, args);
            }
            // ?????????????????? - /logout
            if (commandText.equalsIgnoreCase(COMMAND_LOGOUT)) {
                return RunLogoutCommand(sender, args);
            }
            // ???????????????????????? - /changepassword
            if (commandText.equalsIgnoreCase(COMMAND_CHANGE_PASSWORD)) {
                return RunChangePasswordCommand(sender, args);
            }
            // ??????????????????????????????????????????????????? /seen
            if (commandText.equalsIgnoreCase(COMMAND_SEEN)) {
                return RunSeenCommand(sender, args);
            }
            // ????????????????????????????????? - /banplayer
            if (commandText.equalsIgnoreCase(COMMAND_BAN_PLAYER)) {
                return RunBanPlayerCommand(sender, args);
            }
            // ?????????????????? /unbanplayer
            if (commandText.equalsIgnoreCase(COMMAND_UNBAN_PLAYER)) {
                return RunUnBanPlayerCommand(sender, args);
            }
            // ??????hc?????? - /hcplayground
            if (commandText.equalsIgnoreCase(COMMAND_HCPLAYGROUND)) {
                return RunHCPlaygroundCommand(sender, args);
            }
            // ??????scale?????? /scale
            if (commandText.equalsIgnoreCase(COMMAND_SCALE)) {
                return RunScaleCommand(sender, args);
            }
            // ??????course?????? /course
            if (commandText.equalsIgnoreCase(COMMAND_COURSE)) {
                return RunCourseCommand(sender, args);
            }
            // ??????broadcastplus?????? /broadcastplus /bp
            if (commandText.equalsIgnoreCase(COMMAND_BROADCAST_PLUS)) {
                return RunBroadcastPlusCommand(sender, args);
            }
            // ??????minion?????? /minion
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

    private boolean RunRecordManagerCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException {
        // ??????????????????????????????0
        if (args.length <= 0) {
            return ShowCommandHelp(sender, 1);
        }
        // /recordmanager load
        if (args[0].equalsIgnoreCase(COMMAND_RECORD_MANAGER_LOAD)) {
            return RunRecordManagerLoadCommand(sender, args);
        }
        // /recordmanager save
        if (args[0].equalsIgnoreCase(COMMAND_RECORD_MANAGER_SAVE)) {
            return RunRecordManagerSaveCommand(sender, args);
        }
        return false;
    }

    private boolean RunRecordManagerLoadCommand(CommandSender sender, String[] args) throws IOException {
        //File file = new File(String.format("%s/database/record.yml", plugin.getDataFolder()));
        //YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        RecordManager.Load();

        return true;
    }

    private boolean RunRecordManagerSaveCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException {
        RecordManager.Save();
        return true;
    }

    private boolean RunMinionCommand(CommandSender sender, String[] args) {
        // ??????????????????????????????0
        if (args.length <= 0) {
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
        ItemStack is = MinionManager.getMinion(minion, level, amount);

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
        /*
        List<String> tabs = new ArrayList<>();
        MinionType[] types = MinionType.values();

        for (MinionType type : types) {
            tabs.add(type.name().toLowerCase());
        }

         */

        return MinionManager.getDeclaredTypes();
    }

    private List<String> getMinionLevelList(MinionType type) {
        /*
        List<String> tabs = new ArrayList<>();

        tabs.add("I");
        tabs.add("II");
        tabs.add("III");
        tabs.add("IV");
        tabs.add("V");
        tabs.add("VI");
        tabs.add("VII");
        tabs.add("VIII");
        tabs.add("IX");
        tabs.add("X");
        tabs.add("XI");
        tabs.add("XII");

         */

        return MinionManager.getLevels(type);
    }

    private List<String> getCheckpointList(Player player) throws IOException, IllegalAccessException, InvalidConfigurationException {
        PlayerData data = PlayerManager.getPlayerData(player);
        String courseId = data.designer.getCurrentCourseId();

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
     * op????????????????????????????????????<br>???op?????????????????????????????????????????????
     * @param player ????????????
     */
    private List<String> getCourseModifyList(Player player) throws IOException, IllegalAccessException, InvalidConfigurationException {
        PlayerData data = PlayerManager.getPlayerData(player);
        return data.designer.list();
    }

    private List<String> getDeclaredArguments(@NotNull Player player, @NotNull String[] args) {
        List<String> tabs = new ArrayList<>();
        // ???????????????????????????????????????????????????????????????
        int index = args.length;
        // ????????????????????????????????????????????????????????????
        List<CommandArgument> ca = this.args.stream().filter(x -> x.index == index).toList();
        // ???????????????????????????????????????????????????
        for (CommandArgument c : ca) {
            // ????????????????????????
            // ??????: ????????????????????????????????????????????????op
            if (!StringUtils.isBlank(c.permission) && !player.hasPermission(c.permission) && !player.isOp()) continue;
            // ???????????????tabComplete??????
            tabs.add(c.name);
        }

        return tabs;
    }

    private boolean RunCourseCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException, ClassNotFoundException {
        // ??????????????????????????????0
        if (args.length <= 0) {
            return ShowCommandHelp(sender, 1);
        }
        // /course create
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_CREATE)) {
            return RunCourseCreateCommand(sender, args);
        }
        // /course leave
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_LEAVE)) {
            return RunCourseLeaveCommand(sender, args);
        }
        // /course modify
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_MODIFY)) {
            return RunCourseModifyCommand(sender, args);
        }
        // /course list
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_LIST)) {
            return RunCourseListCommand(sender, args);
        }
        // /course list
        if (args[0].equalsIgnoreCase(COMMAND_COURSE_ABANDONS)) {
            return RunCourseAbandonsCommand(sender, args);
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
            return RunCourseStartPointCommand(sender, args);
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
            return RunCourseParkourKitCommand(sender, args);
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
        if (args.length <= 0) {
            return ShowCommandHelp(sender, 1);
        }

        List<String> msg = BroadcastManager.getMessage(args[0]);
        for (String s : msg) {
            plugin.getServer().broadcastMessage(s);
        }

        return true;
    }

    private boolean RunCourseLinkLobbyCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        if (args.length <= 1) return ShowCommandHelp(sender, 2);

        // ???op?????????????????????????????????
        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        String lobby = args[1];
        boolean exist = ParkourApiManager.isLobbyExist(lobby);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("lobbyNotExist", player).replace("%lobby%", lobby));
            return false;
        }

        data.designer.setLinkLobby(lobby);
        return true;
    }

    private boolean RunCourseParkourKitCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???op?????????????????????????????????
        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        data.designer.getParkourKit();
        return true;
    }

    private boolean RunCourseTeleportCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???op?????????????????????????????????
        if (data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        if (args.length <= 1) {
            player.sendMessage(LanguageManager.getString("courseTeleportEmpty", player));
            return false;
        }

        String courseName = args[1];
        if (!data.designer.teleport(courseName)) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseName));
            return false;
        }

        return true;
    }

    private boolean RunCourseDisplayCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???op?????????????????????????????????
        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        if (args.length <= 1) {
            player.sendMessage(LanguageManager.getString("courseDisplayEmpty", player));
            return false;
        }

        data.designer.setDisplayName(args[1]);
        return true;
    }

    private boolean RunCourseReadyCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???op?????????????????????????????????
        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        // ???????????????true??????false????????????????????????
        if (args.length <= 1 || (!args[1].equalsIgnoreCase("true") && !args[1].equalsIgnoreCase("false"))) {
            player.sendMessage(LanguageManager.getString("courseReadyArgInvalid", player));
            return false;
        }

        boolean ready = Boolean.parseBoolean(args[1]);
        data.designer.setReady(ready);
        return true;
    }

    private boolean RunCourseCheckpointCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???op?????????????????????????????????
        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        if (args.length >= 2 && StringUtils.isNumeric(args[1])) {
            int index = Integer.parseInt(args[1]);
            if (!data.designer.addCheckpoint(index)) {
                player.sendMessage(LanguageManager.getString("courseNoStartPoint", player));
            }
        }
        if (args.length >= 2 && args[1].equalsIgnoreCase(COMMAND_COURSE_CHECKPOINT_REMOVE)) {
            data.designer.deleteCheckpoint();
        }
        if (args.length == 1) {
            int index = 0;
            if (!data.designer.addCheckpoint(index)) {
                player.sendMessage(LanguageManager.getString("courseNoStartPoint", player));
                return false;
            }
        }

        return true;
    }

    private boolean RunCourseClaimCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???????????????????????????
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // ???op?????????????????????????????????????????????????????????
        if (data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        // op?????????????????????????????????
        if (player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseOpNoNeedClaim", player));
            return false;
        }
        // ????????????????????????
        String courseId = args[1];
        boolean exist= CourseManager.existCourse(courseId);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseId));
            return false;
        }
        // ???????????????????????????
        boolean abandon = CourseManager.isAbandoned(courseId);
        if (!abandon) {
            player.sendMessage(LanguageManager.getString("courseNonAbandoned", player).replace("%course%", courseId));
            return false;
        }

        data.designer.claim(courseId);
        player.sendMessage(LanguageManager.getString("courseClaimOK", player).replace("%course%", courseId));
        return true;
    }

    // /course list
    private boolean RunCourseListCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        List<String> values = data.designer.list();

        if (values.size() <= 0) {
            player.sendMessage(LanguageManager.getString("courseNoAssetInName", player));
        } else {
            player.sendMessage(LanguageManager.getString("courseHasAssetInName", player));
        }

        for (String s : values) {
            player.sendMessage(s);
        }

        return true;
    }

    private boolean RunCourseAbandonsCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        List<String> values = data.designer.abandons();
        for (String s : values) {
            player.sendMessage(s);
        }

        return true;
    }

    // /course create
    private boolean RunCourseCreateCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);
        // ???????????????????????????
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // ???op????????????????????????????????????????????????????????????
        if (data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        // ?????????????????????????????????????????????????????????????????????
        String courseId = args[1];
        if (CourseManager.existCourse(courseId)) {
            player.sendMessage(LanguageManager.getString("courseExist", player).replace("%course%", courseId));
            return false;
        }
        // ????????????????????????????????????????????????????????????????????????
        CourseInfo course = CourseManager.createCourse(courseId);
        if (!player.isOp()) course.setAbandon(false);
        data.designer.design(courseId, true);
        // ???????????????????????????course.yml
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
        if (data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }
        String courseId = args[1];
        // ?????????op???????????????????????????
        if (!data.courseIds.contains(courseId) && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseNotOwned", player).replace("%course%", courseId));
            return false;
        }
        // ?????????Id?????????????????????
        boolean exist = CourseManager.existCourse(courseId);
        if (!exist) {
            player.sendMessage(LanguageManager.getString("courseNotExist", player).replace("%course%", courseId));
            return false;
        }
        // ???op??????????????????????????????????????????
        boolean abandon = CourseManager.isAbandoned(courseId);
        if (abandon && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseWasAbandoned", player).replace("%course%", courseId));
            return false;
        }

        data.designer.design(courseId, false);
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
        if (data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseDesigning", player));
            return false;
        }

        String courseId = args[1];
        if (!data.courseIds.contains(courseId) && !player.isOp()) {
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

        data.designer.abandon(courseId);
        player.sendMessage(LanguageManager.getString("courseHasAbandoned", player).replace("%course%", courseId));
        return true;
    }

    // /course leave
    private boolean RunCourseLeaveCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException, ClassNotFoundException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        data.designer.leave();
        return false;
    }

    private boolean RunCourseStartPointCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Player player = (Player) sender;
        PlayerData data = PlayerManager.getPlayerData(player);

        if (!data.isCourseDesigning && !player.isOp()) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }

        if (!data.designer.RangeDetection(player.getLocation())) {
            player.sendMessage(LanguageManager.getString("outOfCourseRange", player));
            return false;
        }

        String courseId = data.designer.getCurrentCourseId();
        if (StringUtils.isBlank(courseId)) {
            player.sendMessage(LanguageManager.getString("courseHasLeft", player));
            return false;
        }
        data.designer.startPoint(courseId);
        player.sendMessage(LanguageManager.getString("courseStartPoint", player).replace("%course%", courseId));
        return true;
    }

    private boolean RunCrazyCommand(CommandSender sender, String[] args) throws IOException, IllegalAccessException, InvalidConfigurationException {
        // ??????hc??????????????????1?????????
        if (args.length <= 0) {
            return ShowCommandHelp(sender, 1);
        }

        if (args[0].equalsIgnoreCase(COMMAND_CRAZY_CRAFTING)) {
            return RunCrazyCraftingCommand(sender);
        }
        if (args[0].equalsIgnoreCase(COMMAND_CRAZY_ENCHANTING)) {
            return RunCrazyEnchantingCommand(sender);
        }
        if (args[0].equalsIgnoreCase(COMMAND_CRAZY_ANVIL)) {
            return RunCrazyAnvilCommand(sender);
        }

        return false;
    }

    private boolean RunCrazyCraftingCommand(CommandSender sender) throws IOException, IllegalAccessException, InvalidConfigurationException {
        String menuId = String.format("%s%s", COMMAND_CRAZY, COMMAND_CRAZY_CRAFTING);
        Player player = (Player) sender;
        Inventory inv = MenuManager.CreateMenu(menuId, player);
        if (inv == null) return false;

        player.openInventory(inv);

        return false;
    }

    private boolean RunCrazyEnchantingCommand(CommandSender sender) {
        return false;
    }

    private boolean RunCrazyAnvilCommand(CommandSender sender) {
        return false;
    }

    private boolean RunScaleCommand(CommandSender sender, String[] args) {
        // ??????hc??????????????????2?????????
        if (args.length <= 1) {
            return ShowCommandHelp(sender, 2);
        }

        Player player = (Player) sender;
        if (!StringUtils.isNumeric(args[0]) || !StringUtils.isNumeric(args[1])) {
            player.sendMessage("??c???????????????");
            return false;
        }
        int scale = Integer.parseInt(args[0]);
        int maxHealth = Integer.parseInt(args[1]);
        if (scale <= 0 || maxHealth <= 0) {
            player.sendMessage("??c???????????????");
            return false;
        }

        player.setHealthScale(scale);
        AttributeInstance instance = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (instance == null) return false;
        instance.setBaseValue(maxHealth);

        return true;
    }

    // ??????hcplayground??????
    private boolean RunHCPlaygroundCommand(CommandSender sender, String[] args) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFieldException, IllegalAccessException, IOException {
        // ??????hc??????????????????1?????????
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
            return RunQMGuiCommand((Player) sender, args);
        }

        return false;
    }

    private boolean RunOpenChestMenuCommand(Player player, String menuId) throws IOException, IllegalAccessException, InvalidConfigurationException {
        Inventory inv = MenuManager.CreateMenu(menuId, player);
        if (inv == null) return false;
        player.openInventory(inv);

        return true;
    }


    private boolean RunHCReloadCommand(CommandSender sender) throws SQLException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchFieldException, IllegalAccessException, IOException {
        long a = new Date().getTime();
        Global.ReloadConfiguration();

        long b = new Date().getTime();
        long c = b - a;
        sender.sendMessage(LanguageManager.getString("reload", sender).replace("%time_escaped%", String.valueOf(c)));

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
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }
        // ?????????????????????????????????1?????????
        if (args.length <= 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Unregister(args[0]);
    }

    private boolean RunLoginCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }
        // ?????????????????????????????????1?????????
        if (args.length <= 0) {
            ShowCommandHelp(sender, 1);
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Login(args[0]);
    }

    private boolean RunLogoutCommand(CommandSender sender, String[] args) {
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }

        player.kickPlayer(LanguageManager.getString("playerLogout", sender).replace("%player%", player.getName()));
        return true;
    }

    private boolean RunRegisterCommand(CommandSender sender, String[] args) throws SQLException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }
        // ?????????????????????????????????2?????????
        if (args.length <= 1) {
            ShowCommandHelp(sender, 2);
            return false;
        }
        // ??????2?????????(??????)?????????????????????????????????
        if (!args[0].equalsIgnoreCase(args[1])) {
            sender.sendMessage(LanguageManager.getString("playerPasswordNotMatch", sender));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        return playerData.Register(args[0]);
    }

    private boolean RunChangePasswordCommand(CommandSender sender, String[] args) throws InvalidAlgorithmParameterException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, IllegalAccessException, IOException, InvalidConfigurationException {
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }
        // ?????????????????????????????????3?????????
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
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }
        // ?????????????????????????????????2?????????
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
        // ???????????????????????????
        if (!(sender instanceof Player player)) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }

        PlayerData playerData = PlayerManager.getPlayerData(player);
        playerData.UnBanPlayer(args[0]);

        return true;
    }

    private boolean RunQMGuiCommand(Player player, String[] args) {
        // TODO: ????????????/quartermaster gui??????
        player.sendMessage(LanguageManager.getString("UnderConstruction", player));
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
        // ????????????????????????????????????????????????
        // ?????????????????????????????????????????????????????????
        if (!this.isConsole && sender instanceof ConsoleCommandSender) {
            sender.sendMessage(LanguageManager.getString("console-message", sender).replace("%command%", id));
            return false;
        }
        // ????????????????????????????????????????????????
        // ??????????????????????????????????????????????????????
        if (!this.isPlayer && sender instanceof Player) {
            sender.sendMessage(LanguageManager.getString("player-message", sender).replace("%command%", id));
            return false;
        }
        // ???????????????????????????????????????????????????????????????
        // ???????????????????????????????????????????????????????????????
        if (sender instanceof Player player) {
            // ???????????????op???????????????????????????
            if (player.isOp()) return true;
            // ???????????????????????????????????????
            World world = player.getWorld();
            String worldName = world.getName();
            if (!worlds.contains(worldName) && worlds.size() != 0) {
                sender.sendMessage(LanguageManager.getString("world-message", sender).replace("%command%", id).replace("%world%", worldName));
                return false;
            }
            // ??????????????????????????????
            for (String s : args) {
                // ????????????????????????0?????????????????????????????????????????????????????????????????????????????????
                if (this.args.size() == 0) continue;
                // ??????????????????????????????????????????????????????
                CommandArgument arg = this.args.stream().filter(x -> x.name.equalsIgnoreCase(s)).findAny().orElse(null);
                // ??????????????????????????????????????????????????????????????????????????????/<command> usage
                if (arg == null) {
                    //sender.sendMessage(LanguageManager.getString(String.format("%s.usage", id), sender));
                    continue;
                }
                // ??????????????????????????????????????????????????????????????????????????????????????????/<command> usage
                if (arg.index - 1 != Arrays.asList(args).indexOf(s)) {
                    //sender.sendMessage(getUsage());
                    continue;
                }
                // ??????????????????????????????????????????????????????permission-message
                if (!player.hasPermission(arg.permission) && !arg.permission.equalsIgnoreCase("")) {
                    //sender.sendMessage(LanguageManager.getString("permission-message", sender).replace("%permission%", arg.permission));
                    return false;
                }
            }
            /*
             ?????????????????????????????????????????????????????????
             ????????????????????????permission-message???????????????False
            */
            return this.testPermission(sender);
        }
        // ????????????
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
        // ????????????????????????????????????????????????
        this.setLabel(this.id);
        this.setName(this.id);
        // ?????????????????????????????????null
        if (aliases == null) aliases = new ArrayList<>();
        if (args == null) args = new ArrayList<>();
        if (permission == null) permission = "";
        if (worlds == null) worlds = new ArrayList<>();

        if (!this.permission.equalsIgnoreCase("")) this.setPermission(this.permission);
        this.setAliases(this.aliases);
        this.setPermissionMessage(LanguageManager.getString("permission-message").replace("%permission%", permission));
        this.setUsage(LanguageManager.getString(String.format("%s.usage", id)));
        this.setDescription(LanguageManager.getString(String.format("%s.description", id)));
    }
}
