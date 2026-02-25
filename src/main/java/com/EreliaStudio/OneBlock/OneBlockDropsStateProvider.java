package com.EreliaStudio.OneBlock;

import java.util.List;
import java.util.UUID;

public interface OneBlockDropsStateProvider
{
    List<String> getEnabledDrops(UUID playerId);

    boolean isUnlocked(UUID playerId, String dropItemId);

    boolean unlock(UUID playerId, String dropItemId);

    boolean lock(UUID playerId, String dropItemId);

    boolean setEnabled(UUID playerId, String dropItemId, boolean enabled);
}