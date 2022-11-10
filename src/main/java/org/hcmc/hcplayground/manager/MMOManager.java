package org.hcmc.hcplayground.manager;

import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.enums.RecipeType;
import org.hcmc.hcplayground.model.menu.RecipeMenuPanel;
import org.hcmc.hcplayground.model.menu.SkillMenuPanel;
import org.hcmc.hcplayground.model.mmo.*;
import org.hcmc.hcplayground.serialization.SkillTypeSerialization;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;

public class MMOManager {
    public static final String TEMPLATE_COLLECTION_MATERIALS = "collection_materials";
    public static final String TEMPLATE_COLLECTION_CATEGORIES = "collection_categories";
    public static final String TEMPLATE_SKILL_TEMPLATES = "skill_templates";
    public static final String TEMPLATE_RECIPE_TEMPLATES = "recipe_templates";
    public static final String TEMPLATE_LEVEL_TEMPLATES = "level_templates";

    //private static List<MMOCollectionCategory> collectionCategories = new ArrayList<>();
    private static List<MMORecipeTemplate> recipeTemplates = new ArrayList<>();
    private static List<MMOLevelTemplate> levelTemplates = new ArrayList<>();
    private static YamlConfiguration yaml;

    /**
     * 配方菜单模板，必须在菜单配置列表中定义
     */
    private static final String MENU_TEMPLATE_RECIPE_ID = "recipe_id_template";
    /**
     * 物品收集菜单模板，必须在菜单配置列表中定义
     */
    private static final String MENU_TEMPLATE_COLLECTION_MATERIAL = "collection_material_template";
    /**
     * 配方成分列表及摆放模板，必须在菜单配置列表中定义
     */
    private static final String MENU_TEMPLATE_RECIPE_MATERIAL = "recipe_material_template";
    /**
     * Farming skill statistic
     */
    public static final EntityType[] Poultry = new EntityType[]{
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.GLOW_SQUID,
            EntityType.PIG,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SQUID,
            EntityType.TURTLE,
            EntityType.FROG,
            EntityType.GOAT,
            EntityType.BEE,
            EntityType.FOX,
            EntityType.CAT,
            EntityType.OCELOT,
            EntityType.WOLF,
            EntityType.PANDA,
            EntityType.POLAR_BEAR,
    };

    /**
     * Farming skill statistic
     */
    public static final Material[] FarmingBlocks = new Material[]{
            Material.WHEAT,
            Material.MELON,
            Material.PUMPKIN,
            Material.BAMBOO,
            Material.SUGAR_CANE,
            Material.POTATOES,
            Material.CARROTS,
            Material.COCOA,
            Material.BEETROOTS,
            Material.NETHER_WART,
            Material.CHORUS_FLOWER,
            Material.CHORUS_PLANT,
            Material.RED_MUSHROOM,
            Material.BROWN_MUSHROOM,
            Material.SWEET_BERRY_BUSH,
    };

    /**
     * Combat skill statistic
     */
    public static final EntityType[] Monsters = new EntityType[]{
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_HORSE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.WITHER,
            EntityType.WITHER_SKELETON,
            EntityType.RAVAGER,
            EntityType.VINDICATOR,
            EntityType.SLIME,
            EntityType.EVOKER,
            EntityType.HUSK,
            EntityType.MAGMA_CUBE,
            EntityType.GIANT,
            EntityType.ILLUSIONER,
            EntityType.PHANTOM,
            EntityType.GHAST,
            EntityType.VEX,
            EntityType.PILLAGER,
            EntityType.ENDERMAN,
            EntityType.ENDERMITE,
            EntityType.CAVE_SPIDER,
            EntityType.STRAY,
            EntityType.DROWNED,
            EntityType.SHULKER,
            EntityType.BLAZE,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.HOGLIN,
            EntityType.CREEPER,
            EntityType.SPIDER,
            EntityType.SILVERFISH,
            EntityType.ELDER_GUARDIAN,
            EntityType.SKELETON,
    };

