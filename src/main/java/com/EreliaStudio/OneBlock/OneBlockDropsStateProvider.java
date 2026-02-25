package com.EreliaStudio.OneBlock;

import java.util.List;
import java.util.UUID;

public interface OneBlockDropsStateProvider
{
    List<String> getEnabledDrops(UUID playerId, String chapterId);

    boolean isUnlocked(UUID playerId, String chapterId, String dropItemId);

    boolean unlock(UUID playerId, String chapterId, String dropItemId);

    boolean lock(UUID playerId, String chapterId, String dropItemId);

    boolean setEnabled(UUID playerId, String chapterId, String dropItemId, boolean enabled);

    default List<String> getEnabledDrops(UUID playerId)
    {
        return getEnabledDrops(playerId, OneBlockChapterResolver.DEFAULT_CHAPTER);
    }

    default boolean isUnlocked(UUID playerId, String dropItemId)
    {
        return isUnlocked(playerId, OneBlockChapterResolver.DEFAULT_CHAPTER, dropItemId);
    }

    default boolean unlock(UUID playerId, String dropItemId)
    {
        return unlock(playerId, OneBlockChapterResolver.DEFAULT_CHAPTER, dropItemId);
    }

    default boolean lock(UUID playerId, String dropItemId)
    {
        return lock(playerId, OneBlockChapterResolver.DEFAULT_CHAPTER, dropItemId);
    }

    default boolean setEnabled(UUID playerId, String dropItemId, boolean enabled)
    {
        return setEnabled(playerId, OneBlockChapterResolver.DEFAULT_CHAPTER, dropItemId, enabled);
    }
}
