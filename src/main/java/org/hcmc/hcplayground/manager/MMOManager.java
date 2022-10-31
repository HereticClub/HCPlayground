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
import org.hcmc.hcplayground.enums.MMOType;
import org.hcmc.hcplayground.model.mmo.*;
import org.hcmc.hcplayground.utility.Global;
import org.hcmc.hcplayground.utility.RandomNumber;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;
import java.util.logging.Level;

public class MMOManager {
    private static final String MENU_SKILL_ID_MINING = "skill_mining";
    private static final String MENU_SKILL_ID_FARMING = "skill_farming";
    private static final String MENU_SKILL_ID_COMBAT = "skill_combat";
    private static final String MENU_SKILL_ID_LUMBERING = "skill_lumbering";
    private static final String MENU_SKILL_ID_FISHING = "skill_fishing";
    private static final String MENU_COLLECTION_ID_MINING = "collection_mining";
    private static final String MENU_COLLECTION_ID_FARMING = "collection_farming";
    private static final String MENU_COLLECTION_ID_COMBAT = "collection_combat";
    private static final String MENU_COLLECTION_ID_LUMBERING = "collection_lumbering";
    private static final String MENU_COLLECTION_ID_FISHING = "collection_fishing";
    private static final String MENU_RECIPE_ID_MINING = "recipe_mining";
    private static final String MENU_RECIPE_ID_FARMING = "recipe_farming";
    private static final String MENU_RECIPE_ID_COMBAT = "recipe_combat";
    private static final String MENU_RECIPE_ID_LUMBERING = "recipe_lumbering";
    private static final String MENU_RECIPE_ID_FISHING = "recipe_fishing";
    private static final String MENU_RECIPE_ID_MINION = "recipe_minion";
    /**
     * 技能菜单模板名称，必须在菜单配置列表中定义
     */
    private static final String MENU_TEMPLATE_SKILL_ID = "skill_id_template";
    /**
     * 收集菜单模板，必须在菜单配置列表中定义
     */
    private static final String MENU_TEMPLATE_COLLECTION_ID = "collection_id_template";
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
    private static List<String> skillMenuKeys = new ArrayList<>();
    private static List<String> collectionMenuKeys = new ArrayList<>();
    private static List<String> recipeMenuKeys = new ArrayList<>();
    /**
     * String - Material name for collection<br>
     * String - Menu template id
     */
    private static final Map<String, String> materialMenuMapping = new HashMap<>();
    /**
     * String - Skill id or Collection id<br>
     * String - Menu template id
     */
    private static final Map<String, String> baseMenuMapping = new HashMap<>();
    private static final Map<MMOType, Material[]> collectionMapping = new HashMap<>();
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

