package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionCategory;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.hcmc.hcplayground.utility.RomanNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MinionEntity {

    @Expose
    @SerializedName(value = "type")
    private MinionType type;
    @Expose
    @SerializedName(value = "level")
    private int level;
    @Expose
    @SerializedName(value = "world")
    private String world;
    @Expose
    @SerializedName(value = "x")
    private double x;
    @Expose
    @SerializedName(value = "y")
    private double y;
    @Expose
    @SerializedName(value = "z")
    private double z;
    @Expose
    @SerializedName(value = "pitch")
    private float pitch;
    @Expose
    @SerializedName(value = "yaw")
    private float yaw;
    @Expose
    @SerializedName(value = "sack")
    private Map<Material, Integer> sack = new HashMap<>();
    @Expose
    @SerializedName(value = "uuid")
    private UUID uuid;
    @Expose
    @SerializedName(value = "owner")
    private UUID owner;
    @Expose
    @SerializedName(value = "lastAcquireTime")
    private Date lastAcquireTime = new Date();

    @Expose(deserialize = false, serialize = false)
    private Location location;
    @Expose(deserialize = false, serialize = false)
    private List<Location> platformLocations = new ArrayList<>();
    @Expose(deserialize = false, serialize = false)
    private MinionTemplate template;
    @Expose(deserialize = false, serialize = false)
    private MinionTemplate nextLevel;
    @Expose(deserialize = false, serialize = false)
    private ArmorStand armorStand;
    @Expose(deserialize = false, serialize = false)
    private Inventory inventory;
    @Expose(deserialize = false, serialize = false)
    private ItemStack tool = new ItemStack(Material.AIR, 1);
    @Expose(serialize = false, deserialize = false)
    private JavaPlugin plugin;

    public MinionEntity(ArmorStand armorStand, MinionType type, int level) {
        initial(armorStand, type, level);
    }

    public void initial(MinionEntity other) {
        initial(other.armorStand, other.type, other.level);
    }

    public void initial(ArmorStand armorStand, MinionType type, int level) {
        this.type = type;
        this.level = level;
        this.armorStand = armorStand;
        this.location = armorStand.getLocation();
        this.uuid = armorStand.getUniqueId();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
        this.world = armorStand.getWorld().getName();
        this.template = MinionManager.getMinionTemplate(type, level);
        this.nextLevel = MinionManager.getMinionTemplate(type, level + 1);
        this.plugin = HCPlayground.getInstance();

        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment != null) this.tool = equipment.getItemInMainHand();
        if (this.lastAcquireTime == null) this.lastAcquireTime = new Date();

        if (this.platformLocations == null) {
            this.platformLocations = new ArrayList<>();
        } else {
            this.platformLocations.clear();
        }

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                this.platformLocations.add(this.location.clone().add(x, -1, z));
            }
        }
    }

    /**
     * 摆放方块并且播放摆放相应方块的声音
     * @param block 要摆放的方块实例
     * @param material 方块的材质
     */
    public void placeBlock(Block block, Material material) {
        if (!block.getType().equals(material)) block.setType(material);
        if (block.getBlockData() instanceof Bisected) {
            Block upBlock = block.getRelative(BlockFace.UP);
            upBlock.setType(material, false);
            Bisected bisected = (Bisected) upBlock.getBlockData();
            bisected.setHalf(Bisected.Half.TOP);
            upBlock.setBlockData(bisected);
        }
        if (block.getBlockData() instanceof Directional directional) {
            List<BlockFace> faces = directional.getFaces().stream().toList();
            int rnd = RandomNumber.getRandomInteger(faces.size());
            directional.setFacing(faces.get(rnd));
            block.setBlockData(directional);
        }

        Player player = Bukkit.getPlayer(owner);
        if (player == null) return;
        SoundGroup soundGroup = block.getBlockData().getSoundGroup();
        player.playSound(block.getLocation(), soundGroup.getPlaceSound(), soundGroup.getVolume(), soundGroup.getPitch());
    }

    /**
     * 破坏指定位置的方块，并且播放破坏相应方块的声音
     * @param block 要破坏的方块的位置
     */
    public void breakBlock(Block block) {
        if (block.getType().equals(Material.AIR)) return;

        Player player = Bukkit.getPlayer(owner);
        if (player == null) return;
        SoundGroup soundGroup = block.getBlockData().getSoundGroup();
        player.playSound(block.getLocation(), soundGroup.getBreakSound(), soundGroup.getVolume(), soundGroup.getPitch());
        block.setType(Material.AIR);
    }

    public MinionTemplate getMinionTemplate() {
        return MinionManager.getMinionTemplate(type, level);
    }

    public List<Entity> getNearbyCubs() {
        List<Entity> entities = armorStand.getNearbyEntities(4, 10, 4);
        return entities.stream().filter(x -> x.getType().equals(template.getCubs())).toList();
    }

    public Location getRandomPlatform() {
        int rnd = RandomNumber.getRandomInteger(platformLocations.size());
        return platformLocations.get(rnd);
    }

    public Entity getRandomCubs() {
        List<Entity> entities = getNearbyCubs();
        if (entities.size() == 0) return null;

        int rnd = RandomNumber.getRandomInteger(entities.size());
        return entities.get(rnd);
    }

    public void harvestHoney(Block block) {
        LookAt(block.getLocation());
        if (!(block.getBlockData() instanceof Beehive beehive)) return;
        if (beehive.getHoneyLevel() < beehive.getMaximumHoneyLevel()) return;
        boolean rnd = new Random().nextBoolean();
        int count = new Random().nextInt(3) + 1;
        ItemStack is = new ItemStack(rnd ? Material.HONEYCOMB : Material.HONEY_BOTTLE, count);
        Item item = block.getWorld().dropItemNaturally(block.getLocation(), is);
        beehive.setHoneyLevel(0);
        block.setBlockData(beehive);
        // 10Ticks(500毫秒)后，显示模拟收集效果
        new BukkitRunnable() {
            @Override
            public void run() {
                int amount = sack.getOrDefault(is.getType(), 0);
                amount += is.getAmount();
                sack.put(is.getType(), amount);
                refreshSack();
                item.remove();
            }
        }.runTaskLater(plugin, 10);
    }

    public void harvest(List<Item> drops, Location location) {
        // MinionEntity 望向可收获的方块
        LookAt(location);
        // 遍历掉落物品
        for (Item item : drops) {
            ItemStack is = item.getItemStack();
            // 排除空气方块(AIR)
            if (is.getType().equals(Material.AIR)) continue;
            // 10Ticks(500毫秒)后，显示模拟收集效果
            int amount = sack.getOrDefault(is.getType(), 0);
            amount += is.getAmount();
            sack.put(is.getType(), amount);
            refreshSack();
            item.remove();
        }
    }

    /**
     * 破坏方块，从方块中收获掉落物品，然后放入MinionEntity实例的袋中
     *
     * @param block 方块实例
     */
    public void harvest(Block block) {
        // MinionEntity华丽转身，望向可收获的方块
        LookAt(block.getLocation());
        // 获取方块的掉落列表
        List<ItemStack> dropStacks = block.getDrops(tool).stream().toList();
        // 在掉落列表中随机选择掉落物品
        int dropBound = dropStacks.size();
        int dropCount = RandomNumber.getRandomInteger(dropStacks.size()) + 1;
        List<Integer> dropList = RandomNumber.getRandomInteger(dropBound, dropCount);
        // 重置可收成方块为空气方块(AIR)
        breakBlock(block);
        dropList.forEach(x -> {
            // 遍历掉落物品
            ItemStack is = dropStacks.get(x).clone();
            // 排除空气方块(AIR)
            if (is.getType().equals(Material.AIR)) return;
            // 在世界中显示物品的掉落效果
            Item item = block.getWorld().dropItemNaturally(block.getLocation(), is);
            // 10Ticks(500毫秒)后，显示模拟收集效果
            new BukkitRunnable() {
                @Override
                public void run() {
                    int amount = sack.getOrDefault(is.getType(), 0);
                    amount += is.getAmount();
                    sack.put(is.getType(), amount);
                    refreshSack();
                    item.remove();
                }
            }.runTaskLater(plugin, 10);
        });
    }

    public void breedingCubs() {
        if (template == null) return;
        MinionCategory category = template.getCategory();
        if (!category.equals(MinionCategory.BUTCHER) && !category.equals(MinionCategory.FIGHTER)) return;

        World world = armorStand.getWorld();
        List<Entity> cubs = getNearbyCubs();
        Location l = getRandomPlatform().clone().add(0, 1, 0);
        if (cubs.size() <= 4) world.spawnEntity(l, template.getCubs());
    }

    /**
     * 针对类型为蜜蜂爪牙的操作，搭建蜂箱，种植各种花
     */
    public void breedingBees() {
        if (template == null) return;
        if (!template.getType().equals(MinionType.BEE)) return;
        World world = armorStand.getWorld();
        List<Location> undressingLocations = new ArrayList<>();
        List<Location> beehiveLocations = new ArrayList<>();
        for (Location l : platformLocations) {
            Block block = l.clone().add(0, 1, 0).getBlock();
            Material material = block.getType();
            if (block.getBlockData() instanceof Beehive) beehiveLocations.add(block.getLocation());
            if (!Arrays.asList(MMOManager.Flowers).contains(material) && !(block.getBlockData() instanceof Beehive))
                undressingLocations.add(block.getLocation());
        }

        int size = undressingLocations.size();
        if (size == 0) return;
        int rnd = RandomNumber.getRandomInteger(size);
        Location l = undressingLocations.get(rnd);
        if (beehiveLocations.size() < 6) {
            placeBlock(l.getBlock(), Material.BEEHIVE);
            world.spawnEntity(this.location.clone().add(0, 2, 0), EntityType.BEE);
            world.spawnEntity(this.location.clone().add(0, 2, 0), EntityType.BEE);
        } else {
            int flowerIndex = RandomNumber.getRandomInteger(MMOManager.Flowers.length);
            placeBlock(l.getBlock(), MMOManager.Flowers[flowerIndex]);
        }
    }

    /**
     * Minion 修整平台，将Minion的工作平台修整为所需要的方块
     */
    public void dressingPlatform() {
        if (template == null || template.getPlatform().equals(Material.AIR)) return;
        List<Location> undressingLocation = new ArrayList<>(platformLocations.stream().filter(x -> !x.getBlock().getType().equals(template.getPlatform())).toList());
        switch (type) {
            case WHEAT, CARROT, POTATO, BEETROOT, MELON, PUMPKIN -> undressingLocation.removeIf(x -> x.getBlock().getType().equals(Material.FARMLAND));
            case SUGAR_CANE -> undressingLocation.removeIf(x -> x.getBlock().getType().equals(Material.WATER));
        }

        int size = undressingLocation.size();
        if (size == 0) return;
        int rnd = RandomNumber.getRandomInteger(size);
        Block block = undressingLocation.get(rnd).getBlock();
        placeBlock(block, template.getPlatform());
    }

    public void reduceItemInSack(@NotNull ItemStack... itemStacks) {
        for (ItemStack is : itemStacks) {
            Material material = is.getType();
            int amount = is.getAmount();
            reduceItemInSack(material, amount);
        }
    }

    public ItemStack reclaim() {
        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment == null) return null;

        ItemStack helmet = equipment.getHelmet();
        RecordManager.removeMinionRecord(armorStand.getUniqueId());
        armorStand.remove();

        return helmet;
    }

    public void reduceItemInSack(Material material, int amount) {
        if (!sack.containsKey(material)) return;
        int rest = sack.get(material) - amount;
        sack.replace(material, Math.max(rest, 0));
    }

    public void setItemInSack(ItemStack... itemStacks) {
        for (ItemStack is : itemStacks) {
            Material material = is.getType();
            int amount = is.getAmount();
            setItemInSack(material, amount);
        }
    }

    public void setItemInSack(Material material, int amount) {
        if (!sack.containsKey(material)) return;
        sack.replace(material, amount);
    }

    public void clearSack() {
        sack.clear();
    }

    public void upgrade() {
        ItemStack helmet = MinionManager.getMinionStack(this.type, level + 1, 1);
        if (helmet == null) return;
        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment == null) return;

        level = level + 1;
        equipment.setHelmet(helmet);
    }

    public void refreshSack() {
        // 获取爪牙的定义模板
        if (template == null) return;
        // 获取爪牙的储存槽位数量
        int storageAmount = template.getStorageAmount();
        // 创建物品的拆分列表，计算溢出物品数量
        List<ItemStack> acquired = new ArrayList<>();
        Map<Material, Boolean> check = new HashMap<>();
        // 创建剩余采集数量变量(sack属性的副本)
        Map<Material, Integer> remainder = new HashMap<>();
        Set<Material> keys = sack.keySet();
        for (Material m : keys) {
            remainder.put(m, sack.get(m));
        }

        // 按照物品的最大叠堆数量拆分采集物品
        outer:
        do {
            // 每个爪牙可能会采集多种物品
            for (Material m : keys) {
                // 物品的剩余数量
                int restAmount = remainder.get(m);
                // 按物品的最大叠堆获取拆分数量，不足最大叠堆数则全部拿取
                int count = Math.min(restAmount, m.getMaxStackSize());
                // 剩余数量为0，表示这种物品已经拆分完毕
                if (restAmount <= 0) {
                    check.put(m, true);
                    continue;
                }
                // 添加到拆分列表
                acquired.add(new ItemStack(m, count));
                // 覆盖物品拆分后的剩余数量
                remainder.replace(m, restAmount - count);
                // 拆分组数量不能大于爪牙的储存槽位数量
                if (acquired.size() >= storageAmount) break outer;
            }
        } while (check.size() != keys.size());
        // 覆盖爪牙袋子的数量，将溢出数量去掉
        for (Material m : keys) {
            sack.replace(m, Math.abs(sack.get(m) - remainder.get(m)));
        }
        // 清空可用储存槽位
        if (inventory == null) return;
        if (!(inventory.getHolder() instanceof MinionPanel panel)) return;
        MinionPanelSlot slot = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.STORAGE)).findAny().orElse(null);
        if (slot == null) return;
        for (int i = 0; i < storageAmount; i++) {
            ItemStack air = new ItemStack(Material.AIR, 1);
            inventory.setItem(slot.getSlots()[i], air);
        }
        inventory.addItem(acquired.toArray(new ItemStack[0]));
    }

    /**
     * 打开每个爪牙相应的控制面板
     */
    public Inventory openControlPanel() {
        // 设置爪牙的标题，容量，采集周期
        int storage = 1;
        String title = "";
        if (template == null) return null;
        title = template.getDisplay();
        storage = template.getStorageAmount();

        // 创建爪牙的控制面板
        Inventory inventory = MinionManager.createBasePanel(title, this);
        if (!(inventory.getHolder() instanceof MinionPanel panel)) return null;
        // 获取储存插槽实例
        MinionPanelSlot slotStorage = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.STORAGE)).findAny().orElse(null);
        if (slotStorage == null) return null;
        int[] slotIndexes = slotStorage.getSlots();
        Arrays.sort(slotIndexes);
        // 解锁可用的存储位置
        for (int i = 0; i < storage; i++) {
            inventory.setItem(slotIndexes[i], new ItemStack(Material.AIR, 1));
        }
        // 设置升级槽位的爪牙升级信息
        MinionPanelSlot slotUpgrade = panel.getSlots().stream().filter(x -> x.getType().equals(PanelSlotType.UPGRADE)).findAny().orElse(null);
        if (slotUpgrade == null) return null;
        for (int i : slotUpgrade.getSlots()) {
            // 一些不是判断的判断
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null) continue;
            ItemMeta meta = itemStack.getItemMeta();
            if (meta == null) continue;
            List<String> lore = meta.getLore();
            if (lore == null) continue;
            // 下一级的模板为null，则认为已经升级至最大级别
            if (nextLevel == null) {
                lore = LanguageManager.getStringList("minionMaxLevelLore");
                lore.replaceAll(x -> x.replace("%current_level%", RomanNumber.fromInteger(level))
                        .replace("%current_period%", String.valueOf(template.getPeriod()))
                        .replace("%current_storage%", String.valueOf(template.getStorageAmount())));
            } else {
                lore.replaceAll(x -> x.replace("%current_level%", RomanNumber.fromInteger(level))
                        .replace("%next_level%", RomanNumber.fromInteger(level + 1))
                        .replace("%current_period%", String.valueOf(template.getPeriod()))
                        .replace("%next_period%", String.valueOf(nextLevel.getPeriod()))
                        .replace("%current_storage%", String.valueOf(template.getStorageAmount()))
                        .replace("%next_storage%", String.valueOf(nextLevel.getStorageAmount())));

                String upgradeLore = slotUpgrade.getUpgradeLore();
                List<ItemStack> upgrade = template.getUpgrade();
                for (ItemStack isUpgrade : upgrade) {
                    ItemBase ib = ItemManager.getItemBase(isUpgrade);
                    lore.add(upgradeLore.replace("%name%", ib.isNativeItemStack() ? isUpgrade.getType().name() : ib.getName())
                            .replace("%amount%", String.valueOf(isUpgrade.getAmount())));
                }
            }

            meta.setLore(lore);
            itemStack.setItemMeta(meta);
        }

        // 获取爪牙的头盔ItemStack，必须是player_head
        ItemStack helmet = MinionManager.getMinionStack(type, level, 1);
        inventory.setItem(4, helmet);

        this.inventory = inventory;
        return inventory;
    }

    public boolean isItemInSack(ItemStack itemStack) {
        if (itemStack == null) return false;
        Material material = itemStack.getType();
        return sack.containsKey(material);
    }

    public ItemStack[] getItemInSack() {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Material m : sack.keySet()) {
            itemStacks.add(new ItemStack(m, sack.get(m)));
        }
        return itemStacks.toArray(new ItemStack[0]);
    }

    public Date getLastAcquireTime() {
        return lastAcquireTime;
    }

    public void setLastAcquireTime(Date lastAcquireTime) {
        this.lastAcquireTime = lastAcquireTime;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Map<Material, Integer> getSack() {
        return new HashMap<>(sack);
    }

    public void setSack(Map<Material, Integer> sack) {
        this.sack = new HashMap<>(sack);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public List<Location> getPlatformLocations() {
        return platformLocations;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getWorld() {
        return world;
    }

    public Location getLocation() {
        World w = Bukkit.getWorld(world);
        return new Location(w, x, y, z, yaw, pitch);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public MinionType getType() {
        return type;
    }

    public void setType(MinionType type) {
        this.type = type;
    }

    public boolean isNonOwner(Player player) {
        return !owner.equals(player.getUniqueId());
    }

    /**
     * 从startBlock开始获取相同材质的方块
     * @param startBlock 起始方块，但必须是各种原木
     * @param tree 返回一组和startBlock相同的方块<br>
     *             这个参数必须以变量方式传入<br>
     *             这个方法被调用后，这个参数会被重新设置
     */
    public void setWholeTree(@NotNull Block startBlock,@Nullable List<Location> tree) {
        if (tree == null) tree = new ArrayList<>();
        if (startBlock.getType().equals(Material.AIR)) return;
        if (!startBlock.getType().equals(Material.BIRCH_LOG) &&
                !startBlock.getType().equals(Material.OAK_LOG) &&
                !startBlock.getType().equals(Material.ACACIA_LOG) &&
                !startBlock.getType().equals(Material.JUNGLE_LOG) &&
                !startBlock.getType().equals(Material.DARK_OAK_LOG) &&
                !startBlock.getType().equals(Material.SPRUCE_LOG) &&
                !startBlock.getType().equals(Material.MANGROVE_LOG) &&
                !startBlock.getType().equals(Material.CHORUS_FLOWER) &&
                !startBlock.getType().equals(Material.CHORUS_PLANT) &&
                !startBlock.getType().equals(Material.BIRCH_LOG)) return;

        Material m = startBlock.getType();

        tree.add(startBlock.getLocation());

        for (BlockFace face : BlockFace.values()) {
            // 排除非正向方块
            if (!face.isCartesian()) continue;
            // 获取连接的方块
            Block relative = startBlock.getRelative(face);
            // 排除和起始方块不一致的方块
            if (!relative.getType().equals(m)) continue;
            // 获取方块的位置
            Location l = relative.getLocation();
            // 排除位置超出界限的方块
            Location distance = l.clone().subtract(location);
            if (Math.abs(distance.getX()) >= 5 || Math.abs(distance.getZ()) >= 5) continue;
            // 排除已在“树”中的位置
            if (tree.stream().anyMatch(x -> x.toVector().equals(l.toVector()))) continue;
            // 添加相关方块的位置，防止该位置的方块被外部因素所改变
            tree.add(l);
            System.out.println(l.toVector());
            // 递归继续获取下一正向位置的方块
            setWholeTree(relative, tree);
        }
    }

    private void LookAt(Location target) {
        Location l = location.getBlock().getLocation();

        double dx = target.getX() - l.getX();
        double dy = target.getY() - l.getY();
        double dz = target.getZ() - l.getZ();
        double dxz = Math.sqrt(Math.pow(dx, 2) + Math.pow(dz, 2));
        float yaw1 = location.getYaw();
        float pitch1 = (float) -Math.atan(dy / dxz);

        if (dx != 0)
            yaw1 = (float) ((dx < 0 ? 1.5 * Math.PI : 0.5 * Math.PI) - Math.atan(dz / dx));
        else if (dz < 0) {
            yaw1 = (float) Math.PI;
        }

        yaw1 = (float) (yaw1 * -180f / Math.PI);
        pitch1 = (float) (pitch1 * 180f / Math.PI);
        if (Math.abs(yaw1) >= 360) yaw1 = 0;
        armorStand.setRotation(yaw1, pitch1);
        new BukkitRunnable() {
            @Override
            public void run() {
                armorStand.setRotation(yaw, pitch);
            }
        }.runTaskLater(plugin, 30);
    }
}