package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.NameBinaryTagResolver;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class PlayerManager {
    private static final Map<UUID, PlayerData> mapPlayerData;

    static {
        mapPlayerData = new HashMap<>();
    }

    private static final Plugin plugin = HCPlayground.getInstance();

    public PlayerManager() {

    }

    /**
     * 获取实体玩家的所有配置信息
     *
     * @param player 实体玩家实例
     * @return 该实体玩家的配置信息实例
     */
    @NotNull
    public static PlayerData getPlayerData(@NotNull Player player) {
        UUID playerUuid = player.getUniqueId();
        PlayerData pd = mapPlayerData.get(playerUuid);

        if (pd == null) {
            pd = new PlayerData(player);
            //Global.LogMessage(String.format("\033[1;35mgetPlayerData GameMode: \033[1;33m%s\033[0m", player.getGameMode()));
            mapPlayerData.put(playerUuid, pd);
        }

        return pd;
    }

    /**
     * 推送玩家配置信息到一个缓存列表
     *
     * @param player Minecraft的玩家实例
     * @param data   玩家的配置信息实例
     */
    public static void setPlayerData(@NotNull Player player, PlayerData data) {
        UUID playerUuid = player.getUniqueId();
        //Global.LogMessage(String.format("\033[1;35msetPlayerData GameMode: \033[1;33m%s\033[0m", data.GameMode));
        mapPlayerData.put(playerUuid, data);
    }

    public static void removePlayerData(@NotNull Player player, PlayerData data) {
        UUID playerUuid = player.getUniqueId();

        player.removeAttachment(data.getAttachment());
        data.getAttachment().remove();

        //Global.LogMessage(String.format("\033[1;35mremovePlayerData GameMode: \033[1;33m%s\033[0m", data.GameMode));
        mapPlayerData.remove(playerUuid, data);
    }

    public static void purgePlayerData() {
        mapPlayerData.clear();
    }

    public static void getEquipmentData(Player player, ItemStack[] itemStacks) throws IllegalAccessException, IOException, InvalidConfigurationException {
        double armor = PlayerData.BASE_ARMOR;
        double attackReach = PlayerData.BASE_ATTACK_REACH;
        double bloodSucking = PlayerData.BASE_BLOOD_SUCKING;
        double critical = PlayerData.BASE_CRITICAL;
        double criticalDamage = PlayerData.BASE_CRITICAL_DAMAGE;
        double recover = PlayerData.BASE_RECOVER;
        double intelligence = PlayerData.BASE_INTELLIGENCE;
        double diggingSpeed = PlayerData.BASE_DIGGING_SPEED;
        double loggingSpeed = PlayerData.BASE_LOGGING_SPEED;

        // 检查玩家的装备栏和副手物品
        for (ItemStack is : itemStacks) {
            // 忽略没有装备的物品
            if (is == null) continue;
            // 忽略没有ItemMeta的物品
            ItemMeta im = is.getItemMeta();
            if (im == null) continue;
            // 获取玩家身上所有装备和副手物品的额外数值
            NameBinaryTagResolver nbt = new NameBinaryTagResolver(is);
            // Minecraft直接支持的玩家基本属性
            armor += nbt.getFloatValue(ItemBase.PERSISTENT_ARMOR_KEY);
            // Minecraft不支持的玩家基本属性
            recover += nbt.getFloatValue(ItemBase.PERSISTENT_RECOVER_KEY);
            bloodSucking += nbt.getFloatValue(ItemBase.PERSISTENT_BLOOD_SUCKING_KEY);
            critical += nbt.getFloatValue(ItemBase.PERSISTENT_CRITICAL_KEY);
            criticalDamage += nbt.getFloatValue(ItemBase.PERSISTENT_CRITICAL_DAMAGE_KEY);
            // Minecraft实验性玩家基本属性，当前版本还不支持
            attackReach += nbt.getFloatValue(ItemBase.PERSISTENT_ATTACK_REACH_KEY);
            intelligence += nbt.getFloatValue(ItemBase.PERSISTENT_INTELLIGENCE);
            diggingSpeed += nbt.getFloatValue(ItemBase.PERSISTENT_DIGGING_SPEED);
            loggingSpeed += nbt.getFloatValue(ItemBase.PERSISTENT_LOGGING_SPEED);
        }
        PlayerData data = getPlayerData(player);
        data.setTotalArmor(armor);
        data.setTotalAttackReach(attackReach);
        data.setTotalBloodSucking(bloodSucking);
        data.setTotalCritical(critical);
        data.setTotalCriticalDamage(criticalDamage);
        data.setTotalRecover(recover);
        data.setTotalIntelligence(intelligence);
        data.setTotalDiggingSpeed(diggingSpeed);
        data.setTotalLoggingSpeed(loggingSpeed);
        setPlayerData(player, data);
        /*
        TODO: 在后续的版本需要实施血量压缩显示
        */
    }
}
