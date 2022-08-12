package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.hcmc.hcplayground.enums.MMOSkillType;

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
    private MMOSkillType type;
    /**
     * 技能的显示名称
     */
    @Expose
    @SerializedName(value = "name")
    private String name;

    @Expose(serialize = false, deserialize = false)
    private List<MMOSkillLevel> levels = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private String id;

    public String getId() {
        return id;
    }

    public List<MMOSkillLevel> getLevels() {
        return levels;
    }

    public String getName() {
        return name;
    }

    public void setLevels(List<MMOSkillLevel> levels) {
        this.levels = levels;
        for (MMOSkillLevel level : this.levels) {
            level.initialize(this);
        }
    }

    public MMOSkillType getType() {
        return type;
    }

    public void setType(MMOSkillType type) {
        this.type = type;
    }

    public MMOSkill() {

    }

    public Map<Integer, ItemStack> resolveLevels(int statistic) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();

        List<MMOSkillLevel> reached = levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
        List<MMOSkillLevel> unreached = new ArrayList<>(levels.stream().filter(x -> statistic < x.getThreshold()).toList());
        unreached.sort(Comparator.comparing(MMOSkillLevel::getThreshold));

        for (MMOSkillLevel level : reached) {
            ItemStack itemStack = setupItemStack(level, statistic, 0);
            itemStacks.put(level.getSlot() - 1, itemStack);
        }
        for (int i = 0; i < unreached.size(); i++) {
            MMOSkillLevel level = unreached.get(i);
            ItemStack itemStack;
            if (i == 0) {
                itemStack = setupItemStack(level, statistic, 1);
            } else {
                itemStack = setupItemStack(level, statistic, 2);
            }
            itemStacks.put(level.getSlot() - 1, itemStack);
        }

        return itemStacks;
    }

    /**
     * flag<br>
     * 0: reached<br>
     * 1: in-progress<br>
     * 2: unreached<br>
     */
    private ItemStack setupItemStack(MMOSkillLevel level, int currentPoints, int flag) {
        ItemStack itemStack = switch (flag) {
            case 0 -> new ItemStack(level.getReachedMaterial(), level.getAmount());
            case 1 -> new ItemStack(level.getCurrentMaterial(), level.getAmount());
            case 2 -> new ItemStack(level.getUnreachedMaterial(), level.getAmount());
            default -> new ItemStack(Material.AIR, level.getAmount());
        };

        StringBuilder pass = new StringBuilder("=");
        StringBuilder left = new StringBuilder("-");
        int progressBarCount = 20;
        float percent = (float) currentPoints / level.getThreshold() * 100;
        int progress = (int) percent / 5;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) return itemStack;
        meta.setDisplayName(level.getDisplay());
        List<String> lore = new ArrayList<>(level.getLore());
        lore.replaceAll(x -> x.replace("%skill_points%", String.valueOf(currentPoints))
                .replace("%threshold%", String.valueOf(level.getThreshold()))
                .replace("%percent%", String.format("%.2f%%", percent)));
        if (flag == 1) {
            pass.append("=".repeat(Math.max(0, progress)));
            left.append("-".repeat(progressBarCount - progress - 1));
            String line = String.format("§7 §a§m%s§e%s", pass, left);
            lore.add(line);
        }
        meta.setLore(lore);
        itemStack.setItemMeta(meta);

        return itemStack;
    }
}
