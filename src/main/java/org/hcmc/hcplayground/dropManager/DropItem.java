package org.hcmc.hcplayground.dropManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Material;
import org.hcmc.hcplayground.itemManager.ItemBaseA;

/**
 * 挖掘采集时概率额外掉落的物品
 */
public class DropItem {

    /**
     * 掉落概率
     */
    @Expose
    @SerializedName(value = "rate")
    public float rate = 0.0F;
    /**
     * 被挖掘或采集的方块
     */
    @Expose
    @SerializedName(value = "block")
    public Material block = Material.STONE;
    /**
     * 额外掉落物品的列表<br>
     * 可以是自定义物品<br>
     */
    @Expose
    @SerializedName(value = "drops")
    public ItemBaseA[] drops;
    /**
     * 如果方块是作物，则可再判断其年龄
     * 比如小麦胡萝卜的成熟时年龄为7
     */
    @Expose
    @SerializedName(value = "age")
    public int age = 0;

    public DropItem() {

    }
}
