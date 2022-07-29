package org.hcmc.hcplayground.model.minion;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MinionPanel implements InventoryHolder {


    private final MinionEntity owner;
    private List<MinionPanelSlot> slots = new ArrayList<>();
    private Inventory inventory;

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

    public MinionEntity getOwner() {
        return owner;
    }

    public MinionPanel(MinionEntity owner) {
        this.owner = owner;
    }

    public void Reclaim() {

    }

    public void Upgrade() {

    }

    /**
     * 从Minion的Sack里面拿一组(最多64个)物品<br>
     * 玩家只能拿走背包所能容纳的物品<br>
     * 剩余不能拿走的仍然存放在Sack里面
     * @param player 要拿走这组物品的玩家
     * @param itemStack 要拿走的物品
     */
    public void pickone(@NotNull Player player, @NotNull ItemStack itemStack) {
        ItemStack current = itemStack.clone();
        PlayerInventory playerInventory = player.getInventory();

        Map<Integer, ItemStack> remainder = playerInventory.addItem(current.clone());
        if (remainder.isEmpty()) {
            owner.reduceItemInSack(current);
        }
        for (ItemStack rest : remainder.values()) {
            if (!rest.getType().equals(current.getType())) continue;
            int amount = current.getAmount() - rest.getAmount();
            if (amount <= 0) continue;
            ItemStack reduce = new ItemStack(rest.getType(), amount);
            owner.reduceItemInSack(reduce);
        }
        owner.refreshSack();
    }

    /**
     * 尝试从Minion的Sack里面拿走所有的物品<br>
     * 玩家只能拿走背包所能容纳的物品<br>
     * 剩余不能拿走的仍然存放在Sack里面
     * @param player 尝试拿走所有物品的玩家
     */
    public void pickup(Player player) {
        if (inventory == null) return;
        PlayerInventory playerInventory = player.getInventory();
        ItemStack[] itemStacks = owner.getItemInSack();

        Map<Integer, ItemStack> remainder = playerInventory.addItem(itemStacks);
        System.out.println(remainder);
        owner.setItemInSack(remainder.values().toArray(new ItemStack[0]));
        owner.refreshSack();
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
