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
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.CcmdActionType;
import org.hcmc.hcplayground.enums.CrazyBlockType;
import org.hcmc.hcplayground.enums.ItemFeatureType;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.manager.BanItemManager;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.manager.RecordManager;
import org.hcmc.hcplayground.model.config.AuthmeConfiguration;
import org.hcmc.hcplayground.model.config.CourseConfiguration;
import org.hcmc.hcplayground.model.config.PotionConfiguration;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @Desciption Store global static variants and methods
 */
public final class Global {
    private final static String[] childrenFolders;
    private final static String[] ymlConfigurable;
    private final static String[] ymlMigratable;
    private final static JavaPlugin plugin;
    private final static Type mapCharInteger = new TypeToken<Map<Character, Integer>>() {
    }.getType();
    private final static Type mapCharItemBase = new TypeToken<Map<Character, ItemBase>>() {
    }.getType();
    private final static Type listMenuItem=new TypeToken<List<MenuItem>>(){}.getType();

    public final static String CONFIG_AUTHME = "authme";
    public final static String CONFIG_POTION = "potion";
    public final static String CONFIG_BAN_ITEM = "banitem";
    public final static String CONFIG_PARKOUR = "parkouradmin";
    public final static String FIELD_NAME_COMMANDMAP = "commandMap";
    public final static String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public final static Pattern patternNumber = Pattern.compile("-?\\d+(\\.\\d+)?");

    public final static String FILE_CONFIG = "config.yml";
    public final static String FILE_ITEMS = "items.yml";
    public final static String FILE_DROPS = "drops.yml";
    public final static String FILE_MESSAGES = "messages.yml";
    public final static String FILE_LEVELS = "levels.yml";
    public final static String FILE_COMMANDS = "command.yml";
    public final static String FILE_MENU = "menu.yml";
    public final static String FILE_PERMISSION = "permission.yml";
    public final static String FILE_MOBS = "mobs.yml";
    public final static String FILE_BROADCAST = "broadcast.yml";
    public final static String FILE_CLEARLAG = "clearlag.yml";
    public final static String FILE_RECIPE = "recipe.yml";
    public final static String FILE_CCMD = "ccmd.yml";
    public final static String FILE_SCOREBOARD = "scoreboard.yml";
    public final static String FILE_HOLOGRAM = "hologram.yml";
    public final static String FILE_COURSE = "database/course.yml";
    public final static String FILE_RECORD = "database/record.yml";
    public final static String FOLDER_PROFILE = "profile";
    public final static String FOLDER_DATABASE = "database";
    public final static String FOLDER_DESIGNER = "designer";
    public final static String FOLDER_STORAGE = "storage";

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
                FILE_MOBS,
                FILE_RECIPE,
                FILE_RECORD,
                FILE_SCOREBOARD,
        };

        GsonObject = new GsonBuilder()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(CcmdActionType.class, new CcmdActionTypeSerialization())
                .registerTypeAdapter(CrazyBlockType.class, new CrazyTypeSerialization())
                .registerTypeAdapter(Enchantment.class, new EnchantmentSerialization())
                .registerTypeAdapter(EntityType.class, new EntityTypeSerialization())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotSerialization())
                .registerTypeAdapter(GameMode.class, new GameModeSerialization())
                .registerTypeAdapter(InventoryType.class, new InventoryTypeSerialization())
                .registerTypeAdapter(ItemBase.class, new ItemBaseSerialization())
                .registerTypeAdapter(ItemFeatureType.class, new ItemFeatureTypeSerialization())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsSerialization())
                .registerTypeAdapter(ItemStack.class, new ItemStackSerialization())
                .registerTypeAdapter(Location.class, new LocationSerialization())
                .registerTypeAdapter(mapCharInteger, new MapCharIntegerSerialization())
                .registerTypeAdapter(mapCharItemBase, new MapCharItemBaseSerialization())
                .registerTypeAdapter(MaterialData.class, new MaterialDataSerialization())
                .registerTypeAdapter(listMenuItem, new MenuItemListSerialization())
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

    /**
     * 清理所有正在执行的对象，特别是所有继承于BukkitRunnable的对象<br>
     * 在执行/reload指令或者插件退出时都需要执行该方法
     */
    public static void Dispose() throws SQLException, IOException, IllegalAccessException, InvalidConfigurationException {
        // 保存所有在线玩家的数据
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            PlayerData pd = PlayerManager.getPlayerData(player);
            pd.SaveConfig();
        }
        // 清除内存
        PlayerManager.clearAllPlayerData();
        RecordManager.saveCrazyRecord();
        yamlMap.clear();
        // 关闭sqlite数据库
        if (!Sqlite.isClosed()) Sqlite.close();
        // 停止所有runnable线程
        if (!runnable.isCancelled()) runnable.cancel();
    }

    /**
     * 从config.yml加载插件的基本设置
     */
    public static void LoadConfig() throws IllegalAccessException {
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

            String world = course.getWorld();
            World w = Bukkit.getWorld(world);
            if (w == null) {
                WorldCreator creator = new WorldCreator(world);
                Bukkit.createWorld(creator);
            }

            if(Bukkit.getWorld("survival_world") == null){
                WorldCreator creator = new WorldCreator("survival_world");
                Bukkit.createWorld(creator);
            }
        }
    }

    /**
     * 获取section内所有子节段，利用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    @NotNull
    public static <T> List<T> SetItemList(ConfigurationSection section, Class<T> tClass) throws IllegalAccessException {
        Set<String> keys = section.getKeys(false);
        String ClassName = tClass.getSimpleName();
        List<T> list = new ArrayList<>();

        for (String s : keys) {
            ConfigurationSection itemSection = section.getConfigurationSection(s);
            if (itemSection == null) continue;
            String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
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

        return list;
    }

    /**
     * 获取yml文档内所有子节段，利用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    @NotNull
    public static <T> List<T> SetItemList(YamlConfiguration yaml, Class<T> tClass) throws IllegalAccessException {
        Set<String> keys = yaml.getKeys(false);
        List<T> list = new ArrayList<>();

        for (String s : keys) {
            ConfigurationSection itemSection = yaml.getConfigurationSection(s);
            if (itemSection == null) continue;
            String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
            //System.out.println(value);

            T item = GsonObject.fromJson(value, tClass);
            Field fieldId = Arrays.stream(tClass.getDeclaredFields()).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
            if (fieldId != null) {
                fieldId.setAccessible(true);
                fieldId.set(item, s);
            }
            list.add(item);
        }

        return list;
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
     * 验证并且注册WorldGuard插件
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
     * 验证并且注册Vault插件
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
     * 复制所有Yml资源文档到插件目录
     * 加载所有Yml资源文档
     * 创建全局Map<String, YamlConfiguration>对象
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
     * 获取本插件名称
     */
    public static String PluginName() {
        return plugin.getName();
    }

    /**
     * 写记录信息到记录器
     *
     * @param message 要记录的信息
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
        // 获取Bukkit.Server.CommandMap字段
        Field fieldCommandMap = plugin.getServer().getClass().getDeclaredField(FIELD_NAME_COMMANDMAP);
        // 设置CommandMap字段为可访问
        fieldCommandMap.setAccessible(true);
        // 从CommandMap字段获取CommandMap实例
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
