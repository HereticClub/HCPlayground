package org.hcmc.hcplayground.runnable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.manager.ItemManager;

/**
 * 使用指令/enchant为主手的自定义物品附魔后更新说明
 */
public class UpdateLoreRunnable extends BukkitRunnable {
    private final Player player;

    public UpdateLoreRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {
        ItemStack itemStack = player.getInventory().getItem(EquipmentSlot.HAND);
        ItemManager.updateLore(itemStack);

        player.getInventory().setItem(EquipmentSlot.HAND, itemStack);
   }
}
