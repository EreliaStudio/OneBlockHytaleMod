package com.EreliaStudio.OneBlock;

import java.util.UUID;

/** Optional integration surface for mods interested in OneBlock progression. */
public interface OneBlockProgressListener {
    default void onExpeditionUnlocked(UUID playerId, String expeditionId) {}
    default void onExpeditionCompleted(UUID playerId, String expeditionId) {}
    default void onDungeonCompleted(UUID playerId, String dungeonId) {}
}