    static {
        baseMenuMapping.put(MENU_SKILL_ID_COMBAT, MENU_TEMPLATE_SKILL_ID);
        baseMenuMapping.put(MENU_SKILL_ID_FARMING, MENU_TEMPLATE_SKILL_ID);
        baseMenuMapping.put(MENU_SKILL_ID_FISHING, MENU_TEMPLATE_SKILL_ID);
        baseMenuMapping.put(MENU_SKILL_ID_LUMBERING, MENU_TEMPLATE_SKILL_ID);
        baseMenuMapping.put(MENU_SKILL_ID_MINING, MENU_TEMPLATE_SKILL_ID);
        baseMenuMapping.put(MENU_COLLECTION_ID_COMBAT, MENU_TEMPLATE_COLLECTION_ID);
        baseMenuMapping.put(MENU_COLLECTION_ID_FARMING, MENU_TEMPLATE_COLLECTION_ID);
        baseMenuMapping.put(MENU_COLLECTION_ID_FISHING, MENU_TEMPLATE_COLLECTION_ID);
        baseMenuMapping.put(MENU_COLLECTION_ID_LUMBERING, MENU_TEMPLATE_COLLECTION_ID);
        baseMenuMapping.put(MENU_COLLECTION_ID_MINING, MENU_TEMPLATE_COLLECTION_ID);
        baseMenuMapping.put(MENU_RECIPE_ID_COMBAT, MENU_TEMPLATE_RECIPE_ID);
        baseMenuMapping.put(MENU_RECIPE_ID_FARMING, MENU_TEMPLATE_RECIPE_ID);
        baseMenuMapping.put(MENU_RECIPE_ID_FISHING, MENU_TEMPLATE_RECIPE_ID);
        baseMenuMapping.put(MENU_RECIPE_ID_LUMBERING, MENU_TEMPLATE_RECIPE_ID);
        baseMenuMapping.put(MENU_RECIPE_ID_MINING, MENU_TEMPLATE_RECIPE_ID);
        baseMenuMapping.put(MENU_RECIPE_ID_MINION, MENU_TEMPLATE_RECIPE_ID);

        collectionMapping.put(MMOType.COLLECTION_COMBAT, CombatMaterials);
        collectionMapping.put(MMOType.COLLECTION_FARMING, FarmingMaterials);
        collectionMapping.put(MMOType.COLLECTION_FISHING, FishingMaterials);
        collectionMapping.put(MMOType.COLLECTION_LUMBERING, LumberingMaterials);
        collectionMapping.put(MMOType.COLLECTION_MINING, MiningMaterials);

        skillMenuKeys.add(MENU_SKILL_ID_FARMING);
        skillMenuKeys.add(MENU_SKILL_ID_FISHING);
        skillMenuKeys.add(MENU_SKILL_ID_COMBAT);
        skillMenuKeys.add(MENU_SKILL_ID_MINING);
        skillMenuKeys.add(MENU_SKILL_ID_LUMBERING);
        skillMenuKeys.add(MENU_TEMPLATE_SKILL_ID);
        collectionMenuKeys.add(MENU_COLLECTION_ID_LUMBERING);
        collectionMenuKeys.add(MENU_COLLECTION_ID_COMBAT);
        collectionMenuKeys.add(MENU_COLLECTION_ID_FARMING);
        collectionMenuKeys.add(MENU_COLLECTION_ID_FISHING);
        collectionMenuKeys.add(MENU_COLLECTION_ID_MINING);
        collectionMenuKeys.add(MENU_TEMPLATE_COLLECTION_ID);
        collectionMenuKeys.add(MENU_TEMPLATE_COLLECTION_MATERIAL);
        recipeMenuKeys.add(MENU_RECIPE_ID_COMBAT);
        recipeMenuKeys.add(MENU_RECIPE_ID_FARMING);
        recipeMenuKeys.add(MENU_RECIPE_ID_MINING);
        recipeMenuKeys.add(MENU_RECIPE_ID_FISHING);
        recipeMenuKeys.add(MENU_RECIPE_ID_LUMBERING);
        recipeMenuKeys.add(MENU_RECIPE_ID_MINION);
        recipeMenuKeys.add(MENU_TEMPLATE_RECIPE_ID);
        recipeMenuKeys.add(MENU_TEMPLATE_RECIPE_MATERIAL);
    }

    private static List<MMOSkillTemplate> skillTemplates = new ArrayList<>();
    private static List<MMORecipeTemplate> recipeTemplates = new ArrayList<>();
    private static List<MMOCollectionCategory> collectionCategories = new ArrayList<>();
    private static List<MMOCollectionMaterial> collectionMaterials = new ArrayList<>();
    private static List<MMOLevelTemplate> levelTemplates = new ArrayList<>();
    private static YamlConfiguration yaml;
    private static List<String> skillIdList = new ArrayList<>();
    private static final List<String> collectionIdList = new ArrayList<>();

    public MMOManager() {

    }

    public static boolean isSkillMenuId(String menuId) {
        return skillMenuKeys.contains(menuId);
    }

    public static boolean isCollectionMenuId(String menuId) {
        return collectionMenuKeys.contains(menuId);
    }

