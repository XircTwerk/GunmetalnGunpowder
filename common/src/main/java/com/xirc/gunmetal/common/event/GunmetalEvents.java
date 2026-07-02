package com.xirc.gunmetal.common.event;

import com.xirc.gunmetal.common.item.AbstractGunItem;
import com.xirc.gunmetal.common.system.GunAiming;
import com.xirc.gunmetal.common.system.block.BulletBlockDamage;
import com.xirc.gunmetal.common.tickable.GunReloadQueue;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

public final class GunmetalEvents {
    private GunmetalEvents() {
    }

    public static void init() {
        TickEvent.SERVER_POST.register(GunReloadQueue::tick);
        TickEvent.SERVER_POST.register(BulletBlockDamage::tick);
        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, face) ->
                AbstractGunItem.handleLeftClick(player) ? EventResult.interruptTrue() : EventResult.pass());
        PlayerEvent.PLAYER_QUIT.register(player -> GunAiming.set(player, false));
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd) -> GunAiming.set(player, false));
    }
}
