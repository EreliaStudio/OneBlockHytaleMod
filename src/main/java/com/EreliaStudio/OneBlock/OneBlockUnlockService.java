package com.EreliaStudio.OneBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OneBlockUnlockService
{
    private final OneBlockDropsStateProvider stateProvider;
    private final Map<String, UnlockDefinition> definitionByConsumableItemId = new HashMap<>();

    public OneBlockUnlockService(OneBlockDropsStateProvider stateProvider, Map<String, UnlockDefinition> consumableToDropMap)
    {
        this.stateProvider = stateProvider;

        if (consumableToDropMap == null || consumableToDropMap.isEmpty())
        {
            return;
        }

        definitionByConsumableItemId.putAll(consumableToDropMap);
    }

    public UnlockConsumeResult consume(UUID playerId, String consumableItemId)
    {
        if (playerId == null || consumableItemId == null || consumableItemId.isEmpty())
        {
            return UnlockConsumeResult.INVALID_ITEM;
        }

        UnlockDefinition definition = definitionByConsumableItemId.get(consumableItemId);
        if (definition == null || definition.dropItemId == null || definition.dropItemId.isEmpty())
        {
            return UnlockConsumeResult.INVALID_ITEM;
        }

        String chapterId = (definition.chapterId == null || definition.chapterId.isEmpty())
                ? OneBlockChapterResolver.DEFAULT_CHAPTER
                : definition.chapterId;

        if (stateProvider.isUnlocked(playerId, chapterId, definition.dropItemId))
        {
            return UnlockConsumeResult.ALREADY_UNLOCKED;
        }

        boolean unlocked = stateProvider.unlock(playerId, chapterId, definition.dropItemId);
        return unlocked ? UnlockConsumeResult.UNLOCKED : UnlockConsumeResult.UNLOCK_FAILED;
    }

    public String getDropItemIdForConsumable(String consumableItemId)
    {
        UnlockDefinition definition = definitionByConsumableItemId.get(consumableItemId);
        return definition == null ? null : definition.dropItemId;
    }

    public String getChapterForConsumable(String consumableItemId)
    {
        UnlockDefinition definition = definitionByConsumableItemId.get(consumableItemId);
        return definition == null ? null : definition.chapterId;
    }

    public enum UnlockConsumeResult
    {
        INVALID_ITEM,
        ALREADY_UNLOCKED,
        UNLOCKED,
        UNLOCK_FAILED
    }

    public static final class UnlockDefinition
    {
        public final String chapterId;
        public final String dropItemId;
        public final int weight;

        public UnlockDefinition(String chapterId, String dropItemId, int weight)
        {
            this.chapterId = chapterId;
            this.dropItemId = dropItemId;
            this.weight = weight;
        }
    }
}