    public static boolean isRecipeMenuId(String menuId) {
        return recipeMenuKeys.contains(menuId);
    }

    public static boolean isLegacyMenuId(String menuId) {
        return !skillMenuKeys.contains(menuId) && !collectionMenuKeys.contains(menuId) && !recipeMenuKeys.contains(menuId);
    }

    public static Map<String, String> getMaterialMenuMapping() {
        return materialMenuMapping;
    }

    public static void Load(YamlConfiguration yaml) {
        Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        MMOManager.yaml = yaml;
        ConfigurationSection skillSection = yaml.getConfigurationSection("skill_declarations");
        ConfigurationSection collectionCategorySection = yaml.getConfigurationSection("collection_categories");
        ConfigurationSection collectionMaterialSection = yaml.getConfigurationSection("collection_materials");
        ConfigurationSection levelSection = yaml.getConfigurationSection("level_templates");
        ConfigurationSection recipeSection = yaml.getConfigurationSection("recipe_template");

        if (recipeSection != null) {
            recipeTemplates = Global.deserializeList(recipeSection, MMORecipeTemplate.class);
        }

        if (skillSection != null) {
            skillTemplates = Global.deserializeList(skillSection, MMOSkillTemplate.class);
            skillIdList = skillSection.getKeys(false).stream().toList();
            for (MMOSkillTemplate template : skillTemplates) {
                String id = template.getId().split("\\.")[1];
                String levelsPath = String.format("%s.levels", id);
                ConfigurationSection levelsSection = skillSection.getConfigurationSection(levelsPath);

                if (levelsSection == null) continue;
                List<MMOLevelTemplate> levels = Global.deserializeList(levelsSection, MMOLevelTemplate.class);
                for (MMOLevelTemplate level : levels) {
                    int l = getLevelById(level.getId());
                    String rewardsPath = String.format("%s.levels.%s.rewards", id, l);
                    ConfigurationSection rewardsSection = skillSection.getConfigurationSection(rewardsPath);
                    if (rewardsSection != null) {
                        String value = Global.GsonObject.toJson(rewardsSection.getValues(false));
                        Map<String, List<String>> mapRewards = Global.GsonObject.fromJson(value, mapType);
                        level.setRewards(mapRewards);
                    }

                    level.initialize(l, template.getType().name(), template.getName());
                }
                template.setLevels(levels);
            }
        }
        if (collectionMaterialSection != null) {
            collectionMaterials = Global.deserializeList(collectionMaterialSection, MMOCollectionMaterial.class);
            collectionIdList.clear();
            for (MMOCollectionMaterial collection : collectionMaterials) {
                for (Material material : collection.getMaterialTypes()) {
                    collectionIdList.add(material.name().toLowerCase());
                }
            }
        }
        if (collectionCategorySection != null) {
            collectionCategories = Global.deserializeList(collectionCategorySection, MMOCollectionCategory.class);
        }
        if (levelSection != null) {
            levelTemplates = Global.deserializeList(levelSection, MMOLevelTemplate.class);
        }

        materialMenuMapping.putAll(baseMenuMapping);
        for (Material material : FarmingMaterials) {
            String name = String.format("collection_%s", material.name().toLowerCase());
            if (!materialMenuMapping.containsKey(name))
                materialMenuMapping.put(name, MENU_TEMPLATE_COLLECTION_MATERIAL);
        }
        for (Material material : MiningMaterials) {
            String name = String.format("collection_%s", material.name().toLowerCase());
            if (!materialMenuMapping.containsKey(name))
                materialMenuMapping.put(name, MENU_TEMPLATE_COLLECTION_MATERIAL);
        }
        for (Material material : CombatMaterials) {
            String name = String.format("collection_%s", material.name().toLowerCase());
            if (!materialMenuMapping.containsKey(name))
                materialMenuMapping.put(name, MENU_TEMPLATE_COLLECTION_MATERIAL);
        }
        for (Material material : LumberingMaterials) {
            String name = String.format("collection_%s", material.name().toLowerCase());
            if (!materialMenuMapping.containsKey(name))
                materialMenuMapping.put(name, MENU_TEMPLATE_COLLECTION_MATERIAL);
        }
        for (Material material : FishingMaterials) {
            String name = String.format("collection_%s", material.name().toLowerCase());
            if (!materialMenuMapping.containsKey(name))
                materialMenuMapping.put(name, MENU_TEMPLATE_COLLECTION_MATERIAL);
        }
    }

