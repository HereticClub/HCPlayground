package org.hcmc.hcplayground.model.mmo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.enums.MMOType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MMOCollectionCategory {
    /**
     * 收集类型
     */
    @Expose
    @SerializedName(value = "type")
    private MMOType type;
    /**
     * 收集类型的显示名称
     */
    @Expose
    @SerializedName(value = "name")
    private String name;
    /**
     * 收集进度菜单中不可用的插槽位置
     */
    @Expose
    @SerializedName(value = "inactive-slots")
    private List<Integer> inactiveSlots = new ArrayList<>();

    public MMOCollectionCategory() {

    }

    public String getName() {
        return name;
    }

    public MMOType getType() {
        return type;
    }

    public Map<Integer, ItemStack> decorate(Material[] materials) {
        Map<Integer, ItemStack> itemStacks = new HashMap<>();
        int activeSlot = 1;

        for (Material material : materials) {
            ItemStack is = new ItemStack(material, 1);

            while (inactiveSlots.contains(activeSlot)) {
                activeSlot++;
            }

            itemStacks.put(activeSlot, is);
            activeSlot++;
        }

        return itemStacks;
    }
}
