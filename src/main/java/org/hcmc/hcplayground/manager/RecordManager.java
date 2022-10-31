package org.hcmc.hcplayground.manager;

import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.item.ItemBase;
import org.hcmc.hcplayground.model.minion.MinionEntity;
import org.hcmc.hcplayground.model.recipe.HCItemBlockRecord;
import org.hcmc.hcplayground.utility.Global;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RecordManager {
    private static List<HCItemBlockRecord> hcItemBlockRecords = new ArrayList<>();
    private static List<MinionEntity> minionEntities = new ArrayList<>();
    private static final Plugin plugin = HCPlayground.getInstance();

    /**
     * 各种记录的存档周期，单位: 秒
     */
    public static final int ARCHIVE_PERIOD = 600;

    public RecordManager() {

    }

    public static List<HCItemBlockRecord> getHcItemBlockRecords() {
        return hcItemBlockRecords;
    }

    public static List<MinionEntity> getMinionEntities() {
        return minionEntities;
    }

    public static void addHCItemRecord(HCItemBlockRecord item) {
        if (!existHCItemRecord(item.getLocation())) hcItemBlockRecords.add(item);
    }

    public static void addMinionRecord(MinionEntity item) {
        if (!existMinionEntity(item.getLocation())) minionEntities.add(item);
    }

    public static boolean existMinionEntity(Location location) {
        return minionEntities.stream().anyMatch(x -> x.getLocation().toVector().equals(location.toVector()));
    }

    public static boolean existMinionEntity(UUID armorstandUuid) {
        return minionEntities.stream().anyMatch(x -> x.getUuid().equals(armorstandUuid));
    }

    private static boolean existHCItemRecord(Location l) {
        World world = l.getWorld();
        String worldName = "";
        if (world != null) worldName = world.getName();

        String finalWorldName = worldName;
        return hcItemBlockRecords.stream().anyMatch(x -> x.getX() == l.getX()
                && x.getY() == l.getY()
                && x.getZ() == l.getZ()
                && x.getWorld().equalsIgnoreCase(finalWorldName)
                && x.getPitch() == l.getPitch()
                && x.getYaw() == l.getYaw());
    }

    public static HCItemBlockRecord getHCItemRecord(Location location) {
        World world = location.getWorld();
        String worldName = "";
        if (world != null) worldName = world.getName();

        String finalWorldName = worldName;
        return hcItemBlockRecords.stream().filter(x -> x.getX() == location.getX()
                && x.getY() == location.getY()
                && x.getZ() == location.getZ()
                && x.getWorld().equalsIgnoreCase(finalWorldName)
                && x.getPitch() == location.getPitch()
                && x.getYaw() == location.getYaw()).findAny().orElse(null);
    }

    public static MinionEntity getMinionRecord(Location location) {
        World world = location.getWorld();
        String worldName = "";
        if (world != null) worldName = world.getName();

        String finalWorldName = worldName;
        return minionEntities.stream().filter(x -> x.getX() == location.getX()
                && x.getY() == location.getY()
                && x.getZ() == location.getZ()
                && x.getWorld().equalsIgnoreCase(finalWorldName)).findAny().orElse(null);
    }

    public static MinionEntity getMinionRecord(UUID armorstandUuid) {
        return minionEntities.stream().filter(x -> x.getUuid().equals(armorstandUuid)).findAny().orElse(null);
    }

    public static void removeMinionRecord(UUID armorstandUuid) {
        minionEntities.removeIf(x -> x.getUuid().equals(armorstandUuid));
    }

    public static void removeHCItemRecord(HCItemBlockRecord item) {
        hcItemBlockRecords.remove(item);
    }

    public static void Load() throws IOException {
        loadHCItemRecord();
        loadMinionRecord();
    }

    public static void Save() throws IOException {
        Global.LogMessage("Saving Minion Records ...");
        String jsonMinions = Global.GsonObject.toJson(minionEntities);
        Path pMinion = Paths.get(String.format("%s/%s", plugin.getDataFolder(), Global.FILE_RECORD_MINION));
        Files.write(pMinion, jsonMinions.getBytes());

        Global.LogMessage("Saving Crazy Block Records ...");
        String jsonBlocks = Global.GsonObject.toJson(hcItemBlockRecords);
        Path pBlock = Paths.get(String.format("%s/%s", plugin.getDataFolder(), Global.FILE_RECORD_BLOCK));
        Files.write(pBlock, jsonBlocks.getBytes());
    }

    private static void loadMinionRecord() throws IOException {
        Path p = Paths.get(String.format("%s/%s", plugin.getDataFolder(), Global.FILE_RECORD_MINION));
        if (!p.toFile().exists()) return;
        String jsonMinion = new String(Files.readAllBytes(p));
        Type _type = new TypeToken<List<MinionEntity>>() {
        }.getType();
        minionEntities = Global.GsonObject.fromJson(jsonMinion, _type);
        for (MinionEntity record : minionEntities) {
            Entity entity = Bukkit.getEntity(record.getUuid());
            if (entity instanceof ArmorStand armorStand) {
                // 获取第一个ArmorStand实体
                record.initial(armorStand, record.getType(), record.getLevel());
            } else {
                // 生成ArmorStand实体
                if (entity != null) entity.remove();
                ItemStack is = MinionManager.getMinionStack(record.getType(), record.getLevel(), 1);
                MinionEntity other = MinionManager.spawnMinion(record.getLocation(), is);
                if (other != null) record.initial(other);
            }
        }
    }

    private static void loadHCItemRecord() throws IOException {
        Path p = Paths.get(String.format("%s/%s", plugin.getDataFolder(), Global.FILE_RECORD_BLOCK));
        if(!p.toFile().exists()) return;
        String jsonBlock = new String(Files.readAllBytes(p));
        Type _type = new TypeToken<List<HCItemBlockRecord>>() {
        }.getType();
        hcItemBlockRecords = Global.GsonObject.fromJson(jsonBlock, _type);

        for (HCItemBlockRecord record : hcItemBlockRecords) {
            ItemBase ib = ItemManager.findItemById(record.getName());
            if (ib == null) continue;
            World w = Bukkit.getWorld(record.getWorld());
            if (w == null) continue;

            Location l = record.getLocation();
            Block b = w.getBlockAt(l);
            if (!b.getType().equals(ib.getMaterial().value)) b.setType(ib.getMaterial().value);
        }
    }
}
