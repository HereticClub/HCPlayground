package org.hcmc.hcplayground.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.hcmc.hcplayground.HCPlayground;
import org.hcmc.hcplayground.model.parkour.CourseInfo;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CourseManager {

    private static List<CourseInfo> courses = new ArrayList<>();
    private static final Plugin plugin = HCPlayground.getInstance();


    public CourseManager() {

    }

    public static List<CourseInfo> getCourses() {
        return courses;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        courses = Global.deserializeList(yaml, CourseInfo.class);
        Collections.sort(courses);
    }

    public static void save() throws InvalidConfigurationException, IOException {
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

    public static CourseInfo getCourse(String id) {
        CourseInfo c = courses.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
        if (c == null) return null;
        String name = c.getName().replace("§", "&");
        c.setName(name);
        return c;
    }

    public static boolean isAbandoned(String id) {
        return courses.stream().anyMatch(x -> x.getId().equalsIgnoreCase(id) && x.isAbandon());
    }

    /**
     * 创建一条跑酷赛道实例
     * @param name 赛道的名称
     * @return 已创建的赛道实例
     */
    @NotNull
    public static CourseInfo createCourse(String name) {
        //courseId = name;

        // 获得赛道的空余位置
        Location location = nextLocation();
        // 搭建初始建筑平台
        buildPlatform(location);
        // 创建跑道实例
        CourseInfo course = new CourseInfo(location, name);
        // 默认跑道创建时被舍弃
        course.setAbandon(true);
        // 添加到跑道总列表
        courses.add(course);
        // 返回创建的跑道实例
        return course;
    }

    public static boolean existCourse(String id){
        return courses.stream().anyMatch(x->x.getId().equalsIgnoreCase(id));
    }

    public static List<CourseInfo> getAbandons() {
        return courses.stream().filter(CourseInfo::isAbandon).toList();
    }

    public static List<String> getAbandonIdList() {
        List<CourseInfo> courseList = courses.stream().filter(CourseInfo::isAbandon).toList();
        List<String> idList = new ArrayList<>();

        for (CourseInfo c : courseList) {
            idList.add(c.getId());
        }

        return idList;
    }

    public static List<String> getCheckPointList(String course) {
        List<String> list = new ArrayList<>();
        int count = ParkourApiManager.getCheckpointCount(course);
        if (count == 0) return list;

        for (int i=0;i<count;i++) {
            list.add(String.valueOf(i + 1));
        }
        list.add("delete");

        return list;
    }

    public static List<String> getIdList() {
        List<String> list = new ArrayList<>();

        for (CourseInfo c : courses) {
            list.add(c.getId());
        }

        return list;
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
        String world = Global.course.getWorld();
        World w = Bukkit.getWorld(world);

        int worldEdge = 32768;
        int signX = RandomNumber.getRandomBoolean() ? 1 : -1;
        int signZ = RandomNumber.getRandomBoolean() ? 1 : -1;
        int protectRange = Global.course.getProtectRange();
        double y = Global.course.getStartLayer();
        double x;
        double z;

        while (true) {
            x = RandomNumber.getRandomInteger(worldEdge) * signX * protectRange * 2 + 0.5;
            z = RandomNumber.getRandomInteger(worldEdge) * signZ * protectRange * 2 + 0.5;
            int finalX = (int) x;
            int finalZ = (int) z;
            boolean noneMatch = courses.stream().noneMatch(c -> (int) c.getX() == finalX && (int) c.getZ() == finalZ);
            if (noneMatch) break;
        }

        return new Location(w, x, y, z);
    }
}
