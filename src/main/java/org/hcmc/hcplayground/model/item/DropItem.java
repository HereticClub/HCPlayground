package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.hcmc.hcplayground.utility.MaterialData;

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
    @SerializedName(value = "materials")
    public MaterialData[] materials;
    /**
     * 额外掉落物品的列表<br>
     * 可以是自定义物品<br>
     */
    @Expose
    @SerializedName(value = "drops")
    public ItemBase[] drops;
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
