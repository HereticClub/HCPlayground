package org.hcmc.hcplayground.model.player;

import com.google.gson.annotations.Expose;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
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
import java.util.function.Predicate;

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
    private String currentCourseId;
    private final PlayerData data;
    private final Player player;
    private final Plugin plugin;
    private final World world;
    private final int designRange;
    private final int protectRange;
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
        plugin = HCPlayground.getInstance();
        world = Bukkit.getWorld(Global.course.getWorld());
        designRange = Global.course.getDesignRange();
        protectRange = Global.course.getProtectRange();
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

    public String getCurrentCourseId() {
        return currentCourseId;
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

        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if(course == null) return false;

        Location l = blockLocation.subtract(course.getLocation());
        double x = Math.abs(l.getX());
        double z = Math.abs(l.getZ());
        return x <= designRange && z <= designRange;
    }

    /**
     * 检测要互动的方块是否在某条跑酷赛道上<br>
     * 如果不在某条跑酷赛道上，则由系统决定是否可互动<br>
     * 反之，如果玩家不在设计模式，则不可互动
     * @return True: 允许互动<br>False: 不允许互动
     */
    public boolean InteractDetection(@NotNull Block block) {
        // op玩家忽略该检测
        if (player.isOp()) return true;
        // 根据互动方块的位置，预测该方块所在的跑酷赛道
        List<CourseInfo> courses = CourseManager.getCourses();
        CourseInfo course = courses.stream().filter(predicateCourse(block)).findAny().orElse(null);
        if (course == null) return true;

        return data.isCourseDesigning;
    }

    public void EdgeDetection(Location playerLocation) {
        if (currentCourseId == null) return;

        World w = playerLocation.getWorld();
        if (w == null) return;
        if (!w.equals(world)) return;
        if (!data.isCourseDesigning) return;

        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if (course == null) return;

        Location l = playerLocation.subtract(course.getLocation());
        double x = Math.abs(l.getX());
        double z = Math.abs(l.getZ());
        outOfDesignRange = x >= designRange || z >= designRange;

        if (!outOfDesignRange && showMessage) {
            player.sendMessage(LanguageManager.getString("enterCourseRange", player));
            showMessage = false;
        }
        if (outOfDesignRange && !showMessage) {
            player.sendMessage(LanguageManager.getString("outOfCourseRange", player));
            showMessage = true;
        }
    }

    /**
     * 获取玩家身上所有物品并且放入到储存器，包括背包和装备栏，主副手等物品，并且以json格式保存到玩家文档
     *
     * @param id 跑酷赛道实例的id编号
     * @param create True: 表示该赛道正在被创建, False: 表示该赛道正在被修改
     */
    public void design(@NotNull String id, boolean create) throws IOException, IllegalAccessException, InvalidConfigurationException {
        // 赛道位置实例
        CourseInfo course = CourseManager.getCourse(id);
        if (course == null) return;
        // 赛道设计模式时，获取玩家进入模式前的位置
        // 下面一行代码主要为op玩家设计
        // 因为op玩家可以无限制的随意设计任何赛道
        // 意味着可以在不离开设计模式时重复调用design()方法
        // 而非op玩家必须调用了leave()方法后才能再次调用design方法
        // 因此只记录第一次进入设计模式时的玩家位置
        if (!data.isCourseDesigning) location = player.getLocation();
        // 安全传送，脚下的方块如果是透明的不受碰撞影响的方块，则改变为STONE
        Block block = world.getBlockAt(course.getLocation());
        if (!block.getType().isOccluding()) block.setType(Material.STONE);
        // 保存当前正在设计的赛道实例Id
        currentCourseId = course.getId();
        // 进入设计模式
        data.isCourseDesigning = true;
        // 传送到赛道
        player.teleport(course.getLocation().add(0.5, 1, 0.5));
        // 选择跑酷赛道实例
        if (!create) ParkourApiManager.selectCourse(player, course.getName());
        // 非op玩家必须把背包和装备放入"储物柜"，并且游戏模式被设置为CREATIVE
        if (!player.isOp()) {
            // 等待进入赛道设计模式
            // 主要是等待Multiverse-Inventories插件完成它的任务
            int waitFor = Global.course.getWaitFor();
            if (waitFor <= 0) waitFor = 3;
            // 跑酷插件会强制玩家进入世界后改变玩家的游戏模式
            player.sendMessage(LanguageManager.getString("courseEnteringDesignMode", player).replace("%second%", String.valueOf(waitFor)));
            designTask = new BukkitRunnable() {
                @Override
                public void run() {
                    player.setGameMode(GameMode.CREATIVE);
                    player.sendMessage(LanguageManager.getString("courseDesignModeEntered", player));
                    return;
                }
            }.runTaskLater(plugin, waitFor * 20L);
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
            if (create) data.courseIds.add(id);
        }

        // 保存之前的配置，写入到玩家json文档
        // 包括玩家身上的物品，玩家传送前的位置，玩家的游戏模式
        saveSetting();
        // 保存玩家数据
        PlayerManager.setPlayerData(player, data);
    }

    /**
     * 将储存器的物品发回给玩家，包括背包和装备栏，主副手等物品，然后清空玩家储存文档
     */
    public void leave() throws IOException, IllegalAccessException, InvalidConfigurationException {
        // 加载"储物柜"所有物品
        loadSetting();
        // 非op玩家必须从"储物柜"拿回之前的物品，并且设置游戏模式为进入设计跑道之前的游戏模式
        if (!player.isOp()) {
            // 清除玩家身上所有物品
            player.getInventory().clear();
            // 返还玩家之前的装备和物品
            returnEquipments();
            returnContents();
            player.setGameMode(gameMode);
            // 清空"储物柜"并且保存到文档
            contents.clear();
            equipments.clear();
            saveSetting();
            // 取消WorldGuard保护权限
            String perms = String.format(PermissionManager.PERMISSION_WORLDGUARD_REGION_BYPASS, world.getName());
            PermissionManager.removePermission(player, perms);
        }
        // 传送玩家回到原来的世界
        if (location != null) player.teleport(location);
        // 离开设计模式
        data.isCourseDesigning = false;
        // 取消选择跑酷赛道实例
        currentCourseId = null;
        ParkourApiManager.deselectCourse(player);
        // 保存玩家数据
        PlayerManager.setPlayerData(player, data);
        // 以下代码仅仅只是制造一个效果
        // (形式上)需要玩家等待几秒钟才能离开
        if (!player.isOp()) {
            // 等待离开赛道设计模式，并且返还装备
            int waitFor = Global.course.getWaitFor();
            if (waitFor <= 0) waitFor = 3;
            if (designTask != null && !designTask.isCancelled()) designTask.cancel();
            leaveTask = new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(LanguageManager.getString("courseDesignModeLeave", player));
                    return;
                }
            }.runTaskLater(plugin, waitFor * 20L);
        }
    }

    /**
     * 为正在设计的跑酷赛道添加一个检查点
     * @param index 检查点的序号，如果当前参数值为0，则序号会自动获取，值为检查点总数+1
     * @return True: 添加了一个检查点<br>False: 不能添加检查点，可能性为跑道不存在
     */
    // What the hell the 'Boolean method is always inverted'
    public boolean addCheckpoint(int index){
        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if(course == null) return false;

        return ParkourApiManager.setCheckpoint(player, course.getName(), index);
    }

    public void deleteCheckpoint() {
        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if(course == null) return;

        ParkourApiManager.deleteCheckpoint(player, course.getName());
    }

    public boolean teleport(String id) {
        // 赛道存在检测
        CourseInfo c = CourseManager.getCourse(id);
        if (c == null) return false;
        // 安全传送检测
        Block block = world.getBlockAt(c.getLocation());
        if (!block.getType().isOccluding()) return false;

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

    public void abandon(@NotNull String id) throws IOException, InvalidConfigurationException, IllegalAccessException {
        CourseInfo course = CourseManager.getCourse(id);
        if (course == null) return;

        course.setAbandon(true);
        data.courseIds.remove(course.getId());
        CourseManager.save();
        PlayerManager.setPlayerData(player, data);

        if(!player.isOp()) leave();
    }

    public void claim(@NotNull String id) throws IOException, InvalidConfigurationException, IllegalAccessException {
        CourseInfo course = CourseManager.getCourse(id);
        if (course == null) return;

        course.setAbandon(false);
        data.courseIds.add(course.getId());
        CourseManager.save();
        PlayerManager.setPlayerData(player, data);

        design(id, false);
    }

    public void startPoint(@NotNull String id) {
        CourseInfo course = CourseManager.getCourse(id);
        if (course == null) return;

        String courseName = course.getName();
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
        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if(course == null) return;

        ParkourApiManager.setReady(player, course.getName(), ready);
    }

    public void setLinkLobby(String lobby) {
        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if (course == null) return;

        ParkourApiManager.setLinkLobby(player, course.getName(), lobby);
    }

    public void setDisplayName(String display) {
        CourseInfo course = CourseManager.getCourse(currentCourseId);
        if(course == null) return;

        ParkourApiManager.setDisplay(player, course.getName(), display);
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

    private Predicate<CourseInfo> predicateCourse(Block block){
        World world = block.getWorld();
        Location location = block.getLocation();

        return x -> x.getWorld().equalsIgnoreCase(world.getName()) && Math.abs(x.getLocation().subtract(location).getX()) <= protectRange && Math.abs(x.getLocation().subtract(location).getZ()) <= protectRange;
    }
}
