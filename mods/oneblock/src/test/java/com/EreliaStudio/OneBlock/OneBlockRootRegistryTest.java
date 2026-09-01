package com.EreliaStudio.OneBlock;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OneBlockRootRegistryTest
{
    @TempDir
    Path dataDirectory;

    @Test
    void multipleRootsResolveToTheSameOwnerAndPersist()
    {
        UUID bob = UUID.randomUUID();
        Vector3i first = new Vector3i(10, 80, -4);
        Vector3i second = new Vector3i(30, 91, 12);

        OneBlockRootRegistry registry = new OneBlockRootRegistry(dataDirectory);
        registry.register("shared", first, bob, "Bob");
        registry.register("shared", second, bob, "Bob");

        assertEquals(bob, registry.find("shared", first).ownerId());
        assertEquals(bob, registry.find("shared", second).ownerId());
        assertEquals(2, registry.positions("shared", bob).size());

        OneBlockRootRegistry reloaded = new OneBlockRootRegistry(dataDirectory);
        assertEquals("Bob", reloaded.find("shared", first).ownerName());
        assertEquals(2, reloaded.positions("shared", bob).size());
    }

    @Test
    void removingOneRootKeepsOtherRoots()
    {
        UUID bob = UUID.randomUUID();
        Vector3i first = new Vector3i(1, 2, 3);
        Vector3i second = new Vector3i(4, 5, 6);

        OneBlockRootRegistry registry = new OneBlockRootRegistry(dataDirectory);
        registry.register("shared", first, bob, "Bob");
        registry.register("shared", second, bob, "Bob");

        assertTrue(registry.remove("shared", first));
        assertFalse(registry.remove("shared", first));
        assertNull(registry.find("shared", first));
        assertEquals(bob, registry.find("shared", second).ownerId());
    }

    @Test
    void ownersInTheSameWorldRemainIndependent()
    {
        UUID alice = UUID.randomUUID();
        UUID bob = UUID.randomUUID();

        OneBlockRootRegistry registry = new OneBlockRootRegistry(dataDirectory);
        registry.register("shared", new Vector3i(0, 70, 0), alice, "Alice");
        registry.register("shared", new Vector3i(20, 70, 0), bob, "Bob");

        assertEquals(1, registry.positions("shared", alice).size());
        assertEquals(1, registry.positions("shared", bob).size());
        assertFalse(registry.positions("other-world", alice).size() > 0);
    }

    @Test
    void migratesLegacyWorldProgressToTheFirstRegisteredOwner()
    {
        String worldName = "ob_existing";
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(worldName.getBytes(StandardCharsets.UTF_8));
        Path legacy = dataDirectory.resolve("worlds").resolve(encoded);
        OneBlockExpeditionStateProvider oldState =
                new OneBlockExpeditionStateProvider(legacy.resolve("expedition.json"));
        oldState.startExpedition("Meadow", 42);

        UUID owner = UUID.randomUUID();
        OneBlockRootRegistry registry = new OneBlockRootRegistry(dataDirectory);
        registry.register(worldName, new Vector3i(0, 100, 0), owner, "Owner");

        OneBlockExpeditionStateProvider migrated = new OneBlockExpeditionStateProvider(
                dataDirectory.resolve("root-worlds").resolve(encoded).resolve("owners")
                        .resolve(owner.toString()).resolve("expedition.json")
        );
        assertEquals("Meadow", migrated.getActiveExpeditionId());
        assertEquals(42, migrated.getTicksRemaining());
    }
}
