package com.xirc.milicraft.common.item;

/**
 * One segment of a reload, played in sequence by the reload queue.
 * <p>
 * Each part drives its own geo animation clip and a per-part sound hook
 * ({@link AbstractGunItem#reloadPartSound(String)}), so a distinct sound can be
 * attached to every step (e.g. mag-out, mag-in, slide rack).
 *
 * @param animation geo animation clip name for this part
 * @param ticks     how long this part lasts, in ticks
 */
public record ReloadPart(String animation, int ticks) {
}
