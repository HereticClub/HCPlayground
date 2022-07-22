package org.hcmc.hcplayground.expansion;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class HCPluginExpansion extends PlaceholderExpansion {
    private final PluginDescriptionFile pluginDescription;

    public HCPluginExpansion() {
        Plugin plugin = HCPlayground.getInstance();
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
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "Unknown Player";

        PlayerData data = PlayerManager.getPlayerData(player);

        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_ARMOR_KEY))
            return String.format("%.1f", data.getTotalArmor());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_ARMOR_TOUGHNESS_KEY))
            return String.format("%.1f", data.getTotalArmorToughness());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_ATTACK_DAMAGE_KEY))
            return String.format("%.1f", data.getTotalAttackDamage());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_ATTACK_REACH_KEY))
            return String.format("%.1f", data.getTotalAttackReach());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_ATTACK_SPEED_KEY))
            return String.format("%.1f", data.getTotalAttackSpeed());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_BLOOD_SUCKING_KEY))
            return String.format("%.1f", data.getTotalBloodSucking());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_CRITICAL_KEY))
            return String.format("%.1f", data.getTotalCritical());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_CRITICAL_PERCENTAGE_KEY))
            return String.format("%.1f%%", data.getTotalCritical() * 100);
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_CRITICAL_DAMAGE_KEY))
            return String.format("%.1f", data.getTotalCriticalDamage());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_CRITICAL_DAMAGE_PERCENTAGE_KEY))
            return String.format("%.1f%%", data.getTotalCriticalDamage() * 100);
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_HEALTH_KEY))
            return String.format("%.1f", data.getCurrentHealth());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_MAX_HEALTH_KEY))
            return String.format("%.1f", data.getMaxHealth());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_KNOCKBACK_RESISTANCE_KEY))
            return String.format("%.1f", data.getTotalKnockBackResistance());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_LUCK_KEY))
            return String.format("%.1f", data.getTotalLuck());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_MOVEMENT_SPEED_KEY))
            return String.format("%.1f", data.getTotalMovementSpeed());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_RECOVER_KEY))
            return String.format("%.1f", data.getTotalRecover());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_INTELLIGENCE))
            return String.format("%.1f", data.getTotalIntelligence());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_DIGGING_SPEED))
            return String.format("%.1f", data.getTotalDiggingSpeed());
        if (params.equalsIgnoreCase(ItemBase.PERSISTENT_LOGGING_SPEED))
            return String.format("%.1f", data.getTotalLoggingSpeed());

        return null;
    }
}