    /**
     * Mining skill statistic
     */
    public static final Material[] MiningBlocks = new Material[]{
            Material.COAL_ORE,
            Material.COBBLESTONE,
            Material.COBBLED_DEEPSLATE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.END_STONE,
            Material.GLOWSTONE,
            Material.GOLD_ORE,
            Material.GRAVEL,
            Material.ICE,
            Material.IRON_ORE,
            Material.LAPIS_ORE,
            Material.NETHER_GOLD_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHERRACK,
            Material.OBSIDIAN,
            Material.RED_SAND,
            Material.REDSTONE_ORE,
            Material.SAND,
    };

    public static final Material[] Flowers = new Material[]{
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.SUNFLOWER,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
    };

    /**
     * Lumbering skill statistic
     */
    public static final Material[] LumberingBlocks = new Material[]{
            Material.ACACIA_LOG,
            Material.BIRCH_LOG,
            Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG,
            Material.MANGROVE_LOG,
            Material.OAK_LOG,
            Material.SPRUCE_LOG,
            Material.CRIMSON_STEM,
            Material.WARPED_STEM,
    };

    /**
     * Mining collection statistic
     */
    public static final Material[] MiningMaterials = new Material[]{
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

    /**
     * Farming collection statistic
     */
    public static final Material[] FarmingMaterials = new Material[]{
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

    /**
     * Combat collection statistic
     */
    public static final Material[] CombatMaterials = new Material[]{
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

    /**
     * Lumbering collection statistic
     */
    public static final Material[] LumberingMaterials = new Material[]{
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

    /**
     * Fishing collection statistic
     */
    public static final Material[] FishingMaterials = new Material[]{
            Material.CLAY_BALL,
            Material.TROPICAL_FISH,
            Material.INK_SAC,
            Material.LILY_PAD,
            Material.PRISMARINE_CRYSTALS,
            Material.PRISMARINE_SHARD,
            Material.PUFFERFISH,
            Material.COD,
            Material.SALMON,
            Material.SPONGE,
    };

    public MMOManager() {

    }

    public static YamlConfiguration getYaml() {
        return yaml;
    }

    public static void Load(YamlConfiguration yaml) {
        MMOManager.yaml = yaml;

        ConfigurationSection levelSection = yaml.getConfigurationSection(TEMPLATE_LEVEL_TEMPLATES);
        ConfigurationSection recipeSection = yaml.getConfigurationSection(TEMPLATE_RECIPE_TEMPLATES);

        if (recipeSection != null) {
            recipeTemplates = Global.deserializeList(recipeSection, MMORecipeTemplate.class);
        }
        if (levelSection != null) {
            levelTemplates = Global.deserializeList(levelSection, MMOLevelTemplate.class);
        }
    }

    public static MMORecipeTemplate getRecipeTemplate(RecipeType type) {
        return recipeTemplates.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public static MMOLevelTemplate getLevelTemplate(String name) {
        return levelTemplates.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public static MMOSkillTemplate getSkillTemplate(SkillMenuPanel.SkillType type) {
        Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();

        MMOSkillTemplate skillTemplate = null;
        ConfigurationSection skillSection = null;
        ConfigurationSection section = yaml.getConfigurationSection(TEMPLATE_SKILL_TEMPLATES);
        if (section == null) return null;

        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            skillSection = section.getConfigurationSection(key);
            if (skillSection == null) continue;
            String typeValue = skillSection.getString("type");
            SkillMenuPanel.SkillType _type = StringUtils.isBlank(typeValue) ? SkillMenuPanel.SkillType.UNDEFINED : SkillTypeSerialization.valueOf(typeValue.toUpperCase());
            if (!type.equals(_type)) continue;

            skillTemplate = Global.deserialize(skillSection, MMOSkillTemplate.class);
            break;
        }

        if (skillTemplate == null) return null;

        ConfigurationSection levelsSection = skillSection.getConfigurationSection("levels");
        List<MMOLevelTemplate> levels = levelsSection == null ? new ArrayList<>() : Global.deserializeList(levelsSection, MMOLevelTemplate.class);
        skillTemplate.setLevels(levels);
        for (MMOLevelTemplate level : levels) {
            int l = getLevelByLevelId(level.getId());
            String rewardsPath = String.format("levels.%s.rewards", l);
            ConfigurationSection rewardsSection = skillSection.getConfigurationSection(rewardsPath);
            if (rewardsSection != null) {
                String value = Global.GsonObject.toJson(rewardsSection.getValues(false));
                Map<String, List<String>> mapRewards = Global.GsonObject.fromJson(value, mapType);
                level.setRewards(mapRewards);
            }

            level.initialize(l, skillTemplate.getType().name(), skillTemplate.getName());
        }

        return skillTemplate;
    }

    public static MMOCollectionMaterial getCollectionMaterial(Material material) {
        Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();

        MMOCollectionMaterial collectionMaterial = null;
        ConfigurationSection materialLevelsSection = null;
        ConfigurationSection section = yaml.getConfigurationSection(TEMPLATE_COLLECTION_MATERIALS);
        if (section == null) return null;
        Set<String> keys = section.getKeys(false);

        for (String key : keys) {
            materialLevelsSection = section.getConfigurationSection(key);
            if (materialLevelsSection == null) continue;
            List<String> materialNames = materialLevelsSection.getStringList("material-types");
            if (materialNames.stream().noneMatch(x -> x.equalsIgnoreCase(material.name()))) continue;

            collectionMaterial = Global.deserialize(materialLevelsSection, MMOCollectionMaterial.class);
            break;
        }

        if (collectionMaterial == null) return null;

        collectionMaterial.setName(collectionMaterial.getName().replace("%material%", material.name()));
        ConfigurationSection levelsSection = materialLevelsSection.getConfigurationSection("levels");
        if (levelsSection == null) return collectionMaterial;
        List<MMOLevelTemplate> levels = Global.deserializeList(levelsSection, MMOLevelTemplate.class);
        collectionMaterial.setLevels(levels);

        for (MMOLevelTemplate level : levels) {
            int l = getLevelByLevelId(level.getId());
            String rewardsPath = String.format("levels.%s.rewards", l);
            ConfigurationSection rewardsSection = materialLevelsSection.getConfigurationSection(rewardsPath);
            if (rewardsSection != null) {
                String value = Global.GsonObject.toJson(rewardsSection.getValues(false));
                Map<String, List<String>> mapRewards = Global.GsonObject.fromJson(value, mapType);
                level.setRewards(mapRewards);
            }

            level.initialize(l, material.name(), collectionMaterial.getName());
        }

        return collectionMaterial;
    }

    public static List<String> getSkillRewardNames() {
        List<String> names = new ArrayList<>();
        ConfigurationSection section = yaml.getConfigurationSection(TEMPLATE_SKILL_TEMPLATES);
        if (section == null) return new ArrayList<>();
        Set<String> keys = section.getKeys(false);
        for (String key : keys) {
            String typePath = String.format("%s.type", key);
            String typeValue = section.getString(typePath);
            if (StringUtils.isBlank(typeValue)) continue;
            names.add(typeValue.toLowerCase());
        }

        return names;
    }

    @NotNull
    public static List<String> getCollectionRewardNames() {
        List<String> names = new ArrayList<>();
        ConfigurationSection materialSection = yaml.getConfigurationSection(TEMPLATE_COLLECTION_MATERIALS);
        if (materialSection == null) return new ArrayList<>();
        ConfigurationSection categorySection = yaml.getConfigurationSection(TEMPLATE_COLLECTION_CATEGORIES);
        if (categorySection == null) return new ArrayList<>();

        Set<String> materialKeys = materialSection.getKeys(false);
        for (String key : materialKeys) {
            String materialPath = String.format("%s.material-types", key);
            List<String> materialNames = materialSection.getStringList(materialPath);
            names.addAll(materialNames.stream().map(String::toLowerCase).toList());
        }

        Set<String> categoriesKeys = categorySection.getKeys(false);
        for (String key : categoriesKeys) {
            String categoryPath = String.format("%s.type", key);
            String categoryType = categorySection.getString(categoryPath);
            if (!StringUtils.isBlank(categoryType)) names.add(categoryType.toLowerCase());
        }

        return names;
    }

    public static void incrementStatisticPoints(Player player, SkillMenuPanel.SkillType type, int points) {
        switch (type) {
            case SKILL_COMBAT -> increaseStatisticPoints(player, Monsters, points);
            case SKILL_FARMING -> increaseFarmingPoints(player, points);
            case SKILL_MINING -> increaseStatisticPoints(player, MiningBlocks, points);
            case SKILL_LUMBERING -> increaseStatisticPoints(player, LumberingMaterials, points);
            case SKILL_FISHING -> player.incrementStatistic(Statistic.FISH_CAUGHT, points);
            default -> {
            }
        }
    }

    public static void decreaseStatisticPoints(Player player, SkillMenuPanel.SkillType type, int points) {
        int reminder = switch (type) {
            case SKILL_COMBAT -> decreaseStatisticPoints(player, Monsters, points);
            case SKILL_FARMING -> decreaseFarmingPoints(player, points);
            case SKILL_MINING -> decreaseStatisticPoints(player, MiningBlocks, points);
            case SKILL_LUMBERING -> decreaseStatisticPoints(player, LumberingBlocks, points);
            case SKILL_FISHING -> decreaseFishCaughtPoints(player, points);
            default -> 0;
        };

        String className = MMOManager.class.getSimpleName();
        String methodName = "decreaseStatisticPoints";
        String message = String.format("Remind %s points: %d", type, reminder);
        Global.LogDebug(new Date(), className, methodName, Level.INFO, message);
    }

    public static Item dropProbability(World world, Location location, ItemStack itemStack, float rate) {
        if (!RandomNumber.checkBingo(rate)) return null;
        return world.dropItemNaturally(location, itemStack);
    }

    private static int decreaseFarmingPoints(Player player, int points) {
        int reminder = decreaseStatisticPoints(player, Poultry, points);
        return decreaseStatisticPoints(player, FarmingBlocks, reminder);
    }

    private static int decreaseFishCaughtPoints(Player player, int points) {
        int exist = player.getStatistic(Statistic.FISH_CAUGHT);
        int used = Math.min(exist, points);
        player.decrementStatistic(Statistic.FISH_CAUGHT, used);
        return points - used;
    }

    private static int decreaseStatisticPoints(Player player, EntityType[] types, int points) {
        int used = 0;
        int reminder = 0;

        while (used < points) {
            int zeroCount = 0;
            for (EntityType m : types) {
                int amount = player.getStatistic(Statistic.KILL_ENTITY, m);
                if (amount <= 0) {
                    zeroCount++;
                    continue;
                }
                player.decrementStatistic(Statistic.KILL_ENTITY, m, 1);
                used++;
                if (used >= points) break;
            }

            if (zeroCount >= types.length) {
                reminder = points - used;
                break;
            }
        }
        return reminder;
    }

    private static int decreaseStatisticPoints(Player player, Material[] materials, int points) {
        int used = 0;
        int reminder = 0;

        while (used < points) {
            int zero = 0;
            for (Material m : materials) {
                int amount = player.getStatistic(Statistic.MINE_BLOCK, m);
                if (amount <= 0) {
                    zero++;
                    continue;
                }
                player.decrementStatistic(Statistic.MINE_BLOCK, m, 1);

                used++;
                if (used >= points) break;
            }

            if (zero >= materials.length) {
                reminder = points - used;
                break;
            }
        }
        return reminder;
    }

    private static void increaseFarmingPoints(Player player, int points) {
        int poultryPoint = points / 2;
        int farmingPoint = points - poultryPoint;
        increaseStatisticPoints(player, Poultry, poultryPoint);
        increaseStatisticPoints(player, FarmingBlocks, farmingPoint);
    }

    private static void increaseStatisticPoints(Player player, EntityType[] types, int points) {
        int typeIndex = 0;
        for (int i = 0; i < points; i++) {
            if (typeIndex >= types.length) typeIndex = 0;

            player.incrementStatistic(Statistic.KILL_ENTITY, types[typeIndex], 1);
            typeIndex++;
        }
    }

    private static void increaseStatisticPoints(Player player, Material[] materials, int points) {
        int materialIndex = 0;
        for (int i = 0; i < points; i++) {
            if (materialIndex >= materials.length) materialIndex = 0;

            player.incrementStatistic(Statistic.MINE_BLOCK, materials[materialIndex], 1);
            materialIndex++;
        }
    }

    private static int getLevelByLevelId(String id) {
        int level = -1;
        if (StringUtils.isBlank(id)) return level;
        String[] keys = id.split("\\.");
        if (keys.length >= 2 && StringUtils.isNumeric(keys[1]))
            level = Integer.parseInt(keys[1]);

        return level;
    }
}
