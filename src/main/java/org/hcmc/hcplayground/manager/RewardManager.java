package org.hcmc.hcplayground.manager;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.enums.MMOType;
import org.hcmc.hcplayground.model.mmo.MMOCollectionMaterial;
import org.hcmc.hcplayground.model.mmo.MMOReward;
import org.hcmc.hcplayground.model.mmo.MMOSkill;
import org.hcmc.hcplayground.model.mmo.MMOLevel;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RomanNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardManager {

    private static List<MMOReward> rewards = new ArrayList<>();
    private static List<String> idList = new ArrayList<>();

    public static List<String> getIdList() {
        return idList;
    }

    public static List<MMOReward> getRewards() {
        return rewards;
    }

    public RewardManager() {

    }

    public static void Load(YamlConfiguration yaml) {
        rewards = Global.deserializeList(yaml, MMOReward.class);
        idList = yaml.getKeys(false).stream().toList();
    }

    public static MMOReward getReward(String id) {
        return rewards.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    public static void claim(MMOType type, Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        MMOSkill skill = MMOManager.getSkill(type);
        String skillName = skill == null ? type.name() : skill.getName();
        List<MMOLevel> levels = getUnclaimedLevels(player, type);

        if (levels.size() <= 0) {
            player.sendMessage(LanguageManager.getString("rewardAllClaimed").replace("%reward_type%", skillName));
            return;
        }

        for (MMOLevel level : levels) {
            level.reward(player);
            player.sendMessage(LanguageManager.getString("rewardLevelClaimed")
                    .replace("%reward_type%", skillName)
                    .replace("%level%", RomanNumber.fromInteger(level.getLevel())));
        }

        int maxLevel = levels.stream().mapToInt(MMOLevel::getLevel).max().orElse(0);
        Map<MMOType, Integer> claimed = data.getClaimedSkillLevel();
        claimed.put(type, maxLevel);
        data.setClaimedSkillLevel(claimed);
    }

    public static void claim(Material material, Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        List<MMOLevel> levels = getUnclaimedLevels(player, material);
        MMOCollectionMaterial collection = MMOManager.getCollectionMaterial(material);
        String collectionName = collection == null ? material.name() : collection.getName();

        if (levels.size() <= 0) {
            player.sendMessage(LanguageManager.getString("rewardAllClaimed").replace("%reward_type%", collectionName));
            return;
        }

        for (MMOLevel level : levels) {
            level.reward(player);
            player.sendMessage(LanguageManager.getString("rewardLevelClaimed")
                    .replace("%reward_type%", collectionName)
                    .replace("%level%", RomanNumber.fromInteger(level.getLevel())));
        }

        int maxLevel = levels.stream().mapToInt(MMOLevel::getLevel).max().orElse(0);
        Map<Material, Integer> claimed = data.getClaimedCollectionLevel();
        claimed.put(material, maxLevel);
        data.setClaimedCollectionLevel(claimed);
    }

    public static void reset(MMOType type, Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        Map<MMOType, Integer> claimed = data.getClaimedSkillLevel();
        claimed.put(type, 0);
        data.setClaimedSkillLevel(claimed);
    }

    public static List<MMOLevel> getUnclaimedLevels(Player player, MMOType type) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = data.getStatisticSkill(type);
        Map<MMOType, Integer> claimed = data.getClaimedSkillLevel();
        int claimedLevel = claimed.getOrDefault(type, 0);

        MMOSkill skill = MMOManager.getSkill(type);
        if (skill == null) return new ArrayList<>();
        return skill.getReachedLevels(statistic).stream().filter(x -> x.getLevel() > claimedLevel).toList();
    }

    public static List<MMOLevel> getUnclaimedLevels(Player player, Material material) {
        PlayerData data = PlayerManager.getPlayerData(player);
        int statistic = player.getStatistic(Statistic.PICKUP, material);
        Map<Material, Integer> claimed = data.getClaimedCollectionLevel();
        int claimedLevel = claimed.getOrDefault(material, 0);

        MMOCollectionMaterial collectionMaterial = MMOManager.getCollectionMaterial(material);
        if (collectionMaterial == null) return new ArrayList<>();
        return collectionMaterial.getReachedLevels(statistic).stream().filter(x -> x.getLevel() > claimedLevel).toList();
    }
}
