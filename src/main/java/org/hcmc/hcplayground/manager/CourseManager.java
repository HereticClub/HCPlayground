package org.hcmc.hcplayground.manager;

import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.parkour.CourseInfo;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseManager {

    private static List<CourseInfo> courses = new ArrayList<>();
    private static String courseName;
    private static final Plugin plugin = HCPlayground.getPlugin();


    public CourseManager() {

    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        courses = Global.SetItemList(yaml, CourseInfo.class);
    }

    public static Location createCourse(String name) {
        courseName = name;

        Location location = nextLocation();
        buildPlatform(location);

        return location;
    }

    public static boolean existCourse(String name){
        return courses.stream().anyMatch(x->x.getId().equalsIgnoreCase(name));
    }

    public static Location getCourseLocation(String name) {
        courseName = name;
        CourseInfo course = courses.stream().filter(x -> x.getId().equalsIgnoreCase(name)).findAny().orElse(null);
        if (course == null) return null;
        return course.getLocation();
    }

    private static void buildPlatform(Location location) {
        double expand = 4;

        int minX = (int) (location.getX() - expand);
        int maxX = (int) (location.getX() + expand);
        int minZ = (int) (location.getZ() - expand);
        int maxZ = (int) (location.getZ() + expand);
        int y = (int) location.getY();
        World w = location.getWorld();
        assert w != null;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                w.getBlockAt(x, y, z).setType(Material.STONE);
            }
        }
    }

    private static Location nextLocation() {
        String world = Global.ParkourAdmin.getWorld();
        World w = Bukkit.getWorld(world);

        int worldEdge = 32768;
        int signX = RandomNumber.getRandomBoolean() ? 1 : -1;
        int signZ = RandomNumber.getRandomBoolean() ? 1 : -1;
        int protectRange = Global.ParkourAdmin.getProtectRange();
        double y = Global.ParkourAdmin.getStartLayer();
        double x;
        double z;

        while (true) {
            x = RandomNumber.getRandomNumber(worldEdge) * signX * protectRange * 2 + 0.5;
            z = RandomNumber.getRandomNumber(worldEdge) * signZ * protectRange * 2 + 0.5;
            int finalX = (int) x;
            int finalZ = (int) z;
            boolean noneMatch = courses.stream().noneMatch(c -> (int) c.getX() == finalX && (int) c.getZ() == finalZ);
            if (noneMatch) break;
        }

        Location location = new Location(w, x, y, z);
        CourseInfo course = new CourseInfo(location, courseName);
        courses.add(course);
        return location;
    }

    public static void saveFile() throws InvalidConfigurationException, IOException {
        Map<String, CourseInfo> map = new HashMap<>();

        for (CourseInfo c : courses) {
            map.put(c.getId(), c);
        }
        String value = Global.GsonObject.toJson(map);

        YamlConfiguration y = new YamlConfiguration();
        y.loadFromString(value);
        File f = new File(String.format("%s/%s", plugin.getDataFolder(), Global.FILE_COURSE));
        y.save(f);
    }
}
