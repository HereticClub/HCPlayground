package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.menu.SkillMenuPanel;
import org.hcmc.hcplayground.model.player.PlayerData;

import java.util.*;

/**
 * MMO技能定义
 */
public class MMOSkillTemplate {
    /**
     * MMO技能等级类型
     */
    @Expose
    @SerializedName(value = "type")
    private SkillMenuPanel.SkillType type;
    /**
     * 技能的显示名称
     */
    @Expose
    @SerializedName(value = "name")
    private String name;
    /**
     * 技能等级装饰物品模板名称
     */
    @Expose
    @SerializedName(value = "template")
    private String template;
    @Expose(deserialize = false)
    private List<MMOLevelTemplate> levels = new ArrayList<>();
    @Expose(deserialize = false)
    private String id;

    public String getId() {
        return id;
    }

    public List<MMOLevelTemplate> getLevels() {
        return levels;
    }

    public String getName() {
        return name;
    }

    public void setLevels(List<MMOLevelTemplate> levels) {
        this.levels = new ArrayList<>(levels);
    }

    public SkillMenuPanel.SkillType getType() {
        return type;
    }

    public void setType(SkillMenuPanel.SkillType type) {
        this.type = type;
    }

    public MMOSkillTemplate() {

    }

    /**
     * 获取所有已达成的技能等级实例
     * @param player 统计技能等级的玩家实例
     * @return 所有已达成当前技能的等级实例
     */
    public List<MMOLevelTemplate> getReachedLevels(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.getStatisticSkill(type);

        return levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
    }

    public List<MMOLevelTemplate> getUnclaimedLevels(Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.getStatisticSkill(type);
        Map<SkillMenuPanel.SkillType, Integer> claimed = data.getClaimedSkillLevel();
        int claimedLevel = claimed.getOrDefault(type, 0);

        return levels.stream().filter(x -> statistic >= x.getThreshold() && x.getLevel() > claimedLevel).toList();
    }


    public Map<Integer, ItemStack> decorateLevels(int statistic) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();

        List<MMOLevelTemplate> reached = levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
        List<MMOLevelTemplate> unreached = new ArrayList<>(levels.stream().filter(x -> statistic < x.getThreshold()).toList());
        unreached.sort(Comparator.comparing(MMOLevelTemplate::getThreshold));

        for (MMOLevelTemplate level : reached) {
            ItemStack itemStack = level.setupItemStack(template, statistic, 0);
            itemStacks.put(level.getSlot() - 1, itemStack);
        }

        for (int i = 0; i < unreached.size(); i++) {
            MMOLevelTemplate level = unreached.get(i);
            ItemStack itemStack = i == 0 ? level.setupItemStack(template, statistic, 1) : level.setupItemStack(template, statistic, 2);
            itemStacks.put(level.getSlot() - 1, itemStack);
        }

        return itemStacks;
    }
}
