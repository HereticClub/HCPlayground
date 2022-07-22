package org.hcmc.hcplayground.runnable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionCategory;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MinionAcquireRunnable extends BukkitRunnable {

    private final MinionEntity entity;
    private final MinionTemplate template;

    private final JavaPlugin plugin;

    public MinionAcquireRunnable(@NotNull MinionEntity entity, @NotNull MinionTemplate template) {
        this.entity=entity;
        this.template=template;
        plugin = HCPlayground.getInstance();
    }

    @Override
    public void run() {
        if (template.getRequirement() == null) return;

        MinionCategory category = template.getCategory();
        if (category == null) return;

        switch (category) {
            case Miner -> digging();
            case Farmer -> farming();
            case Butcher -> breeding();
            case Fighter -> killing();
            case Fisherman -> fishing();
            case Lumberjack -> logging();
        }
    }

    /**
     * 矿工挖掘
     */
    private void digging() {
        List<Block> blocks = new ArrayList<>();
        for (Location l : entity.getPlatform()) {
            Block b = l.getBlock();
            if (!b.getType().equals(template.getRequirement())) continue;

            blocks.add(b);
        }

        int size = blocks.size();
        if (size <= 0) return;
        int rnd = RandomNumber.getRandomInteger(size);
        Block block = blocks.get(rnd);
        List<ItemStack> dropStacks = block.getDrops().stream().toList();

        int dropBound = dropStacks.size();
        int dropCount = RandomNumber.getRandomInteger(dropStacks.size()) + 1;
        List<Integer> dropList = RandomNumber.getRandomInteger(dropBound, dropCount);
        block.setType(Material.AIR);
        for (Integer i : dropList) {
            ItemStack is = dropStacks.get(i).clone();
            Item item = block.getWorld().dropItemNaturally(block.getLocation(), is);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Map<Material, Integer> suck = entity.getSack();
                    int amount = suck.get(is.getType());
                    amount += is.getAmount();
                    entity.getSack().put(is.getType(), amount);
                    item.remove();
                }
            }.runTaskLater(plugin, 10);
        }

        Location l = entity.getArmorStand().getLocation().clone();
        l.setY(0);
        l.setPitch(0);
        l.setDirection(block.getLocation().toVector());
        entity.getArmorStand().setRotation(l.getPitch(), l.getYaw());
    }

    /**
     * 木工伐木
     */
    private void logging() {

    }

    private void farming() {

    }

    private void breeding() {

    }

    private void killing() {

    }

    private void fishing() {

    }
}
