package org.hcmc.hcplayground.scheduler;

import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class PluginRunnable extends BukkitRunnable {

    private ScheduleType scheduleType;
    private Inventory inventory;
    private ItemStack isOffHand;

    public PluginRunnable(Inventory inv, ScheduleType type) {
        inventory = inv;
        scheduleType = type;
    }

    public ItemStack getIsOffHand() {
        return isOffHand;
    }

    @Override
    public void run() {
        if (scheduleType == ScheduleType.GetOffHandItem) getOffHandItem(inventory);
    }

    private void getOffHandItem(Inventory inv) {
        isOffHand = ((PlayerInventory) inv).getItem(EquipmentSlot.OFF_HAND);
    }

    public enum ScheduleType {
        GetOffHandItem;
    }
}
