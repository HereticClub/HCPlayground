package org.hcmc.hcplayground.playerManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.hcmc.hcplayground.scheduler.PotionEffectRunnable;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
    /*
    BreakList - 玩家破坏方块的数据
    PlaceList - 玩家放置方块的数据
    Key:
    如果是普通方块比如麦子等
    则保存保存该方块的Material
    如果含有PersistentData，则保存其Id
    Value:
    破快或放置该方块的总数量
    */
    public Map<String, Integer> BreakList = new HashMap<>();
    public Map<String, Integer> PlaceList = new HashMap<>();

    private final Player player;

    public PotionEffectRunnable PotionRunnable;
    public BukkitTask BukkitTask;

    public PlayerData(Player player) {
        this.player = player;
        this.PotionRunnable = new PotionEffectRunnable(player);
    }

    public void RunPotionTimer(JavaPlugin plugin, long delay, long period) {
        this.BukkitTask = PotionRunnable.runTaskTimer(plugin, delay, period);
    }
}
