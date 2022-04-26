package org.hcmc.hcplayground.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.event.PlayerEquipmentChangedEvent;

public class EquipmentMonitorRunnable extends BukkitRunnable {

    private final Player player;

    public EquipmentMonitorRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        getPlayerEquipments(player);
    }

    private void getPlayerEquipments(Player player) {
        PlayerEquipmentChangedEvent event = new PlayerEquipmentChangedEvent(player);
        Bukkit.getPluginManager().callEvent(event);
    }
}
