package org.hcmc.hcplayground.runnable;

import com.google.gson.reflect.TypeToken;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.utility.Global;

import java.lang.reflect.Type;
import java.util.List;

public class CutDownTreeRunnable extends BukkitRunnable {

    private final MinionEntity entity;
    private Block startBlock;
    private final List<Location> tree;

    public CutDownTreeRunnable(MinionEntity entity, Block startBlock, List<Location> tree) {
        this.entity = entity;
        this.startBlock = startBlock;
        this.tree = clone(tree);
    }

    @Override
    public void run() {
        /*
        if (tree.size() == 0) cancel();
        Location location = tree.get(0);
        Block block = location.getBlock();
        if (!block.getType().equals(startBlock.getType())) return;

        entity.harvest(block);
        tree.remove(location);

         */


        if (startBlock.getType().equals(Material.AIR)) cancel();
        if (!startBlock.getType().equals(Material.BIRCH_LOG) &&
                !startBlock.getType().equals(Material.OAK_LOG) &&
                !startBlock.getType().equals(Material.ACACIA_LOG) &&
                !startBlock.getType().equals(Material.JUNGLE_LOG) &&
                !startBlock.getType().equals(Material.DARK_OAK_LOG) &&
                !startBlock.getType().equals(Material.SPRUCE_LOG) &&
                !startBlock.getType().equals(Material.MANGROVE_LOG) &&
                !startBlock.getType().equals(Material.CHORUS_FLOWER) &&
                !startBlock.getType().equals(Material.CHORUS_PLANT) &&
                !startBlock.getType().equals(Material.BIRCH_LOG)) cancel();

        Material material = startBlock.getType();
        entity.harvest(startBlock);

        for (BlockFace face : BlockFace.values()) {
            if (!face.isCartesian()) continue;
            Location distance = startBlock.getLocation().clone().subtract(entity.getLocation());
            if (Math.abs(distance.getX()) >= 5 || Math.abs(distance.getZ()) >= 5) continue;
            Block relativeBlock = startBlock.getRelative(face);
            if (!relativeBlock.getType().equals(material)) continue;

            startBlock = relativeBlock;
            break;
        }
    }

    private List<Location> clone(List<Location> source) {
        Type type = new TypeToken<List<Location>>() {
        }.getType();

        String value = Global.GsonObject.toJson(source);
        return Global.GsonObject.fromJson(value, type);
    }
}
