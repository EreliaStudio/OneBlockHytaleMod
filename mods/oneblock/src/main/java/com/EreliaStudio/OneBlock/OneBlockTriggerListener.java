package com.EreliaStudio.OneBlock;

import java.util.UUID;

/** Receives the same node-scoped update delivered to a player's HUD. */
@FunctionalInterface
public interface OneBlockTriggerListener
{
    void onOneBlockTrigger(UUID playerId, OneBlockTrigger trigger);
}
