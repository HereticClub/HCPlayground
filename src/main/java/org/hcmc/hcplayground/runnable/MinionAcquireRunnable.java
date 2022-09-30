package org.hcmc.hcplayground.runnable;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionCategory;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.MMOManager;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MinionAcquireRunnable extends BukkitRunnable {

    private final MinionEntity entity;
    private final JavaPlugin plugin;
    private final MinionTemplate template;

    public MinionAcquireRunnable(@NotNull MinionEntity entity) {
        this.entity = entity;
        plugin = HCPlayground.getInstance();
        template = entity.getMinionTemplate();
    }

    @Override
    public void run() {
        if (template == null) return;
        MinionCategory category = template.getCategory();
        if (category == null) return;

        switch (category) {
            case MINER -> digging();
            case FARMER -> farming();
            case BUTCHER, FIGHTER -> killing();
            case FISHERMAN -> fishing();
            case LUMBERJACK -> logging();
        }
    }

    /**
     * 矿工挖掘
     */
    private void digging() {
        if (template == null) return;

        List<Block> blocks = new ArrayList<>();
        List<Location> locations = new ArrayList<>(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(template.getPlatform())).toList());
        locations.forEach(x -> blocks.add(x.getBlock()));

        if (blocks.size() >= 1) {
            // 随机选择一个可收成方块
            int rnd = RandomNumber.getRandomInteger(blocks.size());
            Block block = blocks.get(rnd);
            // 收获方块的掉落物品
            entity.harvest(block);
        }
    }

    /**
     * 木工伐木
     */
    private void logging() {
        if (template == null) return;
        MinionCategory category = template.getCategory();
        if (!category.equals(MinionCategory.LUMBERJACK)) return;

        List<Location> saplingLocations = new ArrayList<>();
        for (Location l : entity.getPlatformLocations()) {
            if (!l.getBlock().getType().equals(template.getPlatform())) continue;

            Location distance = l.clone().subtract(entity.getLocation());
            if (Math.abs(distance.getX()) == 2 && Math.abs(distance.getZ()) == 2) saplingLocations.add(l.clone());
        }
        Sapling sapling;



        saplingLocations.forEach(x -> planting(x, template));
    }

    private void farming() {
        // 获取爪牙定义模板
        if (template == null) return;
        MinionType minionType = entity.getType();

        // 从工作平台所有方块中获取所需类型的位置
        List<Location> cropLocations = switch (minionType) {
            case WHEAT, CARROT, POTATO, BEETROOT -> ploughing();
            case PUMPKIN, MELON -> cultivating();
            case CACTUS -> getCactusLocations();
            case BEE -> getBeehives();
            case SUGAR_CANE -> puddle();
            default -> new ArrayList<>(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(template.getPlatform())).toList());
        };

        // 获取可收成的作物方块列表
        List<Block> cropBlocks = new ArrayList<>(
                switch (minionType) {
                    case CACTUS -> getCropBlocks(Material.CACTUS);
                    case SUGAR_CANE -> getCropBlocks(Material.SUGAR_CANE);
                    case PUMPKIN -> getCropBlocks(Material.PUMPKIN);
                    case MELON -> getCropBlocks(Material.MELON);
                    default -> getCropBlocks(cropLocations);
                });

        // 随机收获一个方块的掉落物品
        if (cropBlocks.size() >= 1) {
            // 随机选择一个可收成方块
            int rnd = RandomNumber.getRandomInteger(cropBlocks.size());
            Block block = cropBlocks.get(rnd);
            // 收获方块的掉落物品
            switch (minionType) {
                case BEE -> entity.harvestHoney(block);
                default -> entity.harvest(block);
            }
        }
        // 重新耕地和播种
        cropLocations.forEach(x -> planting(x, template));
    }

    private void killing() {
        List<Entity> cubs = entity.getNearbyCubs();
        if (cubs.size() >= 1) {
            LivingEntity cub = (LivingEntity) entity.getRandomCubs();
            cub.damage(cub.getHealth() + 1, entity.getArmorStand());
        }
    }

    private void fishing() {
        int dropIndex = RandomNumber.getRandomInteger(MMOManager.FishingMaterials.length);
        ItemStack itemStack = new ItemStack(MMOManager.FishingMaterials[dropIndex], 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                Map<Material, Integer> suck = entity.getSack();
                int amount = suck.getOrDefault(itemStack.getType(), 0);
                amount += itemStack.getAmount();
                suck.put(itemStack.getType(), amount);

                entity.setSack(suck);
                entity.refreshSack();
            }
        }.runTaskLater(plugin, 10);

        Player player = Bukkit.getPlayer(entity.getOwner());
        if (player != null)
            player.playSound(entity.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, 1, 1);
    }

    private List<Location> getCactusLocations() {
        if (template == null) return new ArrayList<>();
        List<Location> locations = new ArrayList<>();
        List<Location> platform = entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(template.getPlatform())).toList();

        for (Location l : platform) {
            Location distance = l.clone().subtract(entity.getLocation());
            if (Math.abs(distance.getX()) == 2 && Math.abs(distance.getZ()) == 2) locations.add(l.clone());
            if (Math.abs(distance.getX()) == 2 && Math.abs(distance.getZ()) == 0) locations.add(l.clone());
            if (Math.abs(distance.getX()) == 0 && Math.abs(distance.getZ()) == 2) locations.add(l.clone());
            if (Math.abs(distance.getX()) == 1 && Math.abs(distance.getZ()) == 1) locations.add(l.clone());
        }

        return locations;
    }

    private List<Location> getBeehives() {
        return entity.getPlatformLocations().stream().filter(x -> x.clone().add(0, 1, 0).getBlock().getType().equals(Material.BEEHIVE)).toList();
    }

    private List<Location> cultivating() {
        if (template == null) return new ArrayList<>();
        List<Location> locations = new ArrayList<>(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(template.getPlatform())).toList());
        locations.addAll(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(Material.FARMLAND)).toList());
        Location center = entity.getLocation().clone().add(0, -1, 0);

        locations.forEach(x -> {
            Location distance = center.clone().subtract(x);
            Block block = x.getBlock();
            Material material = block.getType();

            if (Math.abs(distance.getX()) == 1 && !material.equals(Material.FARMLAND))
                entity.placeBlock(block, Material.FARMLAND);
            if (Math.abs(distance.getX()) == 0 && Math.abs(distance.getZ()) == 1 && !material.equals(Material.FARMLAND))
                entity.placeBlock(block, Material.FARMLAND);

            if (block.getBlockData() instanceof Farmland farmland) {
                farmland.setMoisture(farmland.getMaximumMoisture());
                block.setBlockData(farmland);
            }
        });

        locations.removeIf(x -> x.getBlock().getType().equals(template.getPlatform()));
        return locations;
    }


    /**
     * 为种植甘蔗挖水坑
     *
     * @return 可种植甘蔗的位置列表
     */
    private List<Location> puddle() {
        if (template == null) return new ArrayList<>();
        // 获取工作平台所需的方块
        List<Location> locations = new ArrayList<>(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(template.getPlatform())).toList());
        Location center = entity.getLocation().clone().add(0, -1, 0);
        entity.placeBlock(center.getBlock(), Material.WATER);
        // 在指定的方块挖水坑
        List<Location> removes = new ArrayList<>();
        locations.forEach(x -> {
            Location distance = center.clone().subtract(x);
            Material m = x.getBlock().getType();
            if (Math.abs(distance.getX()) == 2 && Math.abs(distance.getZ()) == 1 && !m.equals(Material.WATER)) {
                // 摆放方块并且播放摆放方块的声音
                entity.placeBlock(x.getBlock(), Material.WATER);
                removes.add(x);
            }
            if (Math.abs(distance.getX()) == 0 && Math.abs(distance.getZ()) == 2 && !m.equals(Material.WATER)) {
                entity.placeBlock(x.getBlock(), Material.WATER);
                removes.add(x);
            }
        });

        locations.removeAll(removes);
        return locations;
    }

    /**
     * 耕地
     *
     * @return 已耕地的位置列表
     */
    private List<Location> ploughing() {
        if (template == null) return new ArrayList<>();
        // 获取工作平台所需的方块
        List<Location> locations = new ArrayList<>(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(template.getPlatform())).toList());
        // 添加已经犁地(FARMLAND)的方块
        locations.addAll(entity.getPlatformLocations().stream().filter(x -> x.getBlock().getType().equals(Material.FARMLAND)).toList());
        locations.forEach(x -> {
            // 设置未被犁地(FARMLAND)的方块
            Block block = x.getBlock();
            entity.placeBlock(block, Material.FARMLAND);
            // 为已被犁地(FARMLAND)的方块浇水
            Farmland farmland = (Farmland) block.getBlockData();
            farmland.setMoisture(farmland.getMaximumMoisture());
            block.setBlockData(farmland);
        });
        return locations;
    }

    private void planting(Location location, MinionTemplate template) {
        Block cropBlock = location.clone().add(0, 1, 0).getBlock();
        MinionType minionType = template.getType();
        // 延时8Ticks，让农作物生长
        new BukkitRunnable() {
            @Override
            public void run() {
                // 设置WHEAT, CARROT, POTATO, BEETROOT, NETHER_WART, SWEET_BERRY, MELON, PUMPKIN作物的Age，让作物生长到最大值
                if (cropBlock.getBlockData() instanceof Ageable ageable) {
                    if (ageable.getAge() < ageable.getMaximumAge()) {
                        ageable.setAge(ageable.getAge() + 1);
                        cropBlock.setBlockData(ageable);
                    } else {
                        if (minionType.equals(MinionType.MELON) || minionType.equals(MinionType.PUMPKIN))
                            setAttachedStem(cropBlock);
                    }
                }
                if (cropBlock.getBlockData() instanceof Beehive beehive) {
                    if (beehive.getHoneyLevel() < beehive.getMaximumHoneyLevel()) {
                        beehive.setHoneyLevel(beehive.getHoneyLevel() + 1);
                        cropBlock.setBlockData(beehive);
                    }
                }
                if (cropBlock.getBlockData() instanceof Sapling sapling) {
                    if (sapling.getStage() < sapling.getMaximumStage()) {
                        sapling.setStage(sapling.getStage() + 1);
                        cropBlock.setBlockData(sapling);
                    }
                }
            }
        }.runTaskLater(plugin, 8);
        // 延时16Ticks，在工作平台方块上播种
        new BukkitRunnable() {
            @Override
            public void run() {
                Material blockType = cropBlock.getType();
                // 放置相应的作物种子方块
                if (!blockType.equals(Material.AIR)) {
                    if (blockType.equals(template.getSeed())) return;
                    if (blockType.equals(template.getSapling())) return;
                    if (blockType.equals(Material.BAMBOO)) return;
                    if (blockType.equals(Material.OAK_LOG)) return;
                    if (blockType.equals(Material.BIRCH_LOG)) return;
                    if (blockType.equals(Material.ACACIA_LOG)) return;
                    if (blockType.equals(Material.DARK_OAK_LOG)) return;
                    if (blockType.equals(Material.JUNGLE_LOG)) return;
                    if (blockType.equals(Material.SPRUCE_LOG)) return;
                    if (blockType.equals(Material.MANGROVE_LOG)) return;
                    if (blockType.equals(Material.ATTACHED_PUMPKIN_STEM)) return;
                    if (blockType.equals(Material.ATTACHED_MELON_STEM)) return;
                }

                if (!template.getCategory().equals(MinionCategory.LUMBERJACK) && !template.getSeed().equals(Material.AIR))
                    entity.placeBlock(cropBlock, template.getSeed());
                if (template.getCategory().equals(MinionCategory.LUMBERJACK) && !template.getSapling().equals(Material.AIR))
                    entity.placeBlock(cropBlock, template.getSapling());
            }
        }.runTaskLater(plugin, 16);
    }

    private void setAttachedStem(Block block) {
        Block center = block.getLocation().clone().add(0, -1, 0).getBlock();
        for (BlockFace face : BlockFace.values()) {
            if (!face.isCartesian()) continue;
            Block target = center.getRelative(face);
            Location distance = entity.getLocation().clone().subtract(target.getLocation());
            if (Math.abs(distance.getX()) >= 3 || Math.abs(distance.getZ()) >= 3) continue;
            if (!target.getType().equals(entity.getMinionTemplate().getPlatform())) continue;
            if (!target.getRelative(BlockFace.UP).getType().equals(Material.AIR)) continue;

            if (block.getType().equals(Material.PUMPKIN_STEM)) {
                block.setType(Material.ATTACHED_PUMPKIN_STEM);
                block.getRelative(face).setType(Material.PUMPKIN);
            }
            if (block.getType().equals(Material.MELON_STEM)) {
                block.setType(Material.ATTACHED_MELON_STEM);
                block.getRelative(face).setType(Material.MELON);
            }
            if (!(block.getBlockData() instanceof Directional directional)) continue;
            directional.setFacing(face);
            block.setBlockData(directional);
            break;
        }
    }

    private List<Block> getCropBlocks(List<Location> locations) {
        // 获取普通(不需要过滤位置)的方块
        List<Block> blocks = new ArrayList<>();
        for (Location l : locations) {
            Block block = l.clone().add(0, 1, 0).getBlock();
            if (block.getBlockData() instanceof Ageable ageable && ageable.getAge() < ageable.getMaximumAge())
                continue;
            if (block.getBlockData() instanceof Beehive beehive && beehive.getHoneyLevel() < beehive.getMaximumHoneyLevel())
                continue;
            blocks.add(block);
        }

        return blocks;
    }

    private List<Block> getCropBlocks(Material sample) {
        List<Block> blocks = new ArrayList<>();
        List<Location> locations = entity.getPlatformLocations();

        for (Location location : locations) {
            Block block = location.clone().add(0, 1, 0).getBlock();
            if (!block.getType().equals(sample)) continue;
            blocks.add(block);
        }
        return blocks;
    }
}
