package com.xirc.militech.common.event;

import com.xirc.militech.common.data.gun.GunDataSync;
import com.xirc.militech.common.item.AbstractGunItem;
import com.xirc.militech.common.system.GunAiming;
import com.xirc.militech.common.system.block.BulletBlockDamage;
import com.xirc.militech.common.tickable.GunReloadQueue;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;

public final class MilitechEvents {
    private MilitechEvents() {
    }

    public static void init() {
        TickEvent.SERVER_POST.register(GunReloadQueue::tick);
        TickEvent.SERVER_POST.register(BulletBlockDamage::tick);
        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, face) ->
                AbstractGunItem.handleLeftClick(player) ? EventResult.interruptTrue() : EventResult.pass());
        PlayerEvent.PLAYER_QUIT.register(player -> GunAiming.set(player, false));
        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd) -> GunAiming.set(player, false));
        LifecycleEvent.SERVER_STARTED.register(GunDataSync::setServer);
        LifecycleEvent.SERVER_STOPPED.register(server -> GunDataSync.setServer(null));
        PlayerEvent.PLAYER_JOIN.register(GunDataSync::syncTo);
    }
}
