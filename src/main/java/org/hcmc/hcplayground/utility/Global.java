package org.hcmc.hcplayground.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sk89q.worldguard.WorldGuard;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.util.Vector;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.*;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.config.AuthmeConfiguration;
import org.hcmc.hcplayground.model.config.CourseConfiguration;
import org.hcmc.hcplayground.model.config.PotionConfiguration;
import org.hcmc.hcplayground.model.enchantment.EnchantmentItem;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.menu.MenuItem;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.runnable.PluginRunnable;
import org.hcmc.hcplayground.serialization.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * @Desciption Store global static variants and methods
 */
public final class Global {
    private final static String[] childrenFolders;
    /**
     * ????????????????????????
     */
    private final static String[] ymlConfigurable;
    /**
     * ????????????????????????
     */
    private final static String[] ymlMigratable;
    private final static JavaPlugin plugin;

    private final static Type mapCharInteger = new TypeToken<Map<Character, Integer>>() {
    }.getType();
    private final static Type mapCharItemBase = new TypeToken<Map<Character, ItemBase>>() {
    }.getType();
    private final static Type listMenuItem=new TypeToken<List<MenuItem>>(){}.getType();

    public final static char CHAR_00A7 = '\u00a7';

    private final static String CONFIG_AUTHME = "authme";
    private final static String CONFIG_POTION = "potion";
    private final static String CONFIG_BAN_ITEM = "banitem";
    private final static String CONFIG_PARKOUR = "parkouradmin";
    private final static String FIELD_NAME_COMMANDMAP = "commandMap";
    private final static String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";

    private final static String FOLDER_PROFILE = "profile";
    private final static String FOLDER_DATABASE = "database";
    private final static String FOLDER_DESIGNER = "designer";
    private final static String FOLDER_STORAGE = "storage";
    private final static String FILE_CONFIG = "config.yml";
    private final static String FILE_ITEMS = "items.yml";
    private final static String FILE_DROPS = "drops.yml";
    private final static String FILE_MESSAGES = "messages.yml";
    private final static String FILE_LEVELS = "levels.yml";
    private final static String FILE_COMMANDS = "command.yml";
    private final static String FILE_MENU = "menu.yml";
    private final static String FILE_PERMISSION = "permission.yml";
    private final static String FILE_MOBS = "mobs.yml";
    private final static String FILE_BROADCAST = "broadcast.yml";
    private final static String FILE_CLEARLAG = "clearlag.yml";
    private final static String FILE_RECIPE = "recipe.yml";
    private final static String FILE_CCMD = "ccmd.yml";
    private final static String FILE_SIDEBAR = "scoreboard.yml";
    private final static String FILE_HOLOGRAM = "hologram.yml";
    private final static String FILE_MINION = "minion.yml";
    public final static String FILE_COURSE = "database/course.yml";
    //public final static String FILE_RECORD = "database/record.yml";
    public final static String FILE_RECORD_MINION = "database/minions.json";
    public final static String FILE_RECORD_BLOCK = "database/blocks.json";

    public static PluginRunnable runnable;
    public static Map<String, YamlConfiguration> yamlMap;

    public static Gson GsonObject;
    public static Scoreboard HealthScoreboard;
    public static CourseConfiguration course = null;
    public static AuthmeConfiguration authme = null;
    public static PotionConfiguration potion = null;
    public static Connection Sqlite = null;
    public static WorldGuard worldGuardApi = null;
    public static Economy economyApi = null;
    public static Chat chatApi = null;
    public static Permission permissionApi = null;
    public static CommandMap CommandMap = null;

