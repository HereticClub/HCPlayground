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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.deserializer.*;
import org.hcmc.hcplayground.itemManager.ItemBase;
import org.hcmc.hcplayground.playerManager.PlayerData;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.logging.Level;

/**
 * @Desciption Store global static variants and methods
 */
public final class Global {

    private final static Map<String, YamlConfiguration> yamlMap;
    private final static String[] ymlFilenames;
    private final static JavaPlugin plugin;

    public final static String PERSISTENT_MAIN_KEY = "hccraft";
    public final static String PERSISTENT_SUB_KEY = "content";
    public final static String PERSISTENT_CRIT_KEY = "crit";

    public static Map<UUID, PlayerData> playerMap;
    public static WorldGuard WorldGuardApi = null;
    public static YamlConfiguration yamlPlayer = null;
    public static Economy EconomyApi = null;
    public static Chat ChatApi = null;
    public static Permission PermissionApi = null;

    static {
        plugin = JavaPlugin.getPlugin(HCPlayground.class);
        ymlFilenames = new String[]{"config.yml", "items.yml", "drops.yml"};
        yamlMap = new HashMap<>();
        playerMap = new HashMap<>();
    }

    /**
     * 获取section内所有子节段，利用Gson反序列到每一个T对象，然后返回List&lt;T&gt;数组
     */
    public static <T> List<T> SetItemList(ConfigurationSection section, Class<T> tClass) throws IllegalAccessException {
        Set<String> keys = section.getKeys(false);
        String ClassName = tClass.getSimpleName();
        List<T> list = new ArrayList<>();

        Gson gson = new GsonBuilder()
                .enableComplexMapKeySerialization()
                .disableHtmlEscaping()
                .serializeNulls()
                .registerTypeAdapter(Material.class, new MaterialDeserializer())
                .registerTypeAdapter(ItemFlag.class, new ItemFlagsDeserializer())
                .registerTypeAdapter(EquipmentSlot.class, new EquipmentSlotDeserializer())
                .registerTypeAdapter(PotionEffect.class, new PotionEffectDeserializer())
                .registerTypeAdapter(ItemBase.class, new ItemBaseDeserializer())
                .excludeFieldsWithoutExposeAnnotation()
                .create();

        for (String s : keys) {
            ConfigurationSection itemSection = section.getConfigurationSection(s);
            if (itemSection == null) continue;
            String value = gson.toJson(itemSection.getValues(false)).replace('&', '§');

            T item = gson.fromJson(value, tClass);
            Field fieldId = Arrays.stream(tClass.getFields()).filter(x -> x.getName().equalsIgnoreCase("id")).findAny().orElse(null);
            if (fieldId != null) fieldId.set(item, String.format("%s.%s", ClassName, s));
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
