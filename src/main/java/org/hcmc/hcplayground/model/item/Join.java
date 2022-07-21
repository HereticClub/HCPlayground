package org.hcmc.hcplayground.model.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.awt.print.Book;
import java.util.*;

public class Join extends CraftItemBase {
    /**
     * 在主手时左键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mainhand-left-click")
    private List<String> mainHandLeftClicks = new ArrayList<>();
    /**
     * 在主手时右键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mainhand-right-click")
    private List<String> mainHandRightClicks = new ArrayList<>();
    /**
     * 在菜单时左键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mouse-left-click")
    private List<String> mouseLeftClicks = new ArrayList<>();
    /**
     * 在菜单时右键点击的运行指令列表
     */
    @Expose
    @SerializedName(value = "mouse-right-click")
    private List<String> mouseRightClicks = new ArrayList<>();
    /**
     * 表示该物品是一本书，以及页数和每页内容的列表
     * 当物品被识别为书本时，属性MainHandLeftClick, MainHandRightClick, mouseLeftClicks, MouseRightClick将会被忽略
     * 并且禁止这些动作
     */
    @Expose
    @SerializedName(value = "pages")
    //public List<Page> Pages = new ArrayList<>();
    private Map<Integer, List<String>> pages = new HashMap<>();
    /**
     * 表示该物品是一本书，以及该书本的作者
     */
    @Expose
    @SerializedName(value = "author")
    private String author = "";
    /**
     * 表示该物品是一本书，以及该书本的标题
     */
    @Expose
    @SerializedName(value = "title")
    private String title = "";
    /**
     * 书本的创作模式
     * ORIGINAL - 书本内容是原创
     * COPY_OF_COPY - 书本内容来自于原创的副本
     * COPY_OF_ORIGINAL - 书本内容来自于原创
     * TATTERED - 破烂的书本，玩家无法获得
     */
    @Expose
    @SerializedName(value = "generation")
    private BookMeta.Generation generation = BookMeta.Generation.ORIGINAL;

    public Join() {

    }

    public List<String> getMainHandLeftClicks() {
        return mainHandLeftClicks;
    }

    public List<String> getMainHandRightClicks() {
        return mainHandRightClicks;
    }

    public List<String> getMouseLeftClicks() {
        return mouseLeftClicks;
    }

    public List<String> getMouseRightClicks() {
        return mouseRightClicks;
    }

    public Map<Integer, List<String>> getPages() {
        return pages;
    }

    public ItemStack toBook(Player player) {
        ItemStack is = toItemStack();

        BookMeta bm = setBookMeta(player, is);
        if (bm != null) is.setItemMeta(bm);

        return is;
    }

    @Override
    public void updateAttributeLore() {

    }

    public ItemStack toItemStack() {
        ItemStack is = new ItemStack(getMaterial().value, getAmount());
        // 判断物品只是简单的物品，即没有额外的Meta类型的数据
        // 设置基本的ItemMeta
        ItemMeta im = this.setBaseItemMeta(is);
        // 添加附魔效果
        if (getGlowing()) {
            im.addEnchant(Enchantment.MENDING, 1, true);
            if (!flags.contains(ItemFlag.HIDE_ENCHANTS))
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        is.setItemMeta(im);
        return is;
    }

    private BookMeta setBookMeta(@NotNull Player player, @NotNull ItemStack is) {
        ItemMeta im = is.getItemMeta();
        if(!(im instanceof BookMeta meta)) return null;
        if (!StringUtils.isBlank(author)) meta.setAuthor(author);
        if (!StringUtils.isBlank(title)) meta.setTitle(title);
        List<String> contents = new ArrayList<>();
        // 获取所有页面
        Set<Integer> keys = pages.keySet();
        // 获取每页内容
        for (Integer i : keys) {
            List<String> text = pages.get(i);
            // 书本的每一行内容最后都要添加\n字符
            // 书本的每一页内容都是单个字符串实例
            StringBuilder _line = new StringBuilder();
            for (String s : text) {
                _line.append(String.format("%s§r\n", s));
            }
            // 把内容添加到当前页面
            String str =PlaceholderAPI.setPlaceholders(player, _line.toString());
            contents.add(str);
        }
        meta.addPage(contents.toArray(new String[0]));
        meta.setGeneration(generation);

        return meta;
    }
}
