package org.hcmc.hcplayground.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.hcmc.hcplayground.itemManager.ItemBase;
import org.hcmc.hcplayground.itemManager.ItemBaseA;
import org.hcmc.hcplayground.itemManager.ItemManager;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class ItemBaseDeserializer implements JsonDeserializer<ItemBase> {

    public ItemBaseDeserializer() {

    }

    @Override
    public ItemBase deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String jsonValue = jsonElement.getAsString();
        List<Material> materials = Arrays.stream(Material.values()).toList();
        Material m = materials.stream().filter(x -> x.name().equalsIgnoreCase(jsonValue)).findAny().orElse(null);
        ItemBase ib = ItemManager.FindItemById(jsonValue);

        if (ib == null) {
            ib = new ItemBaseX();
            ib.setId(null);
            ib.setMaterial(m);
        }
        Bukkit.createInventory(null, InventoryType.CHEST);
        return ib;
    }

    private static class ItemBaseX extends ItemBaseA{

        @Override
        public ItemStack toItemStack() {
            return new ItemStack(this.getMaterial());
        }
    }
}
