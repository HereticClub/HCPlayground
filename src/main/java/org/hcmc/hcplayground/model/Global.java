package org.hcmc.hcplayground.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldguard.WorldGuard;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.deserializer.*;
import org.hcmc.hcplayground.itemManager.ItemBaseA;
import org.hcmc.hcplayground.playerManager.PlayerData;
import org.hcmc.hcplayground.sqlite.SqliteManager;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @Desciption Store global static variants and methods
 */
public final class Global {
    private final static String[] ymlFilenames;
    private final static JavaPlugin plugin;

    public final static String PERSISTENT_MAIN_KEY = "hccraft";
    public final static String PERSISTENT_SUB_KEY = "content";
    public final static String PERSISTENT_CRIT_KEY = "crit";
    public final static Pattern patternNumber = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static Map<String, YamlConfiguration> yamlMap;
    public static Map<UUID, PlayerData> playerMap;
    public static Gson GsonObject;
    public static SqliteManager Sqlite = null;
    public static WorldGuard WorldGuardApi = null;
    public static Economy EconomyApi = null;
    public static Chat ChatApi = null;
    public static Permission PermissionApi = null;

    static {
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
        yamlMap = new HashMap<>();
        playerMap = new HashMap<>();
        ymlFilenames = new String[]{
                "config.yml",
                "items.yml",
                "drops.yml",
                "messages.yml",
                "levels.yml",
                "command.yml",
                "inventoryTemplate.yml",
                "permission.yml",
                "database/hcdb.db",
        };

        GsonObject = new GsonBuilder()
                .disableHtmlEscaping()
                .enableComplexMapKeySerialization()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
                .registerTypeAdapter(InventoryType.class, new InventoryTypeDeserializer())
                .registerTypeAdapter(ItemBaseA.class, new ItemBaseDeserializer())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsDeserializer())
                .registerTypeAdapter(Material.class, new MaterialDeserializer())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectDeserializer())
                .registerTypeAdapter(PermissionDefault.class, new PermissionDefaultDeserializer())
                .serializeNulls()
                .setPrettyPrinting()
                .create();
    }

    /**
     * 清理所有正在执行的对象，特别是所有继承于BukkitRunnable的对象<br>
     * 在执行/reload指令或者插件退出时都需要执行该方法
     */
    public static void Dispose() throws SQLException {
        Set<UUID> uuids = playerMap.keySet();
        for (UUID uuid : uuids) {
            PlayerData data = playerMap.get(uuid);
            if (data == null) continue;

            data.CancelPotionTimer();
        }
        Sqlite.Disconnect();
        playerMap.clear();
        yamlMap.clear();
    }

    /**
     * 获取section内所有子节段，利用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
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
    public static <T> List<T> SetItemList(YamlConfiguration yaml, Class<T> tClass) throws IllegalAccessException {
        Set<String> keys = yaml.getKeys(false);
        List<T> list = new ArrayList<>();

        for (String s : keys) {
            ConfigurationSection itemSection = yaml.getConfigurationSection(s);
            if (itemSection == null) continue;
            String value = GsonObject.toJson(itemSection.getValues(false)).replace('&', '§');
            //System.out.println(value);

            T item = GsonObject.fromJson(value, tClass);
            Field fieldId = Arrays.stream(tClass.getFields()).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
            if (fieldId != null) fieldId.set(item, s);
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
            plugin.getLogger().log(Level.WARNING, "WorldGuard not found :(");
        } else {
            WorldGuardApi = WorldGuard.getInstance();
            plugin.getLogger().info(String.format("Found WorldGuard, version: %s", p.getDescription().getVersion()));
        }
    }

    /**
     * 验证并且注册Vault插件
     */
    public static void ValidVaultPlugin() {
        Plugin p = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (p == null) {
            plugin.getLogger().log(Level.WARNING, "Vault not found :(");
        } else {
            SetVaultChat();
            SetVaultEconomy();
            SetVaultPermission();
            plugin.getLogger().info(String.format("Found Vault, version: %s", p.getDescription().getVersion()));
        }
    }

    /**
     * 复制所有Yml资源文档到插件目录
     * 加载所有Yml资源文档
     * 创建全局Map<String, YamlConfiguration>对象
     */
    public static void SaveYamlResource() {

        yamlMap.clear();

        for (String s : ymlFilenames) {
            File f = new File(plugin.getDataFolder(), s);
            if (!f.exists()) {
                LogMessage(String.format("Copying %s ......", f.getName()));
                plugin.saveResource(f.getName(), false);
            }

            yamlMap.put(s, YamlConfiguration.loadConfiguration(f));
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

    private static void SetVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        EconomyApi = rsp.getProvider();
    }

    private static void SetVaultChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return;

        ChatApi = rsp.getProvider();
    }

    private static void SetVaultPermission() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return;

        PermissionApi = rsp.getProvider();
    }
}
