package org.hcmc.hcplayground.manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.NameBinaryTag;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
     * 从各自玩家的json格式文档中加载玩家档案数据<br>
     * 玩家的json档案命名由玩家的uuid决定
     * @param player 玩家实例
     * @return 玩家数据实例
     */
    @NotNull
    public static PlayerData LoadConfig(@NotNull Player player) {
        try {
            UUID playerUuid = player.getUniqueId();
            File f = new File(plugin.getDataFolder(), String.format("profile/%s.json", playerUuid));
            if (!Files.exists(f.toPath())) return new PlayerData(player);

            String value = Files.readString(f.toPath());
            PlayerData data = Global.GsonObject.fromJson(value, PlayerData.class);
            data.initialize(player);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return new PlayerData(player);
        }
    }

    public static void SaveConfig(Player player) {
        try {
            PlayerData data = getPlayerData(player);
            UUID playerUuid = player.getUniqueId();
            File f = new File(plugin.getDataFolder(), String.format("profile/%s.json", playerUuid));

            String value = Global.GsonObject.toJson(data, PlayerData.class);
            Files.writeString(f.toPath(), value);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        PlayerData data = mapPlayerData.get(playerUuid);

        if (data == null) {
            data = LoadConfig(player);
            // Don't delete the code as below
            //Global.LogMessage(String.format("\033[1;35mgetPlayerData GameMode: \033[1;33m%s\033[0m", player.getGameMode()));
            mapPlayerData.put(playerUuid, data);
        }

        return data;
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

    /**
     * 清空所有玩家数据
     */
    public static void purgePlayerData() {
        mapPlayerData.clear();
    }
}
