package org.hcmc.hcplayground.event;


import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class PlayerEquipmentChangedEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final Player player;
    private Map<EquipmentSlot, ItemStack> equipments;

    public PlayerEquipmentChangedEvent(Player player) {
        this.player = player;
        setEquipments();
    }

    public Map<EquipmentSlot, ItemStack> getEquipments() {
        return equipments;
    }

    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    private void setEquipments() {
        equipments = new HashMap<>();
        PlayerInventory inv = player.getInventory();
        equipments.put(EquipmentSlot.HEAD, inv.getHelmet());
        equipments.put(EquipmentSlot.CHEST, inv.getChestplate());
        equipments.put(EquipmentSlot.LEGS, inv.getLeggings());
        equipments.put(EquipmentSlot.FEET, inv.getBoots());
        equipments.put(EquipmentSlot.OFF_HAND, inv.getItemInOffHand());
    }
}
