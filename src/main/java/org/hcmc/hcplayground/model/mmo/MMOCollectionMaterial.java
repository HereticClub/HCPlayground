package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MMOCollectionMaterial implements Cloneable {

    /**
     * MMO技能等级类型
     */
    @Expose
    @SerializedName(value = "material-types")
    private List<Material> materialTypes = new ArrayList<>();
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

    public void setId(String id) {
        this.id = id;
    }

    public List<Material> getMaterialTypes() {
        return materialTypes;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLevels(List<MMOLevel> levels) {
        this.levels = new ArrayList<>(levels);
    }

    public MMOCollectionMaterial() {

    }

    @Override
    public MMOCollectionMaterial clone() {
        try {
            return (MMOCollectionMaterial) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public Map<Integer, ItemStack> decorate(@NotNull Player player, @NotNull Material material) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();
        int statistic = player.getStatistic(Statistic.PICKUP, material);

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

    /**
     * 获取所有已达成的物品收集等级实例
     *
     * @param statistic 当前物品收集的统计值
     * @return 所有已达成的物品收集等级实例
     */
    @NotNull
    public List<MMOLevel> getReachedLevels(int statistic) {
        return levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
    }
}
