package com.EreliaStudio.OneBlock;

import java.util.List;
import java.util.UUID;

public final class InMemoryOneBlockDropsStateProvider extends AbstractOneBlockDropsStateProvider
{
    @Override
    protected void onStateChanged()
    {
        // No persistence needed.
    }

    @Override
    public List<String> getUnlockedDrops(UUID playerId, String expeditionId)
    {
        return getUnlockedDropsInternal(playerId, expeditionId);
    }

    @Override
    public boolean isUnlocked(UUID playerId, String expeditionId, String dropItemId)
    {
        return isUnlockedInternal(playerId, expeditionId, dropItemId);
    }

    @Override
    public boolean unlock(UUID playerId, String expeditionId, String dropItemId)
    {
        return unlockInternal(playerId, expeditionId, dropItemId);
    }

    @Override
    public boolean lock(UUID playerId, String expeditionId, String dropItemId)
    {
        return lockInternal(playerId, expeditionId, dropItemId);
    }
}
