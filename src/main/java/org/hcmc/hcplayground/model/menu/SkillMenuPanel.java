package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.manager.MMOManager;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.mmo.MMOLevelTemplate;
import org.hcmc.hcplayground.model.mmo.MMOSkillTemplate;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.serialization.SkillTypeSerialization;

import java.util.List;
import java.util.Map;

public class SkillMenuPanel extends CraftMenuPanel {

    @Expose(deserialize = false)
    private SkillType type = SkillType.UNDEFINED;

    public SkillMenuPanel() {

    }

    @Override
    public void OnConfigured(YamlConfiguration yaml) {

    }

    @Override
    public Inventory OnOpening(Player player, String menuId) {
        PlayerData data = PlayerManager.getPlayerData(player);
        this.type = SkillTypeSerialization.valueOf(menuId);

        switch (this.type){
            case SKILL_COMBAT -> {
                int statistic = data.statisticEntityKilled(MMOManager.Monsters);
                setupInventory(player, statistic);
            }
            case SKILL_FARMING -> {
                int statistic = data.statisticFarming();
                setupInventory(player, statistic);
            }
            case SKILL_FISHING -> {
                int statistic = data.statisticFishingCaught();
                setupInventory(player, statistic);
            }
            case SKILL_LUMBERING -> {
                int statistic = data.statisticBlockMined(MMOManager.LumberingBlocks);
                setupInventory(player, statistic);
            }
            case SKILL_MINING -> {
                int statistic = data.statisticBlockMined(MMOManager.MiningBlocks);
                setupInventory(player, statistic);
            }
            case SKILL_CRAFTING -> inventory = statisticSkillCrafting(player);
            case SKILL_ENCHANTING -> inventory = statisticSkillEnchanting(player);
            case SKILL_POTION -> inventory = statisticSkillPotion(player);
            case SKILL_TAMING -> inventory = statisticSkillTaming(player);
        }

        if (inventory == null) return null;
        return inventory;
    }

    private void setupInventory(Player player, int statistic) {
        MMOSkillTemplate skill = MMOManager.getSkillTemplate(type);
        if (skill == null) return;

        List<MMOLevelTemplate> levels = skill.getUnclaimedLevels(player);
        String _title = StringUtils.isBlank(skill.getName()) ? title : skill.getName();
        inventory = Bukkit.createInventory(this, size, _title);

        for (MenuPanelSlot slot : decorates) {
            List<String> lore = slot.getLore();
            lore.replaceAll(x->x.replace("%levels%", String.valueOf(levels.size())));
            slot.setLore(lore);

            Map<Integer, List<String>> lefts = slot.getLeftCommands();
            for (Map.Entry<Integer, List<String>> entry : lefts.entrySet()) {
                entry.getValue().replaceAll(x -> x.replace("%skill_id%", type.name().toLowerCase()));
            }

            Map<Integer, ItemStack> itemStacks = slot.createItemStacks(player);
            for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
                inventory.setItem(entry.getKey() - 1, entry.getValue());
            }
        }

        Map<Integer, ItemStack> itemStacks = skill.decorateLevels(statistic);
        for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    private Inventory statisticSkillEnchanting(Player player) {
        return null;
    }

    private Inventory statisticSkillPotion(Player player) {
        return null;
    }

    private Inventory statisticSkillCrafting(Player player) {
        return null;
    }

    private Inventory statisticSkillTaming(Player player) {
        return null;
    }

    public enum SkillType{
        /**
         * 战斗技能
         */
        SKILL_COMBAT,
        /**
         * 农业技能
         */
        SKILL_FARMING,
        /**
         * 渔业技能
         */
        SKILL_FISHING,
        /**
         * 伐木技能
         */
        SKILL_LUMBERING,
        /**
         * 矿产技能
         */
        SKILL_MINING,
        /**
         * 附魔技能
         */
        SKILL_ENCHANTING,
        /**
         * 酿造技能
         */
        SKILL_POTION,
        /**
         * 制作技能
         */
        SKILL_CRAFTING,
        /**
         * 驯养技能
         */
        SKILL_TAMING,
        /**
         * 未定义，用作普通菜单界面
         */
        UNDEFINED,
    }
}
