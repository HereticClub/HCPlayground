package org.hcmc.hcplayground.model.menu;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MenuDetail implements InventoryHolder {

    @Expose
    @SerializedName(value = "title")
    public String title;
    @Expose
    @SerializedName(value = "type")
    public InventoryType type = InventoryType.CHEST;
    @Expose
    @SerializedName(value = "size")
    public int size = 54;
    @Expose
    @SerializedName(value = "worlds")
    public List<String> enableWorlds = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    public String id;
    @Expose(deserialize = false)
    public List<MenuItem> decorates = new ArrayList<>();
    @Expose(serialize = false, deserialize = false)
    private Inventory inventory;

    public MenuDetail() {

    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public MenuItem getSlot(int slot) {
        return decorates.stream().filter(x -> x.numbers.contains(slot)).findAny().orElse(null);
    }
}