    @NotNull
    public static MMOType getCollectionType(@NotNull Material material) {
        for (Map.Entry<MMOType, Material[]> entry : collectionMapping.entrySet()) {
            Material[] materials = entry.getValue();
            if (Arrays.asList(materials).contains(material)) return entry.getKey();
        }

        return MMOType.UNDEFINED;
    }

    public static MMORecipeTemplate getRecipeTemplate(MMOType type) {
        return recipeTemplates.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public static MMOLevelTemplate getLevelTemplate(String name) {
        return levelTemplates.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public static MMOSkillTemplate getSkillTemplate(MMOType type) {
        return skillTemplates.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public static MMOCollectionCategory getCollectionCategory(MMOType type) {
        return collectionCategories.stream().filter(x -> x.getType().equals(type)).findAny().orElse(null);
    }

    public static MMOCollectionMaterial getCollectionMaterial(Material material) {
        Type mapType = new TypeToken<Map<String, List<String>>>() {
        }.getType();
        MMOCollectionMaterial cloneObject = collectionMaterials.stream().filter(x -> x.getMaterialTypes().contains(material)).findAny().orElse(null);
        if (cloneObject == null) return null;

        MMOCollectionMaterial collectionMaterial = cloneObject.clone();
        String id = collectionMaterial.getId().split("\\.")[1];
        String levelsPath = String.format("collection_materials.%s.levels", id);
        collectionMaterial.setName(collectionMaterial.getName().replace("%material%", material.name()));

        ConfigurationSection levelsSection = yaml.getConfigurationSection(levelsPath);
        if (levelsSection == null) return collectionMaterial;

        List<MMOLevelTemplate> levels = Global.deserializeList(levelsSection, MMOLevelTemplate.class);
        collectionMaterial.setLevels(levels);

        for (MMOLevelTemplate level : levels) {
            int l = getLevelById(level.getId());
            String rewardsPath = String.format("collection_materials.%s.levels.%s.rewards", id, l);
            ConfigurationSection rewardsSection = yaml.getConfigurationSection(rewardsPath);
            if (rewardsSection != null) {
                String value = Global.GsonObject.toJson(rewardsSection.getValues(false));
                Map<String, List<String>> mapRewards = Global.GsonObject.fromJson(value, mapType);
                level.setRewards(mapRewards);
            }

            level.initialize(l, material.name(), collectionMaterial.getName());
        }
        return collectionMaterial;
    }

    public static List<String> getSkillIdList() {
        return skillIdList;
    }

    public static List<String> getCollectionIdList() {
        return collectionIdList;
    }

    public static void incrementStatisticPoints(Player player, MMOType type, int points) {
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

    public static void decreaseStatisticPoints(Player player, MMOType type, int points) {
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

    public static void setRecipeMenuMapping(String recipeName) {
        if (materialMenuMapping.containsKey(recipeName.toLowerCase())) return;
        materialMenuMapping.put(recipeName.toLowerCase(), MENU_TEMPLATE_RECIPE_MATERIAL);
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

    private static int getLevelById(String id) {
        int level = -1;
        if (StringUtils.isBlank(id)) return level;
        String[] keys = id.split("\\.");
        if (keys.length >= 2 && StringUtils.isNumeric(keys[1]))
            level = Integer.parseInt(keys[1]);

        return level;
    }
}
