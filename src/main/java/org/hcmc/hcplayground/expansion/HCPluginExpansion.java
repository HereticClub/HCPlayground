package org.hcmc.hcplayground.expansion;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HCPluginExpansion extends PlaceholderExpansion {
    private final PluginDescriptionFile pluginDescription;


    private final static String HC_PLACEHOLDER_HEALTH = "health";
    private final static String HC_PLACEHOLDER_DAMAGE = "damage";
    private final static String HC_PLACEHOLDER_ARMOR = "armor";
    private final static String HC_PLACEHOLDER_ATTACK_SPEED = "attackSpeed";
    private final static String HC_PLACEHOLDER_WORK_SPEED = "workSpeed";
    private final static String HC_PLACEHOLDER_CRITICAL = "critical";
    private final static String HC_PLACEHOLDER_CRITICAL_DAMAGE = "criticalDamage";
    private final static String HC_PLACEHOLDER_INTELLIGENCE = "intelligence";
    private final static String HC_PLACEHOLDER_DIGGING_SPEED = "diggingSpeed";


    public HCPluginExpansion() {
        Plugin plugin = HCPlayground.getPlugin();
        pluginDescription = plugin.getDescription();
    }

    public static void RegisterExpansion() {
        HCPluginExpansion expansion = new HCPluginExpansion();
        if (!expansion.isRegistered()) expansion.register();
        Global.LogMessage("Hook into PlaceholderApi successful.");
    }

    public static void UnregisterExpansion() {
        HCPluginExpansion expansion = new HCPluginExpansion();
        if (expansion.isRegistered()) expansion.unregister();
        Global.LogMessage("Unregister from PlaceholderApi.");
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return pluginDescription.getName().toLowerCase();
    }

    @Override
    @NotNull
    public String getAuthor() {
        List<String> authors = pluginDescription.getAuthors();
        String value = "";
        for (String s : authors) {
            value = String.format("%s%s,", value, s);
        }
        if (!value.isEmpty()) value = value.substring(0, value.length() - 1);
        return value;
    }

    @Override
    @NotNull
    public String getVersion() {
        return pluginDescription.getVersion();
    }

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin(getRequiredPlugin()) != null;
    }

    @Override
    @NotNull
    public String getRequiredPlugin() {
        return pluginDescription.getName();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase(HC_PLACEHOLDER_HEALTH)) {
            return getPlayerHealth(player);
        }
        if (params.equalsIgnoreCase(HC_PLACEHOLDER_ARMOR)) {
            return getPlayerArmor(player);
        }
        return null;
    }

    private String getPlayerHealth(OfflinePlayer offlinePlayer) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "Health Unknown";

        double health = player.getHealth();

        return String.valueOf(health);
    }

    private String getPlayerArmor(OfflinePlayer offlinePlayer){
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "Health Armor";

        return "";
    }
}
