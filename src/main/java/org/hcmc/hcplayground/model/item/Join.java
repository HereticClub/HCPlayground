package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Join extends ItemBaseA {
    /**
     * 在主手时左键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mainhand-left-click")
    public List<String> MainHandLeftClick = new ArrayList<>();
    /**
     * 在主手时右键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mainhand-right-click")
    public List<String> MainHandRightClick = new ArrayList<>();
    /**
     * 在菜单时左键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mouse-left-click")
    public List<String> MouseLeftClick = new ArrayList<>();
    /**
     * 在菜单时右键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mouse-right-click")
    public List<String> MouseRightClick = new ArrayList<>();
    /**
     * 该物品可以允许使用的世界列表
     */
    @Expose
    @SerializedName(value = "worlds")
    public List<String> Worlds = new ArrayList<>();
    /**
     * 表明该物品是一本书，以及页数和每页内容的列表
     * 当物品被识别为书本时，属性MainHandLeftClick, MainHandRightClick, MouseLeftClick, MouseRightClick将会被忽略
     * 并且禁止这些动作
     */
    @Expose
    @SerializedName(value = "pages")
    public List<Page> Pages = new ArrayList<>();
    /**
     * 表明该物品是一本书，以及该书本的作者
     */
    @Expose
    @SerializedName(value = "author")
    public String Author = "";
    /**
     * 表明该物品是一本书，以及该书本的标题
     */
    @Expose
    @SerializedName(value = "title")
    public String Title = "";
    /**
     * 书本的创作模式
     * ORIGINAL - 书本内容是原创
     * COPY_OF_COPY - 书本内容来自于原创的副本
     * COPY_OF_ORIGINAL - 书本内容来自于原创
     * TATTERED - 破烂的书本，玩家无法获得
     */
    @Expose
    @SerializedName(value = "generation")
    public BookMeta.Generation Generation = BookMeta.Generation.ORIGINAL;

    public Join() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(getMaterial().value, getAmount());
        ItemMeta im = is.getItemMeta();
        // 判断物品是书本
        if (im instanceof BookMeta bm) {
            setBookMeta(bm);
            is.setItemMeta(bm);
            return is;
        }
        // 判断物品只是简单的物品，即没有额外的Meta类型的数据
        // 设置基本的ItemMeta
        im = this.setBaseItemMeta(is);
        // 添加附魔效果
        if (this.getGlowing()) {
            im.addEnchant(Enchantment.MENDING, 1, true);
            if (!Arrays.asList(this.getFlags()).contains(ItemFlag.HIDE_ENCHANTS))
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return is;
    }

    private void setBookMeta(BookMeta meta) {
        if (!Author.equalsIgnoreCase("")) meta.setAuthor(Author);
        if (!Title.equalsIgnoreCase("")) meta.setTitle(Title);
        Pages.sort(Comparator.comparing(x -> x.Number));
        for (Page p : Pages) {
            // 书本的每一行内容最后都要添加\n字符
            // 书本的每一页内容都是单个字符串实例
            StringBuilder content = new StringBuilder();
            for (String s : p.Content) {
                content.append(String.format("%s§r\n", s));
            }
            // 每一个字符串实例算添加一页
            meta.addPage(content.toString());
        }
        meta.setGeneration(Generation);
    }
}
