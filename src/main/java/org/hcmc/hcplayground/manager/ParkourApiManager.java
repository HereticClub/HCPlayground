package org.hcmc.hcplayground.manager;

import io.github.a5h73y.parkour.Parkour;
import io.github.a5h73y.parkour.configuration.ConfigManager;
import io.github.a5h73y.parkour.database.DatabaseManager;
import io.github.a5h73y.parkour.database.TimeEntry;
import io.github.a5h73y.parkour.type.checkpoint.CheckpointManager;
import io.github.a5h73y.parkour.type.course.Course;
import io.github.a5h73y.parkour.type.course.CourseManager;
import io.github.a5h73y.parkour.type.course.CourseSettingsManager;
import io.github.a5h73y.parkour.type.kit.ParkourKitConfig;
import io.github.a5h73y.parkour.type.kit.ParkourKitManager;
import io.github.a5h73y.parkour.type.lobby.LobbyConfig;
import io.github.a5h73y.parkour.type.player.PlayerManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ParkourApiManager {

    public static final String PARKOUR_KIT_DEFAULT = "default";
    private static final Parkour parkour = Parkour.getInstance();

    public ParkourApiManager() {

    }

    public static int getCheckpointCount(String course) {

        CourseManager cm = parkour.getCourseManager();
        Course c = cm.findByName(course);
        if (c == null) return 0;
        return c.getNumberOfCheckpoints();

    }

    public static List<String> getTopBestScoreboard() {
        DatabaseManager dm = parkour.getDatabaseManager();
        CourseManager cm = parkour.getCourseManager();
        List<String> courses = cm.getCourseNames();
        List<String> result = new ArrayList<>();


        result.add(LanguageManager.getString("template.courses_top.hearer"));

        for (String course : courses) {
            List<TimeEntry> topBest = dm.getTopBestTimes(course, 1);
            topBest.sort(Comparator.comparing(TimeEntry::getTime));

            for (TimeEntry entry : topBest) {
                Duration duration = Duration.ofMillis(entry.getTime());

                String time = String.format("%s:%s:%s.%s", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart(), duration.toMillisPart());
                String line = LanguageManager.getString("template.courses_top.line")
                        .replace("%player%", entry.getPlayerName())
                        .replace("%course%", course)
                        .replace("%time%", time);
                result.add(line);
            }
        }

        String footer = LanguageManager.getString("template.courses_top.footer");
        if (!StringUtils.isBlank(footer)) result.add(footer);
        return result;
    }

    public static boolean existCourse(String course) {
        CourseManager cm = parkour.getCourseManager();
        return cm.doesCourseExist(course);
    }

    public static void createCourse(Player player, String course) {
        CourseManager cm = parkour.getCourseManager();
        cm.createCourse(player, course);
    }

    public static void giveParkourKit(Player player, String kit) {
        ParkourKitManager km = parkour.getParkourKitManager();
        km.giveParkourKit(player, kit);
    }

    public static List<String> getParkourKitNameList() {
        ConfigManager cm = parkour.getConfigManager();
        ParkourKitConfig kitConfig = cm.getParkourKitConfig();

        return kitConfig.getAllParkourKitNames().stream().toList();
    }

    public static void selectCourse(Player player, String course) {
        PlayerManager pm = parkour.getPlayerManager();
        CourseManager cm = parkour.getCourseManager();

        if (cm.doesCourseExist(course)) pm.selectCourse(player, course);
    }

    public static void deselectCourse(Player player) {
        PlayerManager pm = parkour.getPlayerManager();
        pm.deselectCourse(player);

        parkour.getConfigManager().reloadConfigs();
    }

    public static void setDisplay(Player player, String course, String text) {
        CourseSettingsManager csm = parkour.getCourseSettingsManager();
        csm.setDisplayName(player, course, text);
    }

    public static void setReady(Player player, String course, boolean ready) {
        CourseSettingsManager csm = parkour.getCourseSettingsManager();
        csm.setReadyStatus(player, course, ready);
    }

    public static void setStartPoint(Player player, String course) {
        CourseSettingsManager csm = parkour.getCourseSettingsManager();
        csm.setStartLocation(player, course);
    }

    public static boolean setCheckpoint(Player player, String course, int index) {
        CheckpointManager cpm = parkour.getCheckpointManager();
        CourseManager cm = parkour.getCourseManager();
        Course c = cm.findByName(course);
        if (c == null) return false;

        if (index == 0) {
            int next = c.getNumberOfCheckpoints() + 1;
            cpm.createCheckpoint(player, next);
        } else {
            cpm.createCheckpoint(player, index);
        }

        return true;
    }

    public static List<String> getLobbyNames() {
        LobbyConfig lc = parkour.getConfigManager().getLobbyConfig();
        return lc.getAllLobbyNames().stream().toList();
    }

    public static void setLinkLobby(Player player, String course, String lobby) {
        CourseSettingsManager csm = parkour.getCourseSettingsManager();
        csm.setCourseToLobbyLink(player, course, lobby);
    }

    public static boolean isLobbyExist(String lobby) {
        LobbyConfig lc = parkour.getConfigManager().getLobbyConfig();
        return lc.doesLobbyExist(lobby);
    }

    public static void deleteCheckpoint(Player player, String course) {
        CheckpointManager cpm = parkour.getCheckpointManager();
        cpm.deleteCheckpoint(player, course);
    }
}
