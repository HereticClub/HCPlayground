package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.permission.PermissionItem;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;

import java.io.IOException;
import java.util.*;

public class PermissionManager {

    private static final JavaPlugin plugin = HCPlayground.getPlugin();
    private static final PluginManager pluginManager = plugin.getServer().getPluginManager();
    public static List<PermissionItem> Permissions = new ArrayList<>();

    public static final String PERMISSION_PARKOUR_BASIC_KIT = "Parkour.Basic.Kit";
    public static final String PERMISSION_PARKOUR_BASIC_CREATE = "Parkour.Basic.Create";
    public static final String PERMISSION_WORLDGUARD_REGION_BYPASS = "worldguard.region.bypass.%s";

    public PermissionManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        ConfigurationSection section = yaml.getConfigurationSection("permissions");
        if (section == null) return;
        Permissions = Global.SetItemList(section, PermissionItem.class);

        Set<Permission> bukkitPerms = pluginManager.getPermissions();
        for (Permission p : bukkitPerms) {
            pluginManager.removePermission(p);
        }

        for (PermissionItem p : Permissions) {
            Map<String, Boolean> children = new HashMap<>();
            for (String child : p.children) {
                children.put(child, true);
            }

            Permission bukkitPerm = new Permission(p.name, p.description, p.defaultTo, children);
            pluginManager.addPermission(bukkitPerm);
        }
    }

    public static void addPermission(Player player, String permission) throws IOException, IllegalAccessException, InvalidConfigurationException {
        PlayerData data = PlayerManager.getPlayerData(player);
        data.attachment.setPermission(permission, true);

    }

    public static void removePermission(Player player, String permission) throws IOException, IllegalAccessException, InvalidConfigurationException {
        PlayerData data = PlayerManager.getPlayerData(player);
        data.attachment.unsetPermission(permission);
    }
}
