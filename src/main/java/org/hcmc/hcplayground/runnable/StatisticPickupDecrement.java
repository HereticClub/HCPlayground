package org.hcmc.hcplayground.runnable;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class StatisticPickupDecrement extends BukkitRunnable {

    private final ItemStack itemStack;
    private final Player player;

    public StatisticPickupDecrement(Player player, ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    @Override
    public void run() {
        Material material = itemStack.getType();
        int amount = itemStack.getAmount();
        player.decrementStatistic(Statistic.PICKUP, material, amount);
    }
}
