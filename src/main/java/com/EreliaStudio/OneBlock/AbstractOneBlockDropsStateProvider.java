package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

abstract class AbstractOneBlockDropsStateProvider implements OneBlockDropsStateProvider
{
    protected final Map<UUID, OneBlockPlayerDropsState> stateByPlayer = new HashMap<>();

    protected abstract void onStateChanged();

    protected OneBlockPlayerExpeditionDropsState state(UUID playerId, String expeditionId)
    {
        if (playerId == null)
        {
            return null;
        }

        String expeditionKey = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);
        OneBlockPlayerDropsState playerState = stateByPlayer.computeIfAbsent(playerId, id -> new OneBlockPlayerDropsState());
        return playerState.expeditions.computeIfAbsent(expeditionKey, key ->
        {
            OneBlockPlayerExpeditionDropsState s = new OneBlockPlayerExpeditionDropsState();
            OneBlockExpeditionDefaults.ensureDefaults(expeditionKey, s);
            onStateChanged();
            return s;
        });
    }

    protected List<String> getUnlockedDropsInternal(UUID playerId, String expeditionId)
    {
        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        if (s == null || s.unlockedDrops.isEmpty())
        {
            return new ArrayList<>(OneBlockExpeditionDefaults.getDefaultDropIds(expeditionId));
        }

        return new ArrayList<>(s.unlockedDrops);
    }

    protected boolean isUnlockedInternal(UUID playerId, String expeditionId, String dropItemId)
    {
        if (isBlank(dropItemId))
        {
            return false;
        }

        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        return s != null && s.unlockedDrops.contains(dropItemId);
    }

    protected boolean unlockInternal(UUID playerId, String expeditionId, String dropItemId)
    {
        if (isBlank(dropItemId))
        {
            return false;
        }

        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        if (s == null || s.unlockedDrops.contains(dropItemId))
        {
            return false;
        }

        s.unlockedDrops.add(dropItemId);
        onStateChanged();
        return true;
    }

    protected boolean lockInternal(UUID playerId, String expeditionId, String dropItemId)
    {
        if (isBlank(dropItemId))
        {
            return false;
        }

        if (OneBlockExpeditionDefaults.isDefaultDrop(expeditionId, dropItemId))
        {
            return false;
        }

        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        if (s == null)
        {
            return false;
        }

        boolean removed = s.unlockedDrops.remove(dropItemId);
        OneBlockExpeditionDefaults.ensureDefaults(expeditionId, s);

        if (removed)
        {
            onStateChanged();
        }

        return removed;
    }

    protected static boolean isBlank(String value)
    {
        return value == null || value.isEmpty();
    }
}
