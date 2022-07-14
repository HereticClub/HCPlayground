package org.hcmc.hcplayground.manager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.minion.MinionRecord;
import org.hcmc.hcplayground.model.minion.MinionTemplate;
import org.hcmc.hcplayground.model.recipe.CrazyBlockRecord;
import org.hcmc.hcplayground.model.item.Crazy;
import org.hcmc.hcplayground.utility.Global;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class RecordManager {
    private static final String SECTION_KEY_CRAZY = "crazy";
    private static final String SECTION_KEY_MINION = "minion";

    private static List<CrazyBlockRecord> crazyBlockRecords = new ArrayList<>();
    private static List<MinionRecord> minionRecords = new ArrayList<>();
    //private static YamlConfiguration yamlRecord;
    private static final Plugin plugin = HCPlayground.getInstance();


    public RecordManager() {

    }

    public static List<CrazyBlockRecord> getCrazyRecords() {
        return crazyBlockRecords;
    }

    public static List<MinionRecord> getMinionRecords() {
        return minionRecords;
    }

    public static void addCrazyRecord(CrazyBlockRecord item) {
        if (!existCrazyRecord(item.toLocation())) crazyBlockRecords.add(item);
    }

    public static void addMinionRecord(MinionRecord item) {
        if (!existMinionRecord(item.getLocation())) minionRecords.add(item);
    }

    private static boolean existMinionRecord(Location location) {
        return minionRecords.stream().anyMatch(x -> x.getLocation().toVector().equals(location.toVector()));
    }

    private static boolean existCrazyRecord(Location l) {
        World world = l.getWorld();
        String worldName = "";
        if (world != null) worldName = world.getName();

        String finalWorldName = worldName;
        return crazyBlockRecords.stream().anyMatch(x -> x.getX() == l.getX()
                && x.getY() == l.getY()
                && x.getZ() == l.getZ()
                && x.getWorld().equalsIgnoreCase(finalWorldName)
                && x.getPitch() == l.getPitch()
                && x.getYaw() == l.getYaw());
    }

    public static CrazyBlockRecord findCrazyRecord(Location location) {
        World world = location.getWorld();
        String worldName = "";
        if (world != null) worldName = world.getName();

        String finalWorldName = worldName;
        return crazyBlockRecords.stream().filter(x -> x.getX() == location.getX()
                && x.getY() == location.getY()
                && x.getZ() == location.getZ()
                && x.getWorld().equalsIgnoreCase(finalWorldName)
                && x.getPitch() == location.getPitch()
                && x.getYaw() == location.getYaw()).findAny().orElse(null);
    }

    public static void removeCrazyRecord(CrazyBlockRecord item) {
        crazyBlockRecords.remove(item);
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        loadCrazyRecord(yaml);
        loadMinionRecord(yaml);
    }

    public static void Save() throws IOException, IllegalAccessException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.createSection(SECTION_KEY_CRAZY, saveCrazyRecord());
        yaml.createSection(SECTION_KEY_MINION, saveMinionRecord());
        yaml.save(String.format("%s/%s", plugin.getDataFolder(), Global.FILE_RECORD));
    }

    private static void loadMinionRecord(YamlConfiguration yaml) throws IllegalAccessException {
        ConfigurationSection section = yaml.getConfigurationSection(SECTION_KEY_MINION);
        if (section == null) return;

        minionRecords = Global.SetItemList(section, MinionRecord.class);
        for (MinionRecord record : minionRecords) {
            MinionTemplate m = MinionManager.getMinionTemplate(record.getType(), record.getLevel());
            if(m == null) continue;

            World w = record.getLocation().getWorld();
            if(w == null) continue;

            Location l = record.getLocation();
            Block b = w.getBlockAt(l);
            System.out.println(b);
        }
    }

    private static void loadCrazyRecord(YamlConfiguration yaml) throws IllegalAccessException {
        ConfigurationSection section = yaml.getConfigurationSection(SECTION_KEY_CRAZY);
        if (section == null) return;

        crazyBlockRecords = Global.SetItemList(section, CrazyBlockRecord.class);
        for (CrazyBlockRecord record : crazyBlockRecords) {
            Crazy ib = (Crazy) ItemManager.findItemById(record.getName());
            if (ib == null) continue;

            World w = Bukkit.getWorld(record.getWorld());
            if (w == null) continue;

            Location l = new Location(w, record.getX(), record.getY(), record.getZ(), record.getYaw(), record.getPitch());
            Block b = w.getBlockAt(l);
            if (!b.getType().equals(ib.getMaterial().value)) b.setType(ib.getMaterial().value);
        }
    }

    private static Map<UUID, Object> saveMinionRecord() throws IllegalAccessException {
        Map<UUID, Object> mapYaml = new HashMap<>();
        for (MinionRecord m : minionRecords) {
            Map<String, Object> mapRecord = serializeRecord(m);
            mapYaml.put(UUID.randomUUID(), mapRecord);
        }
        return mapYaml;
    }

    private static Map<UUID, Object> saveCrazyRecord() throws IllegalAccessException {
        Map<UUID, Object> mapYaml = new HashMap<>();
        for (CrazyBlockRecord r : crazyBlockRecords) {
            Map<String, Object> mapRecord = serializeRecord(r);
            mapYaml.put(UUID.randomUUID(), mapRecord);
        }
        return mapYaml;
    }

    private static Map<String, Object> serializeRecord(Object obj) throws IllegalAccessException {
        Field[] fields = obj.getClass().getDeclaredFields();
        Map<String, Object> mapRecord = new HashMap<>();
        for (Field f : fields) {
            Expose e = f.getDeclaredAnnotation(Expose.class);
            SerializedName s = f.getDeclaredAnnotation(SerializedName.class);
            if (e == null || !e.serialize()) continue;

            f.setAccessible(true);
            String propertyName = s == null || StringUtils.isEmpty(s.value()) ? f.getName() : s.value();
            mapRecord.put(propertyName, f.get(obj));
        }
        return mapRecord;
    }
}
