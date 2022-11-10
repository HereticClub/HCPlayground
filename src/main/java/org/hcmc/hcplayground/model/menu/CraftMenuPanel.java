package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.manager.MMOManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class CraftMenuPanel implements MenuPanel {

    @Expose
    @SerializedName(value = "title")
    protected String title;
    @Expose
    @SerializedName(value = "inventory-type")
    protected InventoryType inventoryType = InventoryType.CHEST;
    @Expose
    @SerializedName(value = "size")
    protected int size = 54;
    @Expose
    @SerializedName(value = "worlds")
    protected List<String> enableWorlds = new ArrayList<>();

    /**
     * 菜单的页码
     */
    @Expose(deserialize = false)
    protected int page = 1;
    @Expose(deserialize = false)
    protected List<MenuPanelSlot> decorates = new ArrayList<>();
    @Expose(deserialize = false)
    protected String id;
    @Expose(deserialize = false)
    protected Inventory inventory;

    protected static YamlConfiguration yaml = MMOManager.getYaml();

    @Override
    public void OnClosed(Player player, String menuId) {

    }

    protected @NotNull Inventory createAnvilInventory(String title) {
        return Bukkit.createInventory(this, inventoryType, title);
    }

    protected @NotNull Inventory createChestInventory(String title, Player player) {
        String _title = StringUtils.isBlank(title) ? "" : title;
        Inventory inv = Bukkit.createInventory(this, size, _title);

        for (MenuPanelSlot slot : decorates) {
            Map<Integer, ItemStack> itemStacks = slot.createItemStacks(player);
            for (Map.Entry<Integer, ItemStack> entry : itemStacks.entrySet()) {
                inv.setItem(entry.getKey() - 1, entry.getValue());
            }
        }

        return inv;
    }

    @Override
    public boolean existDecorate(List<Integer> slots) {
        return decorates.stream().anyMatch(x -> new HashSet<>(x.getSlots()).containsAll(slots));
    }

    @Override
    public MenuPanelSlot getDecorate(int slot) {
        MenuPanelSlot s = decorates.stream().filter(x -> x.getSlots().contains(slot)).findAny().orElse(null);
        return s == null ? null : (MenuPanelSlot) s.clone();
    }

    @Override
    public MenuPanelSlot getDecorate(String name) {
        String id = String.format("%s.%s", MenuPanelSlot.class.getSimpleName(), name);
        return decorates.stream().filter(x -> x.getId().equalsIgnoreCase(id)).findAny().orElse(null);
    }

    @Override
    public List<MenuPanelSlot> getDecorates() {
        return decorates;
    }

    @Override
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public int getPage() {
        return page;
    }

    @Override
    public void setPage(int page) {
        this.page = page <= 0 ? 1 : page;
    }

    @Override
    public boolean isDisabled(Player player) {
        String worldName = player.getWorld().getName();
        if (enableWorlds == null) enableWorlds = new ArrayList<>();
        boolean matchWorld = enableWorlds.stream().anyMatch(x -> x.equalsIgnoreCase(worldName));
        return enableWorlds.size() >= 1 && !matchWorld && !player.isOp();
    }

    @Override
    public void setDecorates(List<MenuPanelSlot> decorates) {
        this.decorates = new ArrayList<>(decorates);
    }

    @Override
    public void setId(String value) {
        this.id = value;
    }

    @Override
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}
