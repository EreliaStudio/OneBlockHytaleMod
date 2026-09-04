package com.EreliaStudio.OneBlock;

import org.joml.Vector3i;

import java.util.UUID;

/**
 * Immutable HUD event emitted for one physical OneBlock node.
 * Progress is the completed fraction, clamped to the range {@code 0..1}.
 */
public record OneBlockTrigger(String worldName,
                              Vector3i nodePosition,
                              UUID ownerId,
                              String expeditionId,
                              String name,
                              float progress,
                              boolean active)
{
    public OneBlockTrigger
    {
        if (worldName == null || worldName.isBlank())
            throw new IllegalArgumentException("World name cannot be blank");
        if (nodePosition == null) throw new IllegalArgumentException("Node position cannot be null");
        if (ownerId == null) throw new IllegalArgumentException("Owner UUID cannot be null");
        nodePosition = new Vector3i(nodePosition);
        expeditionId = expeditionId == null ? "" : expeditionId;
        name = name == null ? "" : name;
        progress = Math.max(0.0f, Math.min(1.0f, progress));
    }

    @Override
    public Vector3i nodePosition()
    {
        return new Vector3i(nodePosition);
    }

    public static OneBlockTrigger expedition(String worldName,
                                              Vector3i position,
                                              UUID ownerId,
                                              String expeditionId,
                                              int ticksRemaining,
                                              int totalTicks,
                                              boolean active)
    {
        float progress = totalTicks <= 0
                ? 0.0f
                : 1.0f - ((float) ticksRemaining / (float) totalTicks);
        return new OneBlockTrigger(
                worldName,
                position,
                ownerId,
                expeditionId,
                OneBlockDisplayNames.get(expeditionId),
                progress,
                active
        );
    }

    public static OneBlockTrigger dungeon(String worldName,
                                           Vector3i position,
                                           UUID ownerId,
                                           String dungeonId,
                                           int completedWaves,
                                           int totalWaves,
                                           boolean active)
    {
        float progress = totalWaves <= 0 ? 0.0f : (float) completedWaves / (float) totalWaves;
        return new OneBlockTrigger(
                worldName,
                position,
                ownerId,
                dungeonId,
                OneBlockDisplayNames.get(dungeonId),
                progress,
                active
        );
    }
}
