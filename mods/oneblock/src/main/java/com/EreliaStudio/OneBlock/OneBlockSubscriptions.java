package com.EreliaStudio.OneBlock;

import org.joml.Vector3i;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Tracks the single physical OneBlock node currently observed by each player. */
final class OneBlockSubscriptions
{
    private final ConcurrentHashMap<UUID, NodeKey> nodeByPlayer = new ConcurrentHashMap<>();

    void listen(UUID playerId, String worldName, Vector3i position)
    {
        if (playerId == null || worldName == null || position == null) return;
        nodeByPlayer.put(playerId, NodeKey.of(worldName, position));
    }

    boolean isListening(UUID playerId, String worldName, Vector3i position)
    {
        return playerId != null && NodeKey.of(worldName, position).equals(nodeByPlayer.get(playerId));
    }

    void stop(UUID playerId)
    {
        if (playerId != null) nodeByPlayer.remove(playerId);
    }

    void forget(String worldName, Vector3i position)
    {
        NodeKey removed = NodeKey.of(worldName, position);
        nodeByPlayer.entrySet().removeIf(entry -> removed.equals(entry.getValue()));
    }

    private record NodeKey(String worldName, int x, int y, int z)
    {
        private static NodeKey of(String worldName, Vector3i position)
        {
            return new NodeKey(worldName, position.x(), position.y(), position.z());
        }
    }
}
