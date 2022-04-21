package org.hcmc.hcplayground.model.inventory;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InventoryDetail implements InventoryHolder {

    @Expose
    @SerializedName(value = "canDrag")
    public boolean draggable = false;
    @Expose
    @SerializedName(value = "canDrop")
    public boolean droppable = false;
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
    @SerializedName(value = "decorates")
    public List<InventorySlot> decorates = new ArrayList<>();

    @Expose(serialize = false, deserialize = false)
    public String id;
    @Expose(serialize = false, deserialize = false)
    private Inventory inventory;

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public InventorySlot getSlot(int slot) {
        return decorates.stream().filter(x -> x.number == slot).findAny().orElse(null);
    }
}
