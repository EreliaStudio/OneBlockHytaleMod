package com.EreliaStudio.OneBlockIslands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IslandStoreTest {
    @TempDir Path temp;

    @Test void persistsUuidOwnershipAndMembershipWithoutDuplicates() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        IslandStore store = new IslandStore(temp.resolve("islands.json"));
        IslandRecord first = store.create(owner);
        assertSame(first, store.create(owner));
        store.invite(owner, member);
        assertEquals(first.worldName(), store.accept(member).worldName());

        IslandStore reloaded = new IslandStore(temp.resolve("islands.json"));
        reloaded.load();
        assertEquals(owner, reloaded.findByPlayer(member).orElseThrow().ownerUuid());
        assertTrue(reloaded.findByOwner(owner).orElseThrow().canEdit(member));
    }

    @Test void kickedMemberLosesAuthorization() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        IslandStore store = new IslandStore(temp.resolve("islands.json"));
        IslandRecord island = store.create(owner);
        store.adminAdd(owner, member);
        assertTrue(island.canEnter(member));
        store.kick(owner, member);
        assertFalse(island.canEnter(member));
    }
}
