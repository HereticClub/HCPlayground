package org.hcmc.hcplayground.model.minion;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.LanguageManager;
import org.hcmc.hcplayground.manager.MinionManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinionPanel implements InventoryHolder {


    private final MinionEntity minion;
    private List<MinionPanelSlot> slots = new ArrayList<>();
    private Inventory inventory;
    private final JavaPlugin plugin;

    public List<MinionPanelSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<MinionPanelSlot> slots) {
        this.slots = slots;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public MinionEntity getMinion() {
        return minion;
    }

    public MinionPanel(MinionEntity minion) {
        this.minion = minion;
        plugin = HCPlayground.getInstance();
    }

    public void Reclaim(Player player) {
        // 检查Minion是否当前玩家拥有
        if (minion.isNonOwner(player)) {
            player.sendMessage(LanguageManager.getString("minionInvalidOwner"));
            return;
        }

        ItemStack helmet = minion.reclaim();
        List<ItemStack> remainder = new ArrayList<>();
        remainder.addAll(player.getInventory().addItem(helmet).values());
        remainder.addAll(pickup(player).values());
        player.closeInventory();

        if (!remainder.isEmpty()) {
            player.sendMessage(LanguageManager.getString("playerInventoryFull"));
        }
        for (ItemStack is : remainder) {
            player.getWorld().dropItemNaturally(player.getLocation(), is);
        }
    }

    public void Upgrade(Player player) {
        // 检查Minion是否当前玩家拥有
        if (minion.isNonOwner(player)) {
            player.sendMessage(LanguageManager.getString("minionInvalidOwner"));
            return;
        }
        // template为null表示Minion到达最大级别
        MinionTemplate next = MinionManager.getMinionTemplate(minion.getType(), minion.getLevel() + 1);
        if (next == null) {
            player.sendMessage(LanguageManager.getString("minionMaxLevel"));
            return;
        }
        // 获取Minion等级信息
        MinionTemplate current = MinionManager.getMinionTemplate(minion.getType(), minion.getLevel());
        if (current == null) return;
        PlayerInventory playerInventory = player.getInventory();
        List<ItemStack> upgrade = current.getUpgrade();
        // 检查玩家背包是否有足够的升级物品
        boolean checked = false;
        for (ItemStack itemStack : upgrade) {
            checked = playerInventory.containsAtLeast(itemStack, itemStack.getAmount());
            if (!checked) break;
        }
        if (!checked) {
            player.sendMessage(LanguageManager.getString("minionUpgradeFailed"));
            return;
        }
        // 移除升级Minion所需要物品
        playerInventory.removeItem(upgrade.toArray(new ItemStack[0]));
        // 升级Minion，关闭控制面板
        minion.upgrade();
        player.closeInventory();
        // 制作视觉效果，使控制面板有明显的再打开视觉
        new BukkitRunnable() {
            @Override
            public void run() {
                player.openInventory(minion.openControlPanel());
                minion.refreshSack();
            }
        }.runTaskLater(plugin, 4);
    }

    /**
     * 从Minion的Sack里面拿一组(最多64个)物品<br>
     * 玩家只能拿走背包所能容纳的物品<br>
     * 剩余不能拿走的仍然存放在Sack里面
     * @param player 要拿走这组物品的玩家
     * @param itemStack 要拿走的物品
     */
    public void pickone(@NotNull Player player, @NotNull ItemStack itemStack) {
        // 检查Minion是否当前玩家拥有
        if (minion.isNonOwner(player)) {
            player.sendMessage(LanguageManager.getString("minionInvalidOwner"));
            return;
        }
        ItemStack current = itemStack.clone();
        PlayerInventory playerInventory = player.getInventory();
        Map<Integer, ItemStack> remainder = playerInventory.addItem(current.clone());
        int statAmount = 0;

        if (remainder.isEmpty()) {
            statAmount = current.getAmount();
            minion.reduceItemInSack(current);
        }
        for (ItemStack rest : remainder.values()) {
            if (!rest.getType().equals(current.getType())) continue;
            statAmount = current.getAmount() - rest.getAmount();
            if (statAmount <= 0) continue;
            ItemStack reduce = new ItemStack(rest.getType(), statAmount);
            minion.reduceItemInSack(reduce);
        }
        minion.refreshSack();

        // 爪牙的采集数量也算到玩家的采集统计
        Material material = current.getType();
        if (statAmount >= 1) player.incrementStatistic(Statistic.PICKUP, material, statAmount);
    }

    /**
     * 尝试从Minion的Sack里面拿走所有的物品<br>
     * 玩家只能拿走背包所能容纳的物品<br>
     * 剩余不能拿走的仍然存放在Sack里面
     * @param player 尝试拿走所有物品的玩家
     * @return 没有被拿走的物品和数量
     */
    @NotNull
    public Map<Integer, ItemStack> pickup(Player player) {
        // 检查Minion是否当前玩家拥有
        if (minion.isNonOwner(player)) {
            player.sendMessage(LanguageManager.getString("minionInvalidOwner"));
            return new HashMap<>();
        }

        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] itemStacks = minion.getItemInSack();
        // 爪牙的采集数量也算到玩家的采集统计
        // TODO: 需要修复BUG，即使玩家背包满了统计会照样增加
        for (ItemStack is : itemStacks) {
            Material material = is.getType();
            int amount = is.getAmount();
            if (amount <= 0) continue;
            player.incrementStatistic(Statistic.PICKUP, material, amount);
        }

        Map<Integer, ItemStack> remainder = playerInventory.addItem(itemStacks);
        int count = remainder.values().stream().mapToInt(ItemStack::getAmount).sum();

        if (!remainder.isEmpty() && count > 0) {
            minion.setItemInSack(remainder.values().toArray(new ItemStack[0]));
        } else {
            minion.clearSack();
        }

        minion.refreshSack();
        return remainder;
    }

    public void removeEnergyDevice() {
        // TODO: remove energy device
    }

    public void addEnergyDevice() {

    }

    public void removePerksDevice() {
        // TODO: remove perks device
    }

    public void addShippingDevice() {

    }

    public void removeCompactDevice() {
        // TODO: remove compact device
    }

    public void addCompactDevice() {

    }

    public void removeSmeltDevice() {
        // TODO: remove smelt device
    }

    public void addSmeltDevice() {

    }
}
