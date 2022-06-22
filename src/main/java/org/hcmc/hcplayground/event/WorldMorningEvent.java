package org.hcmc.hcplayground.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class WorldMorningEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();
    private final long tick;
    private final World world;

    public WorldMorningEvent(World world, long tick) {
        this.world = world;
        this.tick = tick;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public World getWorld() {
        return world;
    }

    public long getTick() {
        return tick;
    }
}
