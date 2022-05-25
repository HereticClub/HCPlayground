package org.hcmc.hcplayground.model.player;

import com.google.gson.annotations.Expose;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.utility.Global;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CourseDesigner {
    @Expose
    private Map<EquipmentSlot, ItemStack> equipments = new HashMap<>();
    @Expose
    private Map<Integer, ItemStack> contents = new HashMap<>();
    @Expose
    private Location location;
    @Expose
    private GameMode gameMode;

    @Expose(serialize = false, deserialize = false)
    private final PlayerData data;
    private final Player player;
    private final Plugin plugin;

    public CourseDesigner(PlayerData data) {
        this.data = data;
        this.player = data.getPlayer();
        plugin = HCPlayground.getPlugin();
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

    /**
     * 获取玩家身上所有物品并且放入到储存器，包括背包和装备栏，主副手等物品，并且以json格式保存到玩家文档
     * @param courseLocation 跑酷赛道的位置
     * @param courseName 跑酷赛道的名称
     * @param create True: 表示该赛道正在被创建, False: 表示该赛道正在被修改
     * @throws IOException 文档读写异常
     */
    public void design(Location courseLocation, String courseName, boolean create) throws IOException {
        loadSetting();

        storeEquipments();
        storeContents();
        location = player.getLocation();
        gameMode = player.getGameMode();
        data.isCourseSetting = true;
        if(create) data.courseNames.add(courseName);
        saveSetting();

        player.getInventory().clear();
        player.setGameMode(GameMode.CREATIVE);
        player.teleport(courseLocation);
    }

    /**
     * 将储存器的物品发回给玩家，包括背包和装备栏，主副手等物品，然后清空玩家储存文档
     *
     * @throws IOException
     */
    public void leave() throws IOException {
        loadSetting();

        player.getInventory().clear();
        player.setGameMode(gameMode);
        player.teleport(location);
        returnEquipments();
        returnContents();

        data.isCourseSetting = false;
        this.contents.clear();
        this.equipments.clear();
        saveSetting();
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
        if(!f.exists()) return;

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
