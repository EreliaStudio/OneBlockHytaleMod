package com.EreliaStudio.OneBlock;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OneBlockCrystalOwnershipTest
{
    @TempDir
    Path dataDirectory;

    @Test
    void guestCrystalStartsGuestRootInsteadOfIslandOwnerRoot()
    {
        String islandWorld = "island-owned-by-a";
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        OneBlockRootRegistry roots = new OneBlockRootRegistry(dataDirectory);
        roots.register(islandWorld, new Vector3i(0, 100, 0), playerA, "A");
        roots.register(islandWorld, new Vector3i(10, 100, 0), playerB, "B");

        OneBlockRootRegistry.RootEntry selected = roots.findOwnedRoot(islandWorld, playerB, "B");

        assertEquals(playerB, selected.ownerId());
        roots.expeditionState(islandWorld, selected.ownerId()).startExpedition("Cave", 25);
        assertFalse(roots.expeditionState(islandWorld, playerA).hasActiveExpedition());
        assertTrue(roots.expeditionState(islandWorld, playerB).hasActiveExpedition());
        assertEquals("Cave", roots.expeditionState(islandWorld, playerB).getActiveExpeditionId());
    }

    @Test
    void guestWithoutAnOwnedRootCannotFallBackToIslandOwnersRoot()
    {
        String islandWorld = "island-owned-by-a";
        UUID playerA = UUID.randomUUID();
        UUID playerB = UUID.randomUUID();
        OneBlockRootRegistry roots = new OneBlockRootRegistry(dataDirectory);
        roots.register(islandWorld, new Vector3i(0, 100, 0), playerA, "A");

        assertNull(roots.findOwnedRoot(islandWorld, playerB, "B"));
    }
}
