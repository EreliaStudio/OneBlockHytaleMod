package com.EreliaStudio.OneBlockIslands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.nio.file.Files;
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

    @Test void ownerCannotLeave() throws Exception {
        UUID owner = UUID.randomUUID();
        IslandStore store = new IslandStore(temp.resolve("islands.json"));
        store.create(owner);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> store.leave(owner));
        assertTrue(error.getMessage().contains("owner cannot leave"));
        assertTrue(store.findByOwner(owner).isPresent());
    }

    @Test void playerCanJoinMultipleIslands() throws Exception {
        UUID firstOwner = UUID.randomUUID();
        UUID secondOwner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        IslandStore store = new IslandStore(temp.resolve("islands.json"));
        IslandRecord accepted = store.create(firstOwner);
        IslandRecord other = store.create(secondOwner);
        store.invite(firstOwner, member);
        store.invite(secondOwner, member);

        assertSame(accepted, store.accept(member));
        assertTrue(other.pendingInvites().contains(member));
        assertSame(other, store.accept(member));
        assertTrue(accepted.canEdit(member));
        assertTrue(other.canEdit(member));
    }

    @Test void failedInitialSaveDoesNotKeepReservationInMemory() throws Exception {
        Path parentFile = temp.resolve("not-a-directory");
        Files.writeString(parentFile, "block directory creation");
        UUID owner = UUID.randomUUID();
        IslandStore store = new IslandStore(parentFile.resolve("islands.json"));

        assertThrows(java.io.IOException.class, () -> store.create(owner));
        assertTrue(store.findByOwner(owner).isEmpty());
    }

    @Test void deletingMetadataReturnsDeletedMembership() throws Exception {
        UUID owner = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        IslandStore store = new IslandStore(temp.resolve("islands.json"));
        IslandRecord island = store.create(owner);
        store.adminAdd(owner, member);

        assertSame(island, store.deleteMetadata(owner));
        assertTrue(store.findByPlayer(owner).isEmpty());
        assertTrue(store.findByPlayer(member).isEmpty());
    }

    @Test void ownerCanCreateMultipleUniquelyNamedIslands() throws Exception {
        UUID owner = UUID.randomUUID();
        IslandStore store = new IslandStore(temp.resolve("islands.json"));

        IslandRecord home = store.create(owner);
        IslandRecord friends = store.create(owner, "Friends");

        assertEquals("Home", home.name());
        assertEquals("Friends", friends.name());
        assertNotEquals(home.worldName(), friends.worldName());
        assertSame(friends, store.findOwned(owner, "friends").orElseThrow());
        assertThrows(IllegalStateException.class, () -> store.create(owner, "FRIENDS"));
    }

    @Test void legacyIslandWithoutANameMigratesToHome() throws Exception {
        UUID owner = UUID.randomUUID();
        Path database = temp.resolve("islands.json");
        Files.writeString(database, """
                {"version":1,"islands":[{"id":"legacy","worldName":"ob_legacy","ownerUuid":"%s"}]}
                """.formatted(owner));

        IslandStore store = new IslandStore(database);
        store.load();

        assertEquals("Home", store.findHome(owner).orElseThrow().name());
    }
}
