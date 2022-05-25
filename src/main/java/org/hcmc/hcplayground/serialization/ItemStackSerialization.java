package org.hcmc.hcplayground.serialization;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

public class ItemStackSerialization implements JsonDeserializer<ItemStack>, JsonSerializer<ItemStack> {

    public ItemStackSerialization() {

    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String value = jsonElement.getAsString();

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(value));
            BukkitObjectInputStream bukkitStream = new BukkitObjectInputStream(inputStream);

            ItemStack is = (ItemStack) bukkitStream.readObject();

            inputStream.close();
            bukkitStream.close();
            return is;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonElement serialize(ItemStack itemStack, Type type, JsonSerializationContext jsonSerializationContext) {
        String itemStackValue;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(outputStream);

            bukkitStream.writeObject(itemStack);
            itemStackValue = Base64Coder.encodeLines(outputStream.toByteArray());

            bukkitStream.close();
            outputStream.close();
            return new JsonPrimitive(itemStackValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
