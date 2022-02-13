package org.hcmc.hcplayground.scheduler;

import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.event.InventoryChangedEvent;

public class InventoryChangingRunnable extends BukkitRunnable {
    private static ItemStack lastItemStack;
    private final EquipmentSlot slot;
    private final InventoryAction action;
    private final Inventory inventory;
    private final JavaPlugin plugin;

    public InventoryChangingRunnable(Inventory inv, InventoryAction action, EquipmentSlot slot) {
        inventory = inv;
        this.action = action;
        this.slot = slot;
        plugin = HCPlayground.getPlugin();
    }

    @Override
    public void run() {
        getEquipmentSlotItem(inventory, slot);
    }

    private void getEquipmentSlotItem(Inventory inv, EquipmentSlot slot) {
        ItemStack isOffHand = ((PlayerInventory) inv).getItem(slot);
        if (isOffHand.equals(lastItemStack)) return;
        lastItemStack = isOffHand.clone();

        InventoryChangedEvent event = new InventoryChangedEvent(isOffHand, action, ((PlayerInventory) inv).getHolder());
        plugin.getServer().getPluginManager().callEvent(event);
    }
}
