package com.EreliaStudio.OneBlockAchievement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class AchievementService {
    enum State {
        INACCESSIBLE,
        CURRENTLY_UNLOCKING,
        UNLOCKED
    }

    private final AchievementCatalog catalog;
    private final PlayerProgressStore store;

    AchievementService(AchievementCatalog catalog, PlayerProgressStore store) {
        this.catalog = catalog;
        this.store = store;
    }

    synchronized List<AchievementDefinition> achievements() { return catalog.all(); }
    synchronized AchievementDefinition achievement(String id) { return catalog.get(id); }

    synchronized boolean prerequisitesMet(UUID playerId, AchievementDefinition achievement) {
        return store.player(playerId).unlocked.containsAll(achievement.prerequisites);
    }

    synchronized boolean knowledgeMet(UUID playerId, AchievementDefinition achievement) {
        return store.player(playerId).knownExpeditions.containsAll(achievement.cost.expeditions);
    }

    synchronized boolean knowsExpedition(UUID playerId, String expeditionId) {
        return store.player(playerId).knownExpeditions.contains(expeditionId);
    }

    synchronized boolean unlocked(UUID playerId, String achievementId) {
        return store.player(playerId).unlocked.contains(achievementId);
    }

    synchronized State state(UUID playerId, AchievementDefinition achievement) {
        if (unlocked(playerId, achievement.id)) return State.UNLOCKED;
        return prerequisitesMet(playerId, achievement) ? State.CURRENTLY_UNLOCKING : State.INACCESSIBLE;
    }

    synchronized int level(UUID playerId) { return store.player(playerId).unlocked.size(); }

    synchronized long remainingCurrency(UUID playerId, AchievementDefinition achievement) {
        PlayerProgressStore.AchievementProgress progress = progress(playerId, achievement.id);
        return Math.max(0, achievement.cost.currency - progress.currency);
    }

    synchronized int remainingItem(UUID playerId, AchievementDefinition achievement, AchievementDefinition.ItemCost item) {
        PlayerProgressStore.AchievementProgress progress = progress(playerId, achievement.id);
        return Math.max(0, item.quantity - progress.items.getOrDefault(item.id, 0));
    }

    synchronized void contribute(UUID playerId, String achievementId, long currency, Map<String, Integer> items) {
        if (currency < 0) throw new IllegalArgumentException("Currency contribution cannot be negative");
        store.mutate(playerId, player -> {
            PlayerProgressStore.AchievementProgress progress = player.progress.computeIfAbsent(achievementId, ignored -> new PlayerProgressStore.AchievementProgress());
            progress.normalize();
            progress.currency += currency;
            for (Map.Entry<String, Integer> entry : items.entrySet())
                progress.items.merge(entry.getKey(), entry.getValue(), Integer::sum);
        });
    }

    synchronized boolean tryUnlock(UUID playerId, AchievementDefinition achievement) {
        if (unlocked(playerId, achievement.id)) return false;
        if (!prerequisitesMet(playerId, achievement) || !knowledgeMet(playerId, achievement)) return false;
        if (remainingCurrency(playerId, achievement) > 0) return false;
        for (AchievementDefinition.ItemCost item : achievement.cost.items)
            if (remainingItem(playerId, achievement, item) > 0) return false;
        store.mutate(playerId, player -> player.unlocked.add(achievement.id));
        return true;
    }

    synchronized void refreshUnlocks(UUID playerId) {
        boolean changed;
        do {
            changed = false;
            for (AchievementDefinition achievement : catalog.all()) changed |= tryUnlock(playerId, achievement);
        } while (changed);
    }

    synchronized void learnExpedition(UUID playerId, String expeditionId) {
        store.mutate(playerId, player -> player.knownExpeditions.add(expeditionId));
        for (AchievementDefinition achievement : catalog.all()) tryUnlock(playerId, achievement);
    }

    synchronized String activeTitle(UUID playerId) {
        String id = store.player(playerId).activeTitle;
        AchievementDefinition achievement = catalog.get(id);
        return achievement != null && unlocked(playerId, id) ? achievement.title : null;
    }

    synchronized void selectTitle(UUID playerId, String achievementId) {
        if (achievementId != null && (!unlocked(playerId, achievementId) || catalog.get(achievementId) == null))
            throw new IllegalStateException("That title has not been unlocked");
        store.mutate(playerId, player -> player.activeTitle = achievementId);
    }

    synchronized Map<String, Integer> contributedItems(UUID playerId, String achievementId) {
        return Map.copyOf(progress(playerId, achievementId).items);
    }

    synchronized long contributedCurrency(UUID playerId, String achievementId) {
        return progress(playerId, achievementId).currency;
    }

    private PlayerProgressStore.AchievementProgress progress(UUID playerId, String achievementId) {
        PlayerProgressStore.PlayerData player = store.player(playerId);
        PlayerProgressStore.AchievementProgress result = player.progress.get(achievementId);
        if (result == null) return new PlayerProgressStore.AchievementProgress();
        result.normalize();
        return result;
    }
}
