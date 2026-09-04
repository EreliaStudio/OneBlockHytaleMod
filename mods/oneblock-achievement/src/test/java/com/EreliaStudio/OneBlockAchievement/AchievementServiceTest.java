package com.EreliaStudio.OneBlockAchievement;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AchievementServiceTest {
    @TempDir Path temp;

    @Test void partialItemsPersistAndUnlockSelectableTitle() throws Exception {
        Path catalogPath = writeCatalog();
        Path progressPath = temp.resolve("players.json");
        AchievementCatalog catalog = new AchievementCatalog(catalogPath);
        catalog.load();
        PlayerProgressStore store = new PlayerProgressStore(progressPath);
        store.load();
        AchievementService service = new AchievementService(catalog, store);
        UUID player = UUID.randomUUID();
        AchievementDefinition beginner = service.achievement("beginner");
        AchievementDefinition expert = service.achievement("expert");

        assertEquals(AchievementService.State.CURRENTLY_UNLOCKING, service.state(player, beginner));
        assertEquals(AchievementService.State.INACCESSIBLE, service.state(player, expert));

        service.contribute(player, beginner.id, 0, Map.of("Rock_Stone", 32));
        assertEquals(32, service.remainingItem(player, beginner, beginner.cost.items.getFirst()));
        assertFalse(service.tryUnlock(player, beginner));

        PlayerProgressStore reloadedStore = new PlayerProgressStore(progressPath);
        reloadedStore.load();
        AchievementService reloaded = new AchievementService(catalog, reloadedStore);
        reloaded.contribute(player, beginner.id, 0, Map.of("Rock_Stone", 32));
        assertTrue(reloaded.tryUnlock(player, beginner));
        assertEquals(AchievementService.State.UNLOCKED, reloaded.state(player, beginner));
        assertEquals(AchievementService.State.CURRENTLY_UNLOCKING, reloaded.state(player, expert));
        assertEquals(1, reloaded.level(player));
        reloaded.selectTitle(player, beginner.id);
        assertEquals("Beginner Miner", reloaded.activeTitle(player));
    }

    @Test void prerequisiteCurrencyAndKnowledgeAreAllRequired() throws Exception {
        AchievementCatalog catalog = new AchievementCatalog(writeCatalog());
        catalog.load();
        PlayerProgressStore store = new PlayerProgressStore(temp.resolve("progress-two.json"));
        AchievementService service = new AchievementService(catalog, store);
        UUID player = UUID.randomUUID();
        AchievementDefinition beginner = service.achievement("beginner");
        AchievementDefinition expert = service.achievement("expert");

        service.contribute(player, beginner.id, 0, Map.of("Rock_Stone", 64));
        assertTrue(service.tryUnlock(player, beginner));
        service.contribute(player, expert.id, 100, Map.of());
        assertFalse(service.tryUnlock(player, expert));
        service.learnExpedition(player, "CopperCave");
        assertTrue(service.unlocked(player, expert.id));
        assertEquals(2, service.level(player));
    }

    @Test void catalogRejectsPrerequisiteCycles() throws Exception {
        Path path = temp.resolve("cycle.json");
        Files.writeString(path, """
                {"schemaVersion":1,"achievements":[
                  {"id":"a","name":"A","title":"A","prerequisites":["b"],"cost":{}},
                  {"id":"b","name":"B","title":"B","prerequisites":["a"],"cost":{}}
                ]}
                """);
        AchievementCatalog catalog = new AchievementCatalog(path);
        assertThrows(java.io.IOException.class, catalog::load);
    }

    @Test void authoritativeBundledCatalogReplacesAnExistingRuntimeCopy() throws Exception {
        Path path = writeCatalog();
        AchievementCatalog catalog = new AchievementCatalog(path, true);

        catalog.load();

        assertNotNull(catalog.get("first_steps"));
        assertNull(catalog.get("beginner"));
    }

    @Test void creativeUnlockBypassesCostsButKeepsThemUnconsumed() throws Exception {
        AchievementCatalog catalog = new AchievementCatalog(writeCatalog());
        catalog.load();
        PlayerProgressStore store = new PlayerProgressStore(temp.resolve("creative-progress.json"));
        AchievementService service = new AchievementService(catalog, store);
        UUID player = UUID.randomUUID();
        AchievementDefinition beginner = service.achievement("beginner");
        AchievementDefinition expert = service.achievement("expert");

        assertTrue(service.unlockWithoutCosts(player, beginner));
        assertTrue(service.prerequisitesMet(player, expert));
        assertFalse(service.knowledgeMet(player, expert));
        assertEquals(100, service.remainingCurrency(player, expert));

        assertTrue(service.unlockWithoutCosts(player, expert));

        assertTrue(service.unlocked(player, expert.id));
        assertEquals(100, service.remainingCurrency(player, expert));
        assertTrue(service.contributedItems(player, expert.id).isEmpty());
    }

    private Path writeCatalog() throws Exception {
        Path path = temp.resolve("achievements.json");
        Files.writeString(path, """
                {"schemaVersion":1,"achievements":[
                  {"id":"beginner","name":"Beginner Miner","title":"Beginner Miner","prerequisites":[],
                   "cost":{"currency":0,"items":[{"id":"Rock_Stone","quantity":64}],"expeditions":[]}},
                  {"id":"expert","name":"Expert Miner","title":"Expert Miner","prerequisites":["beginner"],
                   "cost":{"currency":100,"items":[],"expeditions":["CopperCave"]}}
                ]}
                """);
        return path;
    }
}
