package org.hcmc.hcplayground.model.scoreboard;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.hcmc.hcplayground.manager.SidebarManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreboardItem {
    /**
     * 计分板标题
     */
    @Expose
    @SerializedName(value = "title")
    private String title;
    /**
     * 计分板内容
     */
    @Expose
    @SerializedName(value = "layout")
    private List<String> layout;
    /**
     * 计分板的可用世界列表，如果设置为null，则所有世界都可用
     */
    @Expose
    @SerializedName(value = "worlds")
    private List<String> worlds;
    /**
     * id，内置属性
     */
    @Expose(serialize = false, deserialize = false)
    private String id;
    @Expose(serialize = false, deserialize = false)
    private Map<String, String> mapTeamLayout;

    public String getId() {
        return id;
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public List<String> getLayout() {
        return layout;
    }

    public ScoreboardItem() {

    }

    public void display(Player player) {
        // 计分板标题
        String _dummy = SidebarManager.SCOREBOARD_CRITERIA_DUMMY;
        String _title = PlaceholderAPI.setPlaceholders(player, title.replace("&", "§"));
        // 初始化map
        //if (mapTeamLayout == null)
        mapTeamLayout = new HashMap<>();
        //mapTeamLayout.clear();
        // 获取新的scoreboard实例
        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) return;
        // 把计分板赋予玩家
        Scoreboard scoreboard = manager.getNewScoreboard();
        player.setScoreboard(scoreboard);
        // 计分板的计分项目
        Objective objective = scoreboard.registerNewObjective(player.getName(), _dummy, _title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int layoutSize = layout.size();
        for (int index = layoutSize - 1; index >= 0; index--) {
            // 计分板每个Entry的显示标识，为了透明效果，使用颜色代码，如&1、&m等
            String flag = String.format("§%s", index);
            // yaml文档里面的布局行
            String line = layout.get(index);
            // 布局行通过Placeholder解析
            String _layout = PlaceholderAPI.setPlaceholders(player, line.replace("&", "§"));
            // 为每个计分注册一个Team
            int ordinal = layoutSize - index - 1;
            Team team = scoreboard.registerNewTeam(String.valueOf(ordinal));
            // 记录原始布局行
            mapTeamLayout.put(String.valueOf(ordinal), line);
            // 计分项目以Team的前缀显示，非计分的Entry
            team.setPrefix(_layout);
            // Team的Entry值和Score的Entry值必须匹配
            team.addEntry(flag);
            Score score = objective.getScore(flag);
            score.setScore(ordinal);
        }
    }

    public void Update(@NotNull Player player) {
        // 获取每个玩家的计分板
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective(DisplaySlot.SIDEBAR);
        if (objective == null) return;
        // 获取计分板内所有Team
        Set<Team> teams = scoreboard.getTeams();
        for (Team t : teams) {
            // 从map中获取原始布局行
            String line = mapTeamLayout.get(t.getName());
            if (StringUtils.isBlank(line)) continue;
            // Placeholder
            String _layout = PlaceholderAPI.setPlaceholders(player, line.replace("&", "§"));
            // 积分项目标题以Team的前缀显示
            t.setPrefix(_layout);
        }
    }
}
