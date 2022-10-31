package org.hcmc.hcplayground.model.armorset;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.entity.Player;
import org.hcmc.hcplayground.enums.ArmorSetType;
import org.hcmc.hcplayground.manager.PlayerManager;
import org.hcmc.hcplayground.model.player.PlayerData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 装备套装效果类
 */
public class ArmorSetEffect {

    /**
     * 说明
     */
    @Expose
    @SerializedName(value = "lore")
    private List<String> lore = new ArrayList<>();

    /**
     * 套装的被动效果，只要装备的数量足够并且穿戴在身上，效果便会被触发<br>
     * Integer - 激活套装效果的装备数量需求<br>
     * {@code Map<String, Object>} - 套装效果<br>
     * ArmorSetType - 效果的类型，可以是某种药水效果，或者增加生命值，攻击值等等，也可以是每5秒触发xxx效果等等<br>
     * Object - 触发效果的值，某种药水名称，生命值，攻击值，每至少x秒效果类型等等<br>
     */
    @Expose(deserialize = false)
    private Map<Integer, Map<ArmorSetType, Object>> passiveEffect = new HashMap<>();
    /**
     * 当前套装效果实例的装备数量需求列表
     */
    @Expose(deserialize = false)
    private List<Integer> requireAmount = new ArrayList<>();
    /**
     * 套装效果名称
     */
    @Expose(deserialize = false)
    private String name;
    /**
     * id
     */
    @Expose(deserialize = false)
    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getRequireAmount() {
        return requireAmount;
    }

    public void setRequireAmount(List<Integer> requireAmount) {
        this.requireAmount = requireAmount;
    }

    public String getId() {
        return id;
    }

    public List<String> getLore() {
        return new ArrayList<>(lore);
    }

    public Map<Integer, Map<ArmorSetType, Object>> getPassiveEffect() {
        return new HashMap<>(passiveEffect);
    }

    public void setPassiveEffect(Map<Integer, Map<ArmorSetType, Object>> passiveEffect) {
        this.passiveEffect = new HashMap<>(passiveEffect);
    }

    public ArmorSetEffect() {

    }

    /**
     * 根据已装备的盔甲数量获取可激活的盔甲套装的装备数量需求列表
     * @param equippedAmount 已经装备的盔甲数量
     * @return 可激活的盔甲套装效果的装备数量需求列表
     */
    public List<Integer> getAmountActivated(int equippedAmount) {
        return requireAmount.stream().filter(x -> x <= equippedAmount).toList();
    }

    public void remove(Player player, List<Integer> amounts) {
        PlayerData data = PlayerManager.getPlayerData(player);
        Map<String, List<Integer>> activated = data.getActivatedArmorSetEffects();
        if (!activated.containsKey(name)) return;
        List<Integer> _amount = activated.get(name);

        for (int amount : amounts) {
            if (_amount.stream().noneMatch(x -> x == amount)) continue;
            if (!passiveEffect.containsKey(amount)) continue;

            _amount.remove(amount);
            Map<ArmorSetType, Object> armorTypes = passiveEffect.get(amount);
            for (Map.Entry<ArmorSetType, Object> entry : armorTypes.entrySet()) {
                switch (entry.getKey()) {
                    case HEALTH -> {
                        double _baseValue = Double.parseDouble(String.valueOf(entry.getValue()));
                        double baseValue = data.getBaseHealth();
                        data.setBaseHealth(baseValue - _baseValue);
                    }
                    case ARMOR -> {
                        double _baseValue = Double.parseDouble(String.valueOf(entry.getValue()));
                        double baseValue = data.getBaseArmor();
                        data.setBaseArmor(baseValue - _baseValue);
                    }
                }
            }
        }

        if (_amount.size() == 0)
            activated.remove(name);
        else
            activated.replace(name, _amount);
    }
}
