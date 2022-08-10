package org.hcmc.hcplayground.manager;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.enums.MMOSkillType;
import org.hcmc.hcplayground.model.level.MMOSkill;
import org.hcmc.hcplayground.model.level.MMOSkillLevel;
import org.hcmc.hcplayground.utility.Global;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MMOSkillManager {
    public static final Material[] miningStatistics = new Material[]{
            Material.COAL,
            Material.COBBLESTONE,
            Material.DIAMOND,
            Material.EMERALD,
            Material.END_STONE,
            Material.GLOWSTONE_DUST,
            Material.GOLD_INGOT,
            Material.IRON_INGOT,
            Material.GRAVEL,
            Material.ICE,
            Material.LAPIS_LAZULI,
            Material.QUARTZ,
            Material.NETHERRACK,
            Material.OBSIDIAN,
            Material.RED_SAND,
            Material.SAND,
            Material.REDSTONE,
    };
    public static final Material[] farmingStatistics = new Material[]{
            Material.FEATHER,
            Material.LEATHER,
            Material.MUTTON,
            Material.RABBIT_HIDE,
            Material.RABBIT_FOOT,
            Material.RABBIT,
            Material.BEEF,
            Material.CHICKEN,
            Material.PORKCHOP,
            Material.BROWN_MUSHROOM,
            Material.RED_MUSHROOM,
            Material.COCOA_BEANS,
            Material.POTATO,
            Material.CARROT,
            Material.CACTUS,
            Material.SUGAR_CANE,
            Material.MELON,
            Material.NETHER_WART,
            Material.PUMPKIN,
            Material.WHEAT,
            Material.WHEAT_SEEDS,
    };
    public static final Material[] combatStatistics = new Material[]{
            Material.BLAZE_ROD,
            Material.BONE,
            Material.ENDER_PEARL,
            Material.GHAST_TEAR,
            Material.GUNPOWDER,
            Material.MAGMA_CREAM,
            Material.ROTTEN_FLESH,
            Material.SLIME_BALL,
            Material.SPIDER_EYE,
            Material.STRING,
    };
    public static final Material[] lumberingStatistics = new Material[]{
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.JUNGLE_LOG,
            Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.ACACIA_SAPLING,
            Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.SPRUCE_SAPLING,
            Material.OAK_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.MANGROVE_PROPAGULE,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
            Material.APPLE,
    };
    public static final Material[] fishingStatistics = new Material[]{
            Material.TROPICAL_FISH,
            Material.INK_SAC,
            Material.LILY_PAD,
            Material.PRISMARINE_CRYSTALS,
            Material.PRISMARINE_SHARD,
            Material.PUFFERFISH,
            Material.COD,
            Material.SALMON,
            Material.SPONGE,
            Material.CLAY,
    };

    private static List<MMOSkill> skills = new ArrayList<>();

    public static List<MMOSkill> getSkills() {
        return skills;
    }

    public static List<String> idList = new ArrayList<>();

    public MMOSkillManager() {

    }

    public static void Load(YamlConfiguration yaml) {
        skills = Global.SetItemList(yaml, MMOSkill.class);
        idList.clear();

        for (MMOSkill skill : skills) {
            idList.add(skill.getId());

            String path = String.format("%s.levels", skill.getId());
            ConfigurationSection section = yaml.getConfigurationSection(path);
            if (section == null) continue;
            skill.setLevels(Global.SetItemList(section, MMOSkillLevel.class));
        }
    }

    public static MMOSkill getSkill(MMOSkillType type) {
        return skills.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public static List<String> getIdList() {
        return idList;
    }

    public static void incrementStatisticPoint(Player player, MMOSkillType type, int points) {
        int materialIndex = 0;
        List<Material> materials;
        switch (type) {
            case SKILL_COMBAT -> materials = Arrays.asList(combatStatistics);
            case SKILL_FARMING -> materials = Arrays.asList(farmingStatistics);
            case SKILL_MINING -> materials = Arrays.asList(miningStatistics);
            case SKILL_LUMBERING -> materials = Arrays.asList(lumberingStatistics);
            case SKILL_FISHING -> materials = Arrays.asList(fishingStatistics);
            default -> materials = new ArrayList<>();
        }

        for (int i = 0; i < points; i++) {
            if (materialIndex >= materials.size()) materialIndex = 0;
            player.incrementStatistic(Statistic.PICKUP, materials.get(materialIndex), 1);
            materialIndex++;
        }
    }

    public static void decrementStatisticPoint(Player player, MMOSkillType type, int points) {
        int materialIndex = 0;
        List<Material> materials;
        switch (type) {
            case SKILL_COMBAT -> materials = Arrays.asList(combatStatistics);
            case SKILL_FARMING -> materials = Arrays.asList(farmingStatistics);
            case SKILL_MINING -> materials = Arrays.asList(miningStatistics);
            case SKILL_LUMBERING -> materials = Arrays.asList(lumberingStatistics);
            case SKILL_FISHING -> materials = Arrays.asList(fishingStatistics);
            default -> materials = new ArrayList<>();
        }

        for (int i = 0; i < points; i++) {
            if (materialIndex >= materials.size()) materialIndex = 0;
            player.decrementStatistic(Statistic.PICKUP, materials.get(materialIndex), 1);
            materialIndex++;
        }
    }
}
