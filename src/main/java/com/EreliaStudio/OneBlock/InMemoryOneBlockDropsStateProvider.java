package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class InMemoryOneBlockDropsStateProvider implements OneBlockDropsStateProvider
{
    private final Map<UUID, OneBlockPlayerDropsState> stateByPlayer = new HashMap<>();

    private OneBlockPlayerDropsState state(UUID playerId)
    {
        if (playerId == null)
        {
            return null;
        }

        return stateByPlayer.computeIfAbsent(playerId, id ->
        {
            OneBlockPlayerDropsState s = new OneBlockPlayerDropsState();
            s.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
            s.enabledDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
            return s;
        });
    }

    @Override
    public List<String> getEnabledDrops(UUID playerId)
    {
        OneBlockPlayerDropsState s = state(playerId);
        if (s == null || s.enabledDrops.isEmpty())
        {
            List<String> fallback = new ArrayList<>();
            fallback.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
            return fallback;
        }

        return new ArrayList<>(s.enabledDrops);
    }

    @Override
    public boolean isUnlocked(UUID playerId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        return s != null && s.unlockedDrops.contains(dropItemId);
    }

    @Override
    public boolean unlock(UUID playerId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        if (s == null)
        {
            return false;
        }

        if (s.unlockedDrops.contains(dropItemId))
        {
            return false;
        }

        s.unlockedDrops.add(dropItemId);

        // default behavior: enable immediately when unlocked
        s.enabledDrops.add(dropItemId);

        return true;
    }

    @Override
    public boolean lock(UUID playerId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        // Never allow removing the default
        if (OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId))
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        if (s == null)
        {
            return false;
        }

        boolean removed = s.unlockedDrops.remove(dropItemId);
        s.enabledDrops.remove(dropItemId);

        if (s.enabledDrops.isEmpty())
        {
            s.enabledDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }

        if (s.unlockedDrops.isEmpty())
        {
            s.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }

        return removed;
    }

    @Override
    public boolean setEnabled(UUID playerId, String dropItemId, boolean enabled)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        if (s == null)
        {
            return false;
        }

        // only unlocked items can be enabled
        if (!s.unlockedDrops.contains(dropItemId))
        {
            return false;
        }

        if (enabled)
        {
            s.enabledDrops.add(dropItemId);
        }
        else
        {
            s.enabledDrops.remove(dropItemId);

            // never allow "no enabled drops"
            if (s.enabledDrops.isEmpty())
            {
                s.enabledDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
            }
        }

        return true;
    }
}