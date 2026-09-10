package com.EreliaStudio.OneBlock;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.EreliaStudio.OneBlock.ExpeditionCatalog.*;

class ExpeditionCatalogTest {
    private static Expedition e(String id, Edge... children) {
        return new Expedition(id, id, "OneBlock_Crystal_" + id, "Easy", 1, 25, "Hand", false,
                List.of(new Loot("Ore", 2, 40, 20, "block")), List.of(), List.of(), List.of(children));
    }
    private final ExpeditionCatalog catalog = new ExpeditionCatalog(List.of(
            e("A", new Edge("B", 50), new Edge("C", 50)), e("B", new Edge("D", 100)), e("C"), e("D")), Map.of());

    @Test void frontierExcludesLockedAndEndpoints() {
        var known = Set.of("A", "C");
        assertEquals(List.of("A"), catalog.frontier(e -> known.contains(e.id()), Expedition::name).stream().map(Expedition::id).toList());
    }
    @Test void partiallyDiscoveredBranchRemainsVisible() {
        assertEquals(1, catalog.discovered(catalog.get("A"), e -> e.id().equals("B")));
        assertEquals(List.of("B", "A"), catalog.frontier(e -> Set.of("A", "B").contains(e.id()), Expedition::name).stream().map(Expedition::id).toList());
    }
    @Test void fullyDiscoveredBranchesAndEndpointsAreExcluded() {
        assertTrue(catalog.frontier(e -> true, Expedition::name).isEmpty());
        assertEquals(List.of("B"), catalog.frontier(e -> !e.id().equals("D"), Expedition::name).stream().map(Expedition::id).toList());
    }
    @Test void incomingAndOutgoingRoutesRetainProbability() {
        assertEquals(List.of(new Edge("A", 50)), catalog.parents("B"));
        assertEquals(List.of(new Edge("B", 50), new Edge("C", 50)), catalog.get("A").children());
    }
    @Test void sourcePriorityPrefersAccessibleOverHigherYield() {
        var sorted = catalog.sources("Ore").stream().sorted(sourceOrder(e -> !e.id().equals("D"), e -> e.id().equals("B"), Expedition::name)).toList();
        assertEquals(List.of("B", "A", "C", "D"), sorted.stream().map(s -> s.expedition().id()).toList());
    }
    @Test void realCatalogueMatchesNativeWeightsAndExpectedYield() {
        var actual = ExpeditionCatalog.load();
        assertEquals(124, actual.all().size());
        var weights = OneBlockExpeditionDefaults.getDefaultWeights();
        for (var e : actual.all()) {
            if (e.dungeon()) continue;
            var pool = weights.get(e.id());
            double total = pool.values().stream().mapToInt(Integer::intValue).sum();
            assertEquals(e.drops().size(), pool.size());
            assertEquals(100, e.drops().stream().mapToDouble(Loot::chance).sum(), 0.000001);
            for (var loot : e.drops()) {
                assertEquals(pool.get(loot.id()) * 100 / total, loot.chance(), 0.000001, e.id());
                assertEquals(e.duration() * loot.quantity() * loot.chance() / 100, loot.expected(), 0.000001);
                assertEquals(loot.quantity(), actual.dropQuantity(e.id(), loot.id()));
            }
        }
    }
    @Test void reverseLookupIncludesItemsMobsWavesAndRewards() {
        var actual = ExpeditionCatalog.load();
        assertFalse(actual.sources("ExpeditionPoint").isEmpty());
        assertTrue(actual.sources("entity:Crawler_Void").stream().anyMatch(s -> s.loot().kind().equals("wave")));
        assertTrue(actual.sources("entity:Crawler_Void").stream().anyMatch(s -> s.loot().kind().equals("block")));
        for (String id : actual.lootIds()) if (id.startsWith("entity:")) assertNotNull(actual.mob(id), id);
    }
    @Test void unknownIdsAndCyclesAreRejected() {
        assertNull(catalog.get("../../Other"));
        assertThrows(IllegalArgumentException.class, () -> new ExpeditionCatalog(List.of(e("A", new Edge("missing", 100))), Map.of()));
        assertThrows(IllegalArgumentException.class, () -> new ExpeditionCatalog(List.of(e("A", new Edge("A", 100))), Map.of()));
    }
    @Test void searchNormalizesLocalizedAccents() {
        assertEquals("expedition", ExpeditionAtlasPage.fold("EXPÉDITION"));
        assertEquals("kristal", ExpeditionAtlasPage.fold("Krištáľ"));
    }
}
