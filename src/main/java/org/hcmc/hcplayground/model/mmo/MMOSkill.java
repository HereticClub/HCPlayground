package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.MMOType;
import org.hcmc.hcplayground.manager.*;
import org.hcmc.hcplayground.utility.RomanNumber;

import java.util.*;

/**
 * MMO技能定义
 */
public class MMOSkill {
    /**
     * MMO技能等级类型
     */
    @Expose
    @SerializedName(value = "type")
    private MMOType type;
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
    private List<MMOLevel> levels = new ArrayList<>();
    @Expose(deserialize = false)
    private String id;

    public String getId() {
        return id;
    }

    public List<MMOLevel> getLevels() {
        return levels;
    }

    public String getName() {
        return name;
    }

    public void setLevels(List<MMOLevel> levels) {
        this.levels = new ArrayList<>(levels);
    }

    public MMOType getType() {
        return type;
    }

    public void setType(MMOType type) {
        this.type = type;
    }

    public MMOSkill() {

    }

    /**
     * 获取所有已达成的技能等级实例
     * @param statistic 当前技能类型的统计值
     * @return 所有已达成当前技能的等级实例
     */
    public List<MMOLevel> getReachedLevels(int statistic) {
        return levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
    }

    public Map<Integer, ItemStack> decorateLevels(int statistic) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();

        List<MMOLevel> reached = levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
        List<MMOLevel> unreached = new ArrayList<>(levels.stream().filter(x -> statistic < x.getThreshold()).toList());
        unreached.sort(Comparator.comparing(MMOLevel::getThreshold));

        for (MMOLevel level : reached) {
            ItemStack itemStack = level.setupItemStack(template, statistic, 0);
            itemStacks.put(level.getSlot() - 1, itemStack);
        }

        for (int i = 0; i < unreached.size(); i++) {
            MMOLevel level = unreached.get(i);
            ItemStack itemStack = i == 0 ? level.setupItemStack(template, statistic, 1) : level.setupItemStack(template, statistic, 2);
            itemStacks.put(level.getSlot() - 1, itemStack);
        }

        return itemStacks;
    }
}
