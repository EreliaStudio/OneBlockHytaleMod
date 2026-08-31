package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.universe.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OneBlockWorldStateRegistryTest
{
    @TempDir
    Path dataDirectory;

    @Test
    void defaultWorldIsNotManagedOnFirstRun()
    {
        OneBlockWorldStateRegistry registry = new OneBlockWorldStateRegistry(dataDirectory);

        assertFalse(registry.isManaged(World.DEFAULT));
        assertTrue(registry.getManagedWorldNames().isEmpty());
    }

    @Test
    void legacyDefaultEntryIsRemovedWithoutRemovingExplicitWorlds() throws Exception
    {
        Files.writeString(
                dataDirectory.resolve("oneblock-worlds.json"),
                "{\"worlds\":[\"default\",\"oneblock-party\"]}"
        );

        OneBlockWorldStateRegistry registry = new OneBlockWorldStateRegistry(dataDirectory);

        assertFalse(registry.isManaged(World.DEFAULT));
        assertTrue(registry.isManaged("oneblock-party"));
    }

    @Test
    void defaultWorldCannotBeRegisteredExplicitly()
    {
        OneBlockWorldStateRegistry registry = new OneBlockWorldStateRegistry(dataDirectory);

        assertThrows(IllegalArgumentException.class, () -> registry.registerWorld(World.DEFAULT));
    }
}
