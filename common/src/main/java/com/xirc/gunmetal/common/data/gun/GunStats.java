package com.xirc.gunmetal.common.data.gun;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;

public record GunStats(
        int maxRounds,
        float damage,
        float range,
        float knockback,
        int barrels,
        int pelletsPerBarrel,
        float spread,
        float caliber,
        float bulletLength,
        int stunTicks,
        int inputCooldownTicks,
        int refireCooldownTicks,
        int reloadCooldownTicks,
        int reloadDurationTicks
) {
    public static GunStats fromJson(JsonObject json) {
        int refireCooldownTicks = GsonHelper.getAsInt(json, "refire_cooldown_ticks");
        int reloadDurationTicks = GsonHelper.isValidNode(json, "reload_duration_ticks")
                ? GsonHelper.getAsInt(json, "reload_duration_ticks")
                : GsonHelper.getAsInt(json, "reload_step_ticks");
        float damage = GsonHelper.getAsFloat(json, "damage");
        return new GunStats(
                GsonHelper.getAsInt(json, "max_rounds"),
                damage,
                GsonHelper.getAsFloat(json, "range"),
                GsonHelper.getAsFloat(json, "knockback"),
                GsonHelper.getAsInt(json, "barrels"),
                GsonHelper.getAsInt(json, "pellets_per_barrel"),
                GsonHelper.getAsFloat(json, "spread"),
                GsonHelper.getAsFloat(json, "caliber"),
                GsonHelper.getAsFloat(json, "bullet_length"),
                GsonHelper.getAsInt(json, "stun_ticks"),
                GsonHelper.getAsInt(json, "input_cooldown_ticks"),
                refireCooldownTicks,
                GsonHelper.getAsInt(json, "reload_cooldown_ticks"),
                reloadDurationTicks);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("max_rounds", maxRounds);
        json.addProperty("damage", damage);
        json.addProperty("range", range);
        json.addProperty("knockback", knockback);
        json.addProperty("barrels", barrels);
        json.addProperty("pellets_per_barrel", pelletsPerBarrel);
        json.addProperty("spread", spread);
        json.addProperty("caliber", caliber);
        json.addProperty("bullet_length", bulletLength);
        json.addProperty("stun_ticks", stunTicks);
        json.addProperty("input_cooldown_ticks", inputCooldownTicks);
        json.addProperty("refire_cooldown_ticks", refireCooldownTicks);
        json.addProperty("reload_cooldown_ticks", reloadCooldownTicks);
        json.addProperty("reload_duration_ticks", reloadDurationTicks);
        return json;
    }
}
