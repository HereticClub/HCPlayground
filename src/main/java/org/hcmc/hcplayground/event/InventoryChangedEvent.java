package org.hcmc.hcplayground.event;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class InventoryChangedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final ItemStack is;
    private final InventoryAction ia;
    private final HumanEntity he;
    private boolean cancelled;

    public InventoryChangedEvent(ItemStack item, InventoryAction action, HumanEntity whoClicked) {
        ia = action;
        is = item;
        he = whoClicked;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public InventoryAction getAction() {
        return ia;
    }

    public HumanEntity getWhoClicked() {
        return he;
    }

    public ItemStack getItemStack() {
        return is;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
