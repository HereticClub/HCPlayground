package org.hcmc.hcplayground.manager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.hcmc.hcplayground.model.mmo.MMOReward;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.List;

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

        for (MMOReward reward : rewards) {
            reward.initialize();
        }
    }

    public static MMOReward getReward(String id) {
        return rewards.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }
}
