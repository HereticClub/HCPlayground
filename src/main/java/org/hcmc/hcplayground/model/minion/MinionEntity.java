package org.hcmc.hcplayground.model.minion;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.BigDripleaf;
import org.bukkit.block.data.type.Dripleaf;
import org.bukkit.block.data.type.SmallDripleaf;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.enums.MinionCategory;
import org.hcmc.hcplayground.enums.PanelSlotType;
import org.hcmc.hcplayground.enums.MinionType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.hcmc.hcplayground.utility.RomanNumber;
import org.jetbrains.annotations.NotNull;

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

    @Expose(deserialize = false)
    private String id;
    @Expose(deserialize = false)
    private Location location;
    @Expose(deserialize = false)
    private MinionTemplate template;
    @Expose(deserialize = false)
    private MinionTemplate nextLevel;
    @Expose(deserialize = false)
    private List<Location> platformLocations = new ArrayList<>();
    @Expose(deserialize = false)
    private Date lastAcquireTime = new Date();
    @Expose(deserialize = false)
    private ArmorStand armorStand;
    @Expose(deserialize = false)
    private Inventory inventory;
    @Expose(deserialize = false)
    private ItemStack tool = new ItemStack(Material.AIR, 1);

    public MinionEntity() {

    }

    public MinionEntity(ArmorStand armorStand, MinionType type, int level, Location location) {
        this.type = type;
        this.level = level;
        this.armorStand = armorStand;
        EntityEquipment equipment = armorStand.getEquipment();
        if (equipment != null) this.tool = equipment.getItemInMainHand();

        x = location.getX();
        y = location.getY();
        z = location.getZ();
        pitch = location.getPitch();
        yaw = location.getYaw();
        this.location = location;

        World w = location.getWorld();
        if (w == null) return;
        world = w.getName();

        initialPlatform();
    }

    public void initialPlatform() {
        this.template = MinionManager.getMinionTemplate(type, level);
        this.nextLevel = MinionManager.getMinionTemplate(type, level + 1);

        if (location == null) location = getLocation();
        platformLocations.clear();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == 0 && z == 0) continue;
                Location l = new Location(location.getWorld(), location.getX() + x, location.getY() - 1, location.getZ() + z);
                platformLocations.add(l);
            }
        }
    }

    /**
     * 摆放方块并且播放摆放相应方块的声音
     *
     * @param block    要摆放的方块实例
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
     *
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
        Plugin plugin = HCPlayground.getInstance();
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

        Location l = Global.LookAt(this.location, block.getLocation());
        armorStand.setRotation(l.getPitch(), l.getYaw());
    }

    public void harvest(List<Item> drops, Location location) {
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
        // MinionEntity 望向可收获的方块
        Location l = Global.LookAt(this.location, location);
        armorStand.setRotation(l.getPitch(), l.getYaw());
    }

    /**
     * 破坏方块，从方块中收获掉落物品，然后放入MinionEntity实例的袋中
     *
     * @param block 方块实例
     */
    public void harvest(Block block) {
        Plugin plugin = HCPlayground.getInstance();
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
        // MinionEntity 望向可收获的方块
        Location l = Global.LookAt(location, block.getLocation());
        armorStand.setRotation(l.getPitch(), l.getYaw());
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
                    lore.add(upgradeLore.replace("%name%", ib == null ? isUpgrade.getType().name() : ib.getName())
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

    public String getId() {
        return id;
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

    public void setPlatformLocations(List<Location> platformLocations) {
        this.platformLocations = platformLocations;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public void setArmorStand(ArmorStand armorStand) {
        this.armorStand = armorStand;
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

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
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
}
