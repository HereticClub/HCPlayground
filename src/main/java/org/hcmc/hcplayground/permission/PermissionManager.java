package org.hcmc.hcplayground.permission;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.Global;

import java.util.*;

public class PermissionManager {

    private static final JavaPlugin plugin = HCPlayground.getPlugin();
    private static final PluginManager pluginManager = plugin.getServer().getPluginManager();

    public static List<PermissionItem> Permissions = new ArrayList<>();

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
}
