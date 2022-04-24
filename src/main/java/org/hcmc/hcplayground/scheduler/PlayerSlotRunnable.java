package org.hcmc.hcplayground.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.event.PlayerEquipmentChangedEvent;
import org.w3c.dom.events.Event;

import java.util.HashMap;
import java.util.Map;

public class PlayerSlotRunnable extends BukkitRunnable {

    private final Player player;

    public PlayerSlotRunnable(Player player) {
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
