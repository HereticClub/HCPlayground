package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.hcmc.hcplayground.manager.RewardManager;
import org.hcmc.hcplayground.utility.RomanNumber;

import java.util.ArrayList;
import java.util.List;

public class MMOSkillLevel {
    /**
     * 技能等级的显示名称
     */
    @Expose
    @SerializedName(value = "display")
    private String display;
    /**
     * 奖励id
     */
    @Expose
    @SerializedName(value = "reward")
    private String reward;
    /**
     * 已达成等级的显示物品，通常是绿色玻璃板
     */
    @Expose
    @SerializedName(value = "reached")
    private Material reachedMaterial = Material.LIME_STAINED_GLASS_PANE;
    /**
     * 正在进行的等级的显示物品，通常是黄色玻璃板
     */
    @Expose
    @SerializedName(value = "inprogress")
    private Material currentMaterial = Material.YELLOW_STAINED_GLASS_PANE;
    /**
     * 未达成当前等级时显示的物品
     */
    @Expose
    @SerializedName(value = "unreached")
    private Material unreachedMaterial = Material.RED_STAINED_GLASS_PANE;
    /**
     * 技能等级所在菜单界面的插槽号，以1为开始，最大值: 54
     */
    @Expose
    @SerializedName(value = "slot")
    private int slot;
    /**
     * 表示显示物品的数量
     */
    @Expose
    @SerializedName(value = "amount")
    private int amount;
    /**
     * 通过当前等级的阈值，这是累进数值
     */
    @Expose
    @SerializedName(value = "threshold")
    private int threshold;
    /**
     * 等级说明
     */
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();

    /**
     * 等级id，包含等级数值
     */
    @Expose(deserialize = false)
    private String id;
    /**
     * 等级数值，由id拆解
     */
    @Expose(deserialize = false)
    private int level = -1;

    public String getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public int getSlot() {
        return slot;
    }

    public int getThreshold() {
        return threshold;
    }

    public String getDisplay() {
        return display;
    }
    public Material getCurrentMaterial() {
        return currentMaterial;
    }
    public Material getReachedMaterial() {
        return reachedMaterial;
    }

    public Material getUnreachedMaterial() {
        return unreachedMaterial;
    }

    public List<String> getLore() {
        return lore;
    }

    public MMOSkillLevel() {

    }

    public void initialize(MMOSkill skill) {
        level = -1;
        MMOReward reward = RewardManager.getReward(this.reward);
        if (reward != null) {
            lore.addAll(reward.getLore());
        }

        if (StringUtils.isBlank(id)) return;
        String[] keys = id.split("\\.");
        if (keys.length <= 1) return;
        if (!StringUtils.isNumeric(keys[1])) return;
        level = Integer.parseInt(keys[1]);

        if (StringUtils.isBlank(display))
            display = String.format("%s %s", skill.getName(), RomanNumber.fromInteger(level));
    }
}
