package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 采集材料类，采集材料的等级奖励
 */
public class MMOCollectionMaterial {

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
    private List<MMOLevelTemplate> levels = new ArrayList<>();
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

    public void setMaterialTypes(List<Material> materialTypes) {
        this.materialTypes = new ArrayList<>(materialTypes);
    }

    public List<MMOLevelTemplate> getLevels() {
        return new ArrayList<>(levels);
    }

    public void setLevels(List<MMOLevelTemplate> levels) {
        this.levels = new ArrayList<>(levels);
    }

    public MMOCollectionMaterial() {

    }

    @NotNull
    public Map<Integer, ItemStack> decorate(@NotNull Player player, @NotNull Material material) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();
        int statistic = player.getStatistic(Statistic.PICKUP, material);

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

    /**
     * 获取所有已达成的物品收集等级实例
     *
     * @param player 统计物品收集等级的玩家实例
     * @param material 需要统计的物品
     * @return 所有已达成的物品收集等级实例
     */

    @NotNull
    public List<MMOLevelTemplate> getReachedLevels(Player player, Material material) {
        int statistic = player.getStatistic(Statistic.PICKUP, material);
        return levels.stream().filter(x -> statistic >= x.getThreshold()).toList();
    }

    public List<MMOLevelTemplate> getUnclaimedLevels(Player player, Material material) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = player.getStatistic(Statistic.PICKUP, material);
        Map<Material, Integer> claimed = data.getClaimedCollectionLevel();
        int claimedLevel = claimed.getOrDefault(material, 0);

        return levels.stream().filter(x -> statistic >= x.getThreshold() && x.getLevel() > claimedLevel).toList();
    }
}
