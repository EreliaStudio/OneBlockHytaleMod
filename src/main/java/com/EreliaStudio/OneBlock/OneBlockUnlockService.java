package com.EreliaStudio.OneBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OneBlockUnlockService
{
    private final OneBlockDropsStateProvider stateProvider;
    private final Map<String, String> dropByConsumableItemId = new HashMap<>();

    public OneBlockUnlockService(OneBlockDropsStateProvider stateProvider, Map<String, String> consumableToDropMap)
    {
        this.stateProvider = stateProvider;

        if (consumableToDropMap == null || consumableToDropMap.isEmpty())
        {
            return;
        }

        dropByConsumableItemId.putAll(consumableToDropMap);
    }

    public UnlockConsumeResult consume(UUID playerId, String consumableItemId)
    {
        if (playerId == null || consumableItemId == null || consumableItemId.isEmpty())
        {
            return UnlockConsumeResult.INVALID_ITEM;
        }

        String dropItemId = dropByConsumableItemId.get(consumableItemId);
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return UnlockConsumeResult.INVALID_ITEM;
        }

        if (stateProvider.isUnlocked(playerId, dropItemId))
        {
            return UnlockConsumeResult.ALREADY_UNLOCKED;
        }

        boolean unlocked = stateProvider.unlock(playerId, dropItemId);
        return unlocked ? UnlockConsumeResult.UNLOCKED : UnlockConsumeResult.UNLOCK_FAILED;
    }

    public String getDropItemIdForConsumable(String consumableItemId)
    {
        return dropByConsumableItemId.get(consumableItemId);
    }

    public enum UnlockConsumeResult
    {
        INVALID_ITEM,
        ALREADY_UNLOCKED,
        UNLOCKED,
        UNLOCK_FAILED
    }
}
