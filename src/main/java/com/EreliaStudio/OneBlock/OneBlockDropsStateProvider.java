package com.EreliaStudio.OneBlock;

import java.util.List;
import java.util.UUID;

public interface OneBlockDropsStateProvider
{
    List<String> getUnlockedDrops(UUID playerId, String expeditionId);

    boolean isUnlocked(UUID playerId, String expeditionId, String dropItemId);

    boolean unlock(UUID playerId, String expeditionId, String dropItemId);

    boolean lock(UUID playerId, String expeditionId, String dropItemId);

    default List<String> getUnlockedDrops(UUID playerId)
    {
        return getUnlockedDrops(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION);
    }

    default boolean isUnlocked(UUID playerId, String dropItemId)
    {
        return isUnlocked(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);
    }

    default boolean unlock(UUID playerId, String dropItemId)
    {
        return unlock(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);
    }

    default boolean lock(UUID playerId, String dropItemId)
    {
        return lock(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);
    }
}
