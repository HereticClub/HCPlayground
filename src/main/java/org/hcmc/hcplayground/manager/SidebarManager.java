package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.scoreboard.ScoreboardItem;
import org.hcmc.hcplayground.utility.Global;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SidebarManager {

    public static final String SCOREBOARD_CRITERIA_DEFAULT = "default";
    public static final String SCOREBOARD_CRITERIA_DUMMY = "dummy";

    private static List<ScoreboardItem> scoreboards;

    public SidebarManager() {

    }

    public static List<ScoreboardItem> getScoreboards() {
        return scoreboards;
    }

    public static void Load(YamlConfiguration yaml) throws IllegalAccessException {
        scoreboards = Global.SetItemList(yaml, ScoreboardItem.class);
    }

    /**
     * 获取和worldName匹配的第一个ScoreboardItem实例<br>
     * 优先获取配置了worlds属性的实例，若没有，则返回没有配置world属性的实例
     * @param worldName 世界名城
     * @return 和worldName匹配的Scoreboard实例
     */
    public static ScoreboardItem getItemByWorld(@NotNull String worldName) {
        ScoreboardItem emptyWorldItem = null;
        ScoreboardItem specWorldItem = null;

        for (ScoreboardItem sb : scoreboards) {
            // 获取第一个没有设置worlds属性的scoreboard实例
            if ((sb.getWorlds() == null || sb.getWorlds().size() <= 0) && emptyWorldItem == null) {
                emptyWorldItem = sb;
                continue;
            }
            // 找到第一个匹配worldName的scoreboard实例
            if (sb.getWorlds().contains(worldName)) {
                specWorldItem = sb;
                break;
            }
        }
        // 优先返回specWorldItem，若为null则返回emptyWorldItem
        return specWorldItem == null ? emptyWorldItem : specWorldItem;
    }
}
