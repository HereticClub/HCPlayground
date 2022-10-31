package org.hcmc.hcplayground.manager;

import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.enums.ArmorSetType;
import org.hcmc.hcplayground.model.armorset.ArmorSetEffect;
import org.hcmc.hcplayground.model.player.PlayerData;
import org.hcmc.hcplayground.utility.Global;

import java.lang.reflect.Type;
import java.util.*;

public class ArmorSetManager {

    private static List<ArmorSetEffect> armorSetEffects = new ArrayList<>();
    private static List<String> idList = new ArrayList<>();

    public ArmorSetManager() {

    }

    public static List<String> getIdList() {
        return idList;
    }

    public static void Load(YamlConfiguration yaml) {
        ConfigurationSection armorSetSection = yaml.getConfigurationSection("armor_sets");
        if (armorSetSection == null) return;

        Type mapStringObject = new TypeToken<Map<ArmorSetType, Object>>() {
        }.getType();

        armorSetEffects = Global.deserializeList(armorSetSection, ArmorSetEffect.class);
        idList = new ArrayList<>(armorSetSection.getKeys(false));

        for (ArmorSetEffect armorSetEffect : armorSetEffects) {
            String name = armorSetEffect.getId().split("\\.")[1];
            String amountPath = String.format("%s.amount", name);
            ConfigurationSection amountSection = armorSetSection.getConfigurationSection(amountPath);
            if (amountSection == null) continue;
            List<Integer> amounts = new ArrayList<>();
            Map<Integer, Map<ArmorSetType, Object>> armorAmount = new HashMap<>();

            Set<String> contentKeys = amountSection.getKeys(false);
            for (String s : contentKeys) {
                int amount = Integer.parseInt(s);
                amounts.add(amount);

                String contentPath = String.format("%s", s);
                ConfigurationSection contentSection = amountSection.getConfigurationSection(contentPath);
                if (contentSection == null) continue;

                String contentValue = Global.GsonObject.toJson(contentSection.getValues(false));
                Map<ArmorSetType, Object> content = Global.GsonObject.fromJson(contentValue, mapStringObject);
                armorAmount.put(amount, content);
            }

            armorSetEffect.setPassiveEffect(armorAmount);
            armorSetEffect.setRequireAmount(amounts);
            armorSetEffect.setName(name);
        }
    }

    public static ArmorSetEffect getArmorSetEffect(String name) {
        return armorSetEffects.stream().filter(x -> x.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public static void applyArmorSetEffect(Player player, Map<String, List<Integer>> effects) {
        PlayerData data = PlayerManager.getPlayerData(player);
        Map<String, List<Integer>> activated = data.getActivatedArmorSetEffects();

        for (Map.Entry<String, List<Integer>> entry : activated.entrySet()) {
            removeArmorSetEffect(data, entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, List<Integer>> entry : effects.entrySet()) {
            addArmorSetEffect(data, entry.getKey(), entry.getValue());
        }
        data.setActivatedArmorSetEffects(effects);
        PlayerManager.setPlayerData(player, data);
    }

    private static void addArmorSetEffect(PlayerData data, String name, List<Integer> amounts) {
        ArmorSetEffect effect = getArmorSetEffect(name);
        if (effect == null) return;
        Map<Integer, Map<ArmorSetType, Object>> passiveEffects = effect.getPassiveEffect();
        Set<Integer> keys = passiveEffects.keySet();

        for (int amount : amounts) {
            if (keys.stream().noneMatch(x -> x == amount)) continue;
            Map<ArmorSetType, Object> types = passiveEffects.get(amount);
            for (ArmorSetType type : types.keySet()) {
                processArmorSetType(data, type, types.get(type), true);
            }
        }
    }

    private static void removeArmorSetEffect(PlayerData data, String name, List<Integer> amounts) {
        ArmorSetEffect effect = getArmorSetEffect(name);
        if (effect == null) return;
        Map<Integer, Map<ArmorSetType, Object>> passiveEffects = effect.getPassiveEffect();
        Set<Integer> keys = passiveEffects.keySet();

        for (int amount : amounts) {
            if (keys.stream().noneMatch(x -> x == amount)) continue;
            Map<ArmorSetType, Object> types = passiveEffects.get(amount);
            for (ArmorSetType type : types.keySet()) {
                processArmorSetType(data, type, types.get(type), false);
            }
        }
    }

    private static void processArmorSetType(PlayerData data, ArmorSetType type, Object value, boolean operator){
        double _value = Double.parseDouble(String.valueOf(value));
        switch (type) {
            case ARMOR -> {
                if (operator)
                    data.increaseBaseArmor(_value);
                else
                    data.decreaseBaseArmor(_value);
            }
            case ARMOR_TOUGHNESS -> {
                if (operator)
                    data.increaseBaseArmorToughness(_value);
                else
                    data.decreaseBaseArmorToughness(_value);
            }
            case ATTACK_DAMAGE -> {
                if (operator)
                    data.increaseBaseAttackDamage(_value);
                else
                    data.decreaseBaseAttackDamage(_value);
            }
            case ATTACK_REACH -> {
                if (operator)
                    data.increaseBaseAttackReach(_value);
                else
                    data.decreaseBaseAttackReach(_value);
            }
            case ATTACK_SPEED -> {
                if (operator)
                    data.increaseBaseAttackSpeed(_value);
                else
                    data.decreaseBaseAttackSpeed(_value);
            }
            case BLOOD_SUCKING -> {
                if (operator)
                    data.increaseBaseBloodSucking(_value);
                else
                    data.decreaseBaseBloodSucking(_value);
            }
            case CRITICAL -> {
                if (operator)
                    data.increaseBaseCritical(_value);
                else
                    data.decreaseBaseCritical(_value);
            }
            case CRITICAL_DAMAGE -> {
                if (operator)
                    data.increaseBaseCriticalDamage(_value);
                else
                    data.decreaseBaseCriticalDamage(_value);
            }
            case FORTUNE -> {
                if (operator)
                    data.increaseBaseLuck(_value);
                else
                    data.decreaseBaseLuck(_value);
            }
            case INTELLIGENCE -> {
                if (operator)
                    data.increaseBaseIntelligence(_value);
                else
                    data.decreaseBaseIntelligence(_value);
            }
            case KNOCK_BACK -> {
                if (operator)
                    data.increaseBaseKnockBackResistance(_value);
                else
                    data.decreaseBaseKnockBackResistance(_value);
            }
            case MOVEMENT_SPEED -> {
                if (operator)
                    data.increaseBaseMovementSpeed(_value);
                else
                    data.decreaseBaseMovementSpeed(_value);
            }
            case RECOVER -> {
                if (operator)
                    data.increaseBaseRecover(_value);
                else
                    data.decreaseBaseRecover(_value);
            }
            case HEALTH -> {
                if (operator)
                    data.increaseBaseHealth(_value);
                else
                    data.decreaseBaseHealth(_value);
            }
            case DIGGING_SPEED -> {
                if (operator)
                    data.increaseBaseDiggingSpeed(_value);
                else
                    data.decreaseBaseDiggingSpeed(_value);
            }
            case LOGGING_SPEED -> {
                if (operator)
                    data.increaseBaseLoggingSpeed(_value);
                else
                    data.decreaseBaseLoggingSpeed(_value);
            }
        }
    }
}