    static {
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
        runnable = new PluginRunnable();
        yamlMap = new HashMap<>();
        HealthScoreboard = CreateScoreboard();
        childrenFolders = new String[]{
                FOLDER_DATABASE,
                FOLDER_DESIGNER,
                FOLDER_PROFILE,
                FOLDER_STORAGE,
        };
        ymlMigratable = new String[]{
                FILE_CLEARLAG,
                FILE_COMMANDS,
                FILE_CONFIG,
                FILE_MESSAGES,
                FILE_PERMISSION,
        };
        ymlConfigurable = new String[]{
                FILE_BROADCAST,
                FILE_CCMD,
                FILE_COURSE,
                FILE_DROPS,
                FILE_HOLOGRAM,
                FILE_ITEMS,
                FILE_LEVELS,
                FILE_MENU,
                FILE_MINION,
                FILE_MOBS,
                FILE_RECIPE,
                //FILE_RECORD,
                FILE_SIDEBAR,
        };

        GsonObject = new GsonBuilder()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(CcmdActionType.class, new CcmdActionTypeSerialization())
                .registerTypeAdapter(CrazyBlockType.class, new CrazyTypeSerialization())
                .registerTypeAdapter(Enchantment.class, new EnchantmentSerialization())
                .registerTypeAdapter(EnchantmentItem.class, new EnchantmentItemSerialization())
                .registerTypeAdapter(EntityType.class, new EntityTypeSerialization())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotSerialization())
                .registerTypeAdapter(GameMode.class, new GameModeSerialization())
                .registerTypeAdapter(InventoryType.class, new InventoryTypeSerialization())
                .registerTypeAdapter(ItemBase.class, new ItemBaseSerialization())
                .registerTypeAdapter(ItemFeatureType.class, new ItemFeatureTypeSerialization())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsSerialization())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerialization())
                .registerTypeAdapter(listMenuItem, new MenuItemListSerialization())
                .registerTypeAdapter(Location.class, new LocationSerialization())
                .registerTypeAdapter(mapCharInteger, new MapCharIntegerSerialization())
                .registerTypeAdapter(mapCharItemBase, new MapCharItemBaseSerialization())
                .registerTypeAdapter(Material.class, new MaterialSerialization())
                .registerTypeAdapter(MaterialData.class, new MaterialDataSerialization())
                .registerTypeAdapter(MinionCategory.class, new MinionCategorySerialization())
                .registerTypeAdapter(MinionPanelSlotType.class, new MinionPanelTypeSerialization())
                .registerTypeAdapter(MinionType.class, new MinionTypeSerialization())
                .registerTypeAdapter(NamespacedKey.class, new NamespacedKeySerialization())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectSerialization())
                .registerTypeAdapter(PermissionDefault.class, new PermissionDefaultSerialization())
                .registerTypeAdapter(RecipeType.class, new RecipeTypeSerialization())
                .registerTypeAdapter(Sound.class, new SoundSerialization())
                .serializeNulls()
                .setDateFormat(DATE_TIME_FORMAT)
                .setPrettyPrinting()
                .create();

        try {
            CommandMap = getCommandMap();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void ReloadConfiguration() throws IllegalAccessException, NoSuchFieldException, SQLException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        // ?????????????????????????????????
        InitialChildrenFolders();
        // ????????????Yml???????????????????????????
        // ?????????????????????????????????????????????????????????
        SaveYamlResource();
        // ???????????????????????????config.yml
        LoadConfig();
        // ???yml?????????????????????????????????????????????????????????????????????
        // 1.?????????????????????
        LanguageManager.Load(getYamlConfiguration(FILE_MESSAGES));
        // 2.??????????????????
        PermissionManager.Load(getYamlConfiguration(FILE_PERMISSION));
        // 3.????????????
        CommandManager.Load(getYamlConfiguration(FILE_COMMANDS));
        // 4.?????????????????????
        ItemManager.Load(getYamlConfiguration(FILE_ITEMS));
        // 5.?????????????????????????????????????????????????????????????????????
        DropManager.Load(getYamlConfiguration(FILE_DROPS));
        // 6.????????????????????????
        LevelManager.Load(getYamlConfiguration(FILE_LEVELS));
        // 7.??????????????????(??????)??????
        MenuManager.Load(getYamlConfiguration(FILE_MENU));
        // 8.????????????????????????????????????
        MobManager.Load(getYamlConfiguration(FILE_MOBS));
        // 9.??????????????????????????????
        BroadcastManager.Load(getYamlConfiguration(FILE_BROADCAST));
        // 10.??????????????????????????????
        ClearLagManager.Load(getYamlConfiguration(FILE_CLEARLAG));
        // 11.??????????????????
        RecipeManager.Load(getYamlConfiguration(FILE_RECIPE));
        // 12.????????????????????????
        CourseManager.Load(getYamlConfiguration(FILE_COURSE));
        // 13.???????????????????????????
        CcmdManager.Load(getYamlConfiguration(FILE_CCMD));
        // 14.???????????????????????????
        SidebarManager.Load(getYamlConfiguration(FILE_SIDEBAR));
        // 15.??????????????????????????????
        HologramManager.Load(getYamlConfiguration(FILE_HOLOGRAM));
        // 16.????????????????????????
        MinionManager.Load(getYamlConfiguration(FILE_MINION));
        // 99.?????????????????????????????????????????????
        //RecordManager.Load(getYamlConfiguration(FILE_RECORD));
        RecordManager.Load();
    }

    /**
     * ????????????????????????????????????????????????????????????BukkitRunnable?????????<br>
     * ?????????/reload???????????????????????????????????????????????????
     */
    public static void Dispose() throws SQLException, IOException, IllegalAccessException, InvalidConfigurationException {
        // ?????????????????????????????????
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData pd = PlayerManager.getPlayerData(player);
            pd.SaveConfig();
        }
        LogMessage("Online player data saved");
        // ????????????
        PlayerManager.purgePlayerData();
        LogMessage("Online player data purged");
        RecordManager.Save();
        LogMessage("Specific blocks saved");
        yamlMap.clear();
        // ??????sqlite?????????
        if (!Sqlite.isClosed()) Sqlite.close();
        LogMessage("Sqlite database closed");
        // ????????????runnable??????
        if (!runnable.isCancelled()) runnable.cancel();
        LogMessage("Global runnable thread stopped");
    }

    /**
     * ??????section???????????????????????????Gson?????????????????????T?????????????????????List&lt;T&gt;??????
     */
    @NotNull
    public static <T> List<T> SetItemList(ConfigurationSection section, Class<T> tClass) {
        Set<String> keys = section.getKeys(false);
        String ClassName = tClass.getSimpleName();
        List<T> list = new ArrayList<>();

        try {
            for (String s : keys) {
                ConfigurationSection itemSection = section.getConfigurationSection(s);
                if (itemSection == null) continue;
                String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '??');
                //System.out.println(value);

                T item = GsonObject.fromJson(value, tClass);
                Class<?> findClass = tClass;
                Field fieldId = null;
                while (findClass != null) {
                    Field[] fields = findClass.getDeclaredFields();
                    fieldId = Arrays.stream(fields).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
                    if (fieldId != null) break;
                    findClass = findClass.getSuperclass();
                }

                if (fieldId != null) {
                    fieldId.setAccessible(true);
                    fieldId.set(item, String.format("%s.%s", ClassName, s));
                }
                list.add(item);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * ??????yml?????????????????????????????????Gson?????????????????????T?????????????????????List&lt;T&gt;??????
     */
    @NotNull
    public static <T> List<T> SetItemList(YamlConfiguration yaml, Class<T> tClass) {
        Set<String> keys = yaml.getKeys(false);
        List<T> list = new ArrayList<>();

        try {
            for (String s : keys) {
                ConfigurationSection itemSection = yaml.getConfigurationSection(s);
                if (itemSection == null) continue;
                String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '??');
                //System.out.println(value);

                T item = GsonObject.fromJson(value, tClass);
                Field fieldId = Arrays.stream(tClass.getDeclaredFields()).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
                if (fieldId != null) {
                    fieldId.setAccessible(true);
                    fieldId.set(item, s);
                }
                list.add(item);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * ??????????????????WorldGuard??????
     */
    public static void ValidWorldGuardPlugin() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("WorldGuard");
        if (p == null) {
            LogWarning("WorldGuard not found :(");
        } else {
            worldGuardApi = WorldGuard.getInstance();
            LogMessage(String.format("Found WorldGuard, version: %s", p.getDescription().getVersion()));
        }
    }

    /**
     * ??????????????????Vault??????
     */
    public static void ValidVaultPlugin() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (p == null) {
            LogWarning("Vault not found :(");
        } else {
            SetVaultChat();
            SetVaultEconomy();
            SetVaultPermission();
            LogMessage(String.format("Found Vault, version: %s", p.getDescription().getVersion()));
        }
    }

    public static void ValidParkourPlugin() {
        Plugin pa = plugin.getServer().getPluginManager().getPlugin("Parkour");
        if (pa == null) {
            LogWarning("Parkour not found :(");
        } else {
            LogMessage(String.format("Found Parkour, version: %s", pa.getDescription().getVersion()));
        }
    }

    public static void InitialChildrenFolders() {

        File dataFolder = plugin.getDataFolder();

        if (!dataFolder.exists()) {
            boolean flag = dataFolder.mkdir();
        }

        for (String s : childrenFolders) {
            File f = new File(String.format("%s/%s", dataFolder, s));
            boolean flag = f.mkdir();
        }
    }

    /**
     * Get YamlConfiguration instance special by filename parameter
     *
     * @param filename The .yml format file without path
     * @return The instance of YamlConfiguration was loaded by filename parameter
     */
    public static YamlConfiguration getYamlConfiguration(String filename) {
        return yamlMap.get(filename);
    }

    /**
     * ???config.yml???????????????????????????
     */
    private static void LoadConfig() throws IllegalAccessException {
        String value;
        ConfigurationSection section;
        YamlConfiguration config = getYamlConfiguration(FILE_CONFIG);

        section = config.getConfigurationSection(CONFIG_AUTHME);
        if (section != null) {
            value = GsonObject.toJson(section.getValues(false));
            authme = GsonObject.fromJson(value, AuthmeConfiguration.class);
        }
        section = config.getConfigurationSection(CONFIG_POTION);
        if (section != null) {
            value = GsonObject.toJson(section.getValues(false));
            potion = GsonObject.fromJson(value, PotionConfiguration.class);
        }
        section = config.getConfigurationSection(CONFIG_BAN_ITEM);
        BanItemManager.Load(section);
        section = config.getConfigurationSection(CONFIG_PARKOUR);
        if (section != null) {
            value = GsonObject.toJson(section.getValues(false));
            course = GsonObject.fromJson(value, CourseConfiguration.class);
        }
    }

    /**
     * ????????????Yml???????????????????????????
     * ????????????Yml????????????
     * ????????????Map<String, YamlConfiguration>??????
     */
    public static void SaveYamlResource() throws IOException {

        yamlMap.clear();

        for (String s : ymlMigratable) {
            String ext = s.substring(s.length() - 3);
            if (!ext.equalsIgnoreCase("yml")) {
                plugin.saveResource(s, false);
            } else {
                YamlConfiguration yaml = MigrateConfiguration(s);
                yaml.save(String.format("%s/%s", plugin.getDataFolder(), s));
                yamlMap.put(s, yaml);
            }
        }

        for (String s : ymlConfigurable) {
            File f = new File(String.format("%s/%s", plugin.getDataFolder(), s));
            if (plugin.getResource(s) != null && !f.exists()) plugin.saveResource(s, false);

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(f);
            yamlMap.put(s, yaml);
        }
    }

    /**
     * ?????????????????????
     */
    public static String PluginName() {
        return plugin.getName();
    }

    /**
     * ???????????????????????????
     *
     * @param message ??????????????????
     */
    public static void LogMessage(String message) {
        plugin.getLogger().info(message);
    }

    public static void LogWarning(String message) {
        plugin.getLogger().log(Level.WARNING, message);
    }

    public static String getDateFormat(Date date, int format, Locale locale) {
        String dateFormat;

        DateFormat df = DateFormat.getDateInstance(format, locale);
        DateFormat tf = DateFormat.getTimeInstance(format, locale);
        dateFormat = String.format("%s %s", df.format(date), tf.format(date));

        return dateFormat;
    }

    public static Location LookAt(Location source, Location target) {
        Vector v = source.toVector().subtract(target.toVector());
        Location _s = source.clone();

        double x = v.getX();
        double y = v.getY();
        double z = v.getZ();

        double dXZ = Math.sqrt(x * x + z * z);
        double dY = Math.sqrt(dXZ * dXZ + y * y);

        double newYaw = Math.acos(x / dXZ) * 180 / Math.PI;
        double newPitch = Math.acos(y / dY) * 180 / Math.PI - 90;
        if (z < 0.0)
            newYaw = newYaw + Math.abs(180 - newYaw) * 2;
        newYaw = (newYaw - 90);

        _s.setYaw((float) newYaw);
        _s.setPitch((float) newPitch);

        return _s;
    }

    private static void SetVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        economyApi = rsp.getProvider();
    }

    private static void SetVaultChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return;

        chatApi = rsp.getProvider();
    }

    private static void SetVaultPermission() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return;

        permissionApi = rsp.getProvider();
    }

    @NotNull
    private static YamlConfiguration MigrateConfiguration(String filename) {
        YamlConfiguration yamlResource, yamlPlugin;

        InputStream stream = plugin.getResource(filename);
        if (stream == null) {
            yamlResource = new YamlConfiguration();
        } else {
            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            yamlResource = YamlConfiguration.loadConfiguration(reader);
        }

        File f = new File(String.format("%s/%s", plugin.getDataFolder(), filename));
        yamlPlugin = YamlConfiguration.loadConfiguration(f);

        Set<String> keysResource = yamlResource.getKeys(true);
        Set<String> keysPlugin = yamlPlugin.getKeys(true);

        for (String key : keysResource) {
            List<String> comments = yamlResource.getComments(key);
            Object obj = yamlResource.get(key);

            String exist = keysPlugin.stream().filter(x -> x.equalsIgnoreCase(key)).findAny().orElse(null);
            if (exist == null) {
                yamlPlugin.set(key, obj);
                yamlPlugin.setComments(key, comments);
            }
        }
        return yamlPlugin;
    }

    private static CommandMap getCommandMap() throws NoSuchFieldException, IllegalAccessException {
        // ??????Bukkit.Server.CommandMap??????
        Field fieldCommandMap = plugin.getServer().getClass().getDeclaredField(FIELD_NAME_COMMANDMAP);
        // ??????CommandMap??????????????????
        fieldCommandMap.setAccessible(true);
        // ???CommandMap????????????CommandMap??????
        return (CommandMap) fieldCommandMap.get(plugin.getServer());
    }

    private static Scoreboard CreateScoreboard() {
        ScoreboardManager sm = plugin.getServer().getScoreboardManager();
        if (sm == null) return null;

        Scoreboard scoreboard = sm.getNewScoreboard();
        Set<Objective> objectives = scoreboard.getObjectives();
        for (Objective o : objectives) {
            o.unregister();
        }
        return scoreboard;
    }
}
