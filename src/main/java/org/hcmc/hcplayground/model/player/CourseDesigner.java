package org.hcmc.hcplayground.model.player;

import com.google.gson.annotations.Expose;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.model.parkour.CourseInfo;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CourseDesigner {
    /**
     * 玩家进入赛道设计前的装备
     */
    @Expose
    private Map<EquipmentSlot, ItemStack> equipments = new HashMap<>();

    /**
     * 玩家进入赛道设计前的背包物品
     */
    @Expose
    private Map<Integer, ItemStack> contents = new HashMap<>();

    /**
     * 玩家进入赛道设计前的位置
     */
    @Expose
    private Location location;

    /**
     * 玩家进入赛道设计前的游戏模式
     */
    @Expose
    private GameMode gameMode;

    /**
     * 玩家正在设计的跑酷赛道实例
     */
    private CourseInfo currentCourse;
    private final PlayerData data;
    private final Player player;
    private final Plugin plugin;
    private final World world;
    private final int designRange;
    private boolean outOfDesignRange;

    private BukkitTask designTask;
    private BukkitTask leaveTask;


    private boolean showMessage = false;

    public BukkitTask getLeaveTask() {
        return leaveTask;
    }

    public CourseDesigner(PlayerData data) {
        this.data = data;
        this.player = data.getPlayer();
        plugin = HCPlayground.getPlugin();
        world = Bukkit.getWorld(Global.course.getWorld());
        designRange = Global.course.getDesignRange();
    }

    public Map<EquipmentSlot, ItemStack> getEquipments() {
        return equipments;
    }

    public Map<Integer, ItemStack> getContents() {
        return contents;
    }

    public Location getLocation() {
        return location;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public CourseInfo getCurrentCourse() {
        return currentCourse;
    }

    public boolean isOutOfDesignRange() {
        return outOfDesignRange;
    }

    /**
     * 检测当前位置和跑酷赛道中心点位置的距离，如果在designRange定义值的范围内，返回True，否则返回False
     * @param blockLocation 要检测的位置
     * @return True: 在designRange定义值的范围内<br>
     * False: 超出designRange的定义值
     */
    public boolean RangeDetection(Location blockLocation) {
        // op玩家忽略该检测
        if(player.isOp()) return true;

        World w = blockLocation.getWorld();
        if (w == null) return false;
        if (!w.equals(world)) return true;
        if (!data.isCourseDesigning) return false;

        Location l = blockLocation.subtract(currentCourse.getLocation());
        double x = Math.abs(l.getX());
        double z = Math.abs(l.getZ());
        return x <= designRange && z <= designRange;
    }

    public boolean ContainerDetection(@NotNull Inventory inv) {
        // op玩家忽略该检测
        if (player.isOp()) return true;
        World w = player.getWorld();
        if (!w.equals(world)) return true;
        if (!data.isCourseDesigning) return false;
        Location invLocation = inv.getLocation();
        if (invLocation == null) return true;

        Location l = invLocation.subtract(currentCourse.getLocation());
        double x = Math.abs(l.getX());
        double z = Math.abs(l.getZ());
        return x <= designRange && z <= designRange;
    }

    public void EdgeDetection(Location playerLocation) {
        if(currentCourse == null) return;

        World w = playerLocation.getWorld();
        if (w == null) return;
        if (!w.equals(world)) return;
        if (!data.isCourseDesigning) return;

        Location l = playerLocation.subtract(currentCourse.getLocation());
        double x = Math.abs(l.getX());
        double z = Math.abs(l.getZ());
        outOfDesignRange = x >= designRange || z >= designRange;

        if (!outOfDesignRange && showMessage) {
            player.sendMessage(LocalizationManager.getMessage("enterCourseRange", player));
            showMessage = false;
        }
        if (outOfDesignRange && !showMessage) {
            player.sendMessage(LocalizationManager.getMessage("outOfCourseRange", player));
            showMessage = true;
        }
    }

    /**
     * 获取玩家身上所有物品并且放入到储存器，包括背包和装备栏，主副手等物品，并且以json格式保存到玩家文档
     *
     * @param course 跑酷赛道实例
     * @param create True: 表示该赛道正在被创建, False: 表示该赛道正在被修改
     * @throws IOException 文档读写异常
     */
    public void design(@NotNull CourseInfo course, boolean create) throws IOException, IllegalAccessException, InvalidConfigurationException {
        // 非op玩家必须把背包和装备放入"储物柜"，并且游戏模式被设置为CREATIVE
        if (!player.isOp()) {
            // 储存身上装备和背包物品
            storeEquipments();
            storeContents();
            // 保留GameMode
            gameMode = player.getGameMode();
            // 清空身上装备及背包物品
            player.getInventory().clear();
            // 给与玩家绕过WorldGuard世界保护权限
            String perms = String.format(PermissionManager.PERMISSION_WORLDGUARD_REGION_BYPASS, world.getName());
            PermissionManager.addPermission(player, perms);
            // 创建赛道时，添加赛道Id到玩家列表
            if (create) data.courseIds.add(course.getId());
            // 等待进入赛道设计模式
            int waitFor = Global.course.getWaitFor();
            if (waitFor <= 0) waitFor = 3;
            // 跑酷插件会强制玩家进入世界后改变玩家的游戏模式
            player.sendMessage(LocalizationManager.getMessage("courseEnteringDesignMode", player).replace("%second%", String.valueOf(waitFor)));
            designTask = new BukkitRunnable() {
                @Override
                public void run() {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(LocalizationManager.getMessage("courseDesignModeEntered", player));
                }
            }.runTaskLater(plugin, waitFor * 20L);
        }

        // 赛道设计模式时，获取玩家进入模式前的位置
        if (!data.isCourseDesigning) location = player.getLocation();
        // 保存之前的配置
        saveSetting();
        // 安全传送
        Block block = world.getBlockAt(course.getLocation());
        if (!block.getType().isOccluding()) block.setType(Material.STONE);
        // 保存当前赛道的实例，设计进入设计模式的标记
        currentCourse = course;
        data.isCourseDesigning = true;
        // 保存玩家数据
        PlayerManager.setPlayerData(player, data);
        // 传送到赛道
        player.teleport(course.getLocation().add(0.5, 1, 0.5));
        if (!create) ParkourApiManager.selectCourse(player, course.getName());
    }

    /**
     * 将储存器的物品发回给玩家，包括背包和装备栏，主副手等物品，然后清空玩家储存文档
     *
     * @throws IOException
     */
    public void leave() throws IOException, IllegalAccessException, InvalidConfigurationException {
        // 加载"储物柜"所有物品
        loadSetting();
        // 非op玩家必须从"储物柜"拿回之前的物品，并且设置游戏模式为进入设计跑道之前的游戏模式
        if (!player.isOp()) {
            player.getInventory().clear();
            // 取消WorldGuard保护权限
            String perms = String.format(PermissionManager.PERMISSION_WORLDGUARD_REGION_BYPASS, world.getName());
            PermissionManager.removePermission(player, perms);
            // 等待离开赛道设计模式，并且返还装备
            int waitFor = Global.course.getWaitFor();
            if (waitFor <= 0) waitFor = 3;
            if(designTask != null && !designTask.isCancelled()) designTask.cancel();
            leaveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    returnEquipments();
                    returnContents();
                    contents.clear();
                    equipments.clear();
                    player.setGameMode(gameMode);
                    try {
                        saveSetting();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    player.sendMessage(LocalizationManager.getMessage("courseDesignModeLeave", player));
                }
            }.runTaskLater(plugin, waitFor * 20L);
        }

        currentCourse = null;
        ParkourApiManager.deselectCourse(player);

        if (location != null) player.teleport(location);
        data.isCourseDesigning = false;
        PlayerManager.setPlayerData(player, data);
    }

    /**
     * 为正在设计的跑酷赛道添加一个检查点
     * @param index 检查点的序号，如果当前参数值为0，则序号会自动获取，值为检查点总数+1
     * @return True: 添加了一个检查点<br>False: 不能添加检查点，可能性为跑道不存在
     */
    // What the hell the 'Boolean method is always inverted'
    public boolean addCheckpoint(int index){
        return ParkourApiManager.setCheckpoint(player, currentCourse.getName(), index);
    }

    public void deleteCheckpoint() {
        ParkourApiManager.deleteCheckpoint(player, currentCourse.getName());
    }

    public boolean teleport(String course) {
        CourseInfo c = CourseManager.getCourse(course);
        if (c == null) return false;

        player.teleport(c.getLocation().add(0, 1, 0));

        return true;
    }

    public void getParkourKit() {
        ParkourApiManager.giveParkourKit(player, ParkourApiManager.PARKOUR_KIT_DEFAULT);
    }

    public List<String> list() throws IOException, IllegalAccessException, InvalidConfigurationException {
        if (!player.isOp()) {
            return data.courseIds;
        } else {
            List<String> values = new ArrayList<>();
            for (CourseInfo c : CourseManager.getCourses()) {
                values.add(c.getId());
            }
            return values;
        }
    }

    public List<String> abandons() {
        List<String> values = new ArrayList<>();
        List<CourseInfo> list = CourseManager.getAbandons();

        for (CourseInfo c : list) {
            values.add(c.getId());
        }

        return values;
    }

    public void abandon(@NotNull CourseInfo course) throws IOException, InvalidConfigurationException, IllegalAccessException {
        course.setAbandon(true);
        data.courseIds.remove(course.getId());
        CourseManager.save();
        PlayerManager.setPlayerData(player, data);

        if(!player.isOp()) leave();
    }

    public void claim(@NotNull CourseInfo course) throws IOException, InvalidConfigurationException, IllegalAccessException {
        course.setAbandon(false);
        data.courseIds.add(course.getId());
        CourseManager.save();
        PlayerManager.setPlayerData(player, data);

        design(course, false);
    }

    public void startPoint(@NotNull CourseInfo course) {
        String courseName = course.getName().replace("§","&");

        if (!ParkourApiManager.existCourse(courseName)) ParkourApiManager.createCourse(player, courseName);
        ParkourApiManager.setStartPoint(player, courseName);

        Block block;
        BlockFace face = player.getFacing();
        switch (face) {
            case EAST -> block = player.getWorld().getBlockAt(player.getLocation().add(1, -1, 0));
            case WEST -> block = player.getWorld().getBlockAt(player.getLocation().add(-1, -1, 0));
            case NORTH -> block = player.getWorld().getBlockAt(player.getLocation().add(0, -1, -1));
            case SOUTH -> block = player.getWorld().getBlockAt(player.getLocation().add(0, -1, 1));
            default -> block = player.getWorld().getBlockAt(player.getLocation().add(0, -1, 0));
        }

        block.setType(Material.WHITE_WOOL);
    }

    public void setReady(boolean ready) {
        ParkourApiManager.setReady(player, currentCourse.getName(), ready);
    }

    public void setDisplayName(String display) {
        ParkourApiManager.setDisplay(player, currentCourse.getName(), display);
    }

    private void saveSetting() throws IOException {
        UUID uuid = player.getUniqueId();
        String file = String.format("%s/storage/%s.parkour.json", plugin.getDataFolder(), uuid);
        String value = Global.GsonObject.toJson(this, CourseDesigner.class);

        Path path = Paths.get(file);
        Files.writeString(path, value, Charset.defaultCharset());
    }

    private void loadSetting() throws IOException {
        UUID uuid = player.getUniqueId();
        String file = String.format("%s/storage/%s.parkour.json", plugin.getDataFolder(), uuid);

        File f = new File(file);
        if (!f.exists()) return;

        Path path = Paths.get(file);
        CourseDesigner storage;

        String value = Files.readString(path, Charset.defaultCharset());
        storage = Global.GsonObject.fromJson(value, CourseDesigner.class);

        this.equipments = storage.getEquipments();
        this.contents = storage.getContents();
        this.location = storage.getLocation();
        this.gameMode = storage.getGameMode();
    }

    private void storeEquipments() {
        PlayerInventory inv = player.getInventory();
        EquipmentSlot[] slots = EquipmentSlot.values();
        equipments.clear();

        for (EquipmentSlot s : slots) {
            if (s.equals(EquipmentSlot.HAND)) continue;
            ItemStack is = inv.getItem(s);
            equipments.put(s, is);
        }
    }

    private void storeContents() {
        PlayerInventory inv = player.getInventory();
        contents.clear();

        for (int index = 0; index < 36; index++) {
            ItemStack is = inv.getItem(index);
            if (is == null) continue;

            contents.put(index, is);
        }
    }

    private void returnContents() {
        PlayerInventory inv = player.getInventory();
        Set<Integer> index = contents.keySet();

        for (int i : index) {
            ItemStack is = contents.get(i);
            inv.setItem(i, is);
        }
    }

    private void returnEquipments() {
        PlayerInventory inv = player.getInventory();
        EquipmentSlot[] slots = EquipmentSlot.values();

        for (EquipmentSlot s : slots) {
            ItemStack is = equipments.get(s);
            inv.setItem(s, is);
        }
    }
}
