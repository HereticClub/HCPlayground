package org.hcmc.hcplayground.runnable;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.player.PlayerData;

/**
 * 要获取玩家身上装备的数值<br>
 * 必须在更换装备后的下一tick才能获取<br>
 * 因此必须使用BukkitRunnable类来执行装备更换的监测<br>
 */
public class EquipmentMonitorRunnable extends BukkitRunnable {

    private final Player player;

    public EquipmentMonitorRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        PlayerData data = PlayerManager.getPlayerData(player);
        data.getExtraDataInEquipments();
    }
}
