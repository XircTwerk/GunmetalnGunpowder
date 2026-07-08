package com.xirc.milicraft.common.data.gun;

import com.google.gson.JsonObject;
import com.xirc.milicraft.common.item.GunPartType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A fixed gun bench recipe: the part counts required to assemble one gun.
 * The result supplier defers item resolution until registries are ready.
 * The category groups guns in the bench screen's category tabs.
 */
public record GunAssemblyRecipe(Supplier<? extends Item> result, String category, Map<GunPartType, Integer> parts) {
    public int required(GunPartType type) {
        return parts.getOrDefault(type, 0);
    }

    public static GunAssemblyRecipe fromJson(JsonObject json) {
        ResourceLocation resultId = new ResourceLocation(GsonHelper.getAsString(json, "result"));
        Item item = BuiltInRegistries.ITEM.getOptional(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown result item " + resultId));
        String category = GsonHelper.getAsString(json, "category");
        JsonObject partsJson = GsonHelper.getAsJsonObject(json, "parts");
        Map<GunPartType, Integer> parts = new EnumMap<>(GunPartType.class);
        for (GunPartType type : GunPartType.values()) {
            if (partsJson.has(type.id())) {
                parts.put(type, GsonHelper.getAsInt(partsJson, type.id()));
            }
        }
        return new GunAssemblyRecipe(() -> item, category, parts);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("result", BuiltInRegistries.ITEM.getKey(result.get()).toString());
        json.addProperty("category", category);
        JsonObject partsJson = new JsonObject();
        for (Map.Entry<GunPartType, Integer> entry : parts.entrySet()) {
            partsJson.addProperty(entry.getKey().id(), entry.getValue());
        }
        json.add("parts", partsJson);
        return json;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(result.get()));
        buf.writeUtf(category);
        buf.writeVarInt(parts.size());
        for (Map.Entry<GunPartType, Integer> entry : parts.entrySet()) {
            buf.writeEnum(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }

    public static GunAssemblyRecipe read(FriendlyByteBuf buf) {
        Item item = BuiltInRegistries.ITEM.get(buf.readResourceLocation());
        String category = buf.readUtf();
        int size = buf.readVarInt();
        Map<GunPartType, Integer> parts = new EnumMap<>(GunPartType.class);
        for (int i = 0; i < size; i++) {
            parts.put(buf.readEnum(GunPartType.class), buf.readVarInt());
        }
        return new GunAssemblyRecipe(() -> item, category, parts);
    }
}
