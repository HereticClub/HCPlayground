package org.hcmc.hcplayground.manager;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
//import org.hcmc.hcplayground.enums.MMOType;
import org.hcmc.hcplayground.model.menu.SkillMenuPanel;
import org.hcmc.hcplayground.model.mmo.MMOCollectionMaterial;
import org.hcmc.hcplayground.model.mmo.MMOLevelTemplate;
import org.hcmc.hcplayground.model.mmo.MMOReward;
import org.hcmc.hcplayground.model.mmo.MMOSkillTemplate;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RomanNumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RewardManager {

    private static List<MMOReward> rewards = new ArrayList<>();
    private static List<String> keys = new ArrayList<>();


    public RewardManager() {

    }

    public static List<String> getKeys() {
        return keys;
    }

    public static void Load(YamlConfiguration yaml) {
        rewards = Global.deserializeList(yaml, MMOReward.class);
        keys = yaml.getKeys(false).stream().toList();
    }

    public static MMOReward getReward(String key) {
        return rewards.stream().filter(x -> x.getId().equalsIgnoreCase(key)).findAny().orElse(null);
    }

    public static void claim(Player player, SkillMenuPanel.SkillType type) {
        PlayerData data = PlayerManager.getPlayerData(player);
        MMOSkillTemplate skill = MMOManager.getSkillTemplate(type);
        String skillName = skill == null ? type.name() : skill.getName();
        List<MMOLevelTemplate> unclaimedLevels = skill == null ? new ArrayList<>() : skill.getUnclaimedLevels(player);

        if (unclaimedLevels.size() == 0) {
            player.sendMessage(LanguageManager.getString("rewardAllClaimed").replace("%reward_type%", skillName));
            return;
        }

        for (MMOLevelTemplate level : unclaimedLevels) {
            level.reward(player);
            player.sendMessage(LanguageManager.getString("rewardLevelClaimed")
                    .replace("%reward_type%", skillName)
                    .replace("%level%", RomanNumber.fromInteger(level.getLevel())));
        }

        int maxLevel = unclaimedLevels.stream().mapToInt(MMOLevelTemplate::getLevel).max().orElse(0);
        Map<SkillMenuPanel.SkillType, Integer> claimed = data.getClaimedSkillLevel();
        claimed.put(type, maxLevel);
        data.setClaimedSkillLevel(claimed);
    }

    public static void claim(Player player, Material material) {
        PlayerData data = PlayerManager.getPlayerData(player);
        String collectionName = material.name();

        MMOCollectionMaterial collectionMaterial = MMOManager.getCollectionMaterial(material);
        List<MMOLevelTemplate> unclaimedLevels = collectionMaterial == null ? new ArrayList<>() : collectionMaterial.getUnclaimedLevels(player, material);

        if (unclaimedLevels.size() == 0) {
            player.sendMessage(LanguageManager.getString("rewardAllClaimed").replace("%reward_type%", collectionName));
            return;
        }

        for (MMOLevelTemplate level : unclaimedLevels) {
            level.reward(player);
            player.sendMessage(LanguageManager.getString("rewardLevelClaimed")
                    .replace("%reward_type%", collectionName)
                    .replace("%level%", RomanNumber.fromInteger(level.getLevel())));
        }

        int maxLevel = unclaimedLevels.stream().mapToInt(MMOLevelTemplate::getLevel).max().orElse(0);
        Map<Material, Integer> claimed = data.getClaimedCollectionLevel();
        claimed.put(material, maxLevel);
        data.setClaimedCollectionLevel(claimed);
    }

    public static void reset(SkillMenuPanel.SkillType type, Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        Map<SkillMenuPanel.SkillType, Integer> claimed = data.getClaimedSkillLevel();
        if (claimed.containsKey(type)) claimed.put(type, 0);
        data.setClaimedSkillLevel(claimed);
    }

    public static void reset(Material material, Player player) {
        PlayerData data = PlayerManager.getPlayerData(player);
        Map<Material, Integer> claimed = data.getClaimedCollectionLevel();
        if (claimed.containsKey(material)) claimed.put(material, 0);
        data.setClaimedCollectionLevel(claimed);
    }
}
