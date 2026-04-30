package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class OneBlockNotifier
{
    private OneBlockNotifier() {}

    public static void notifyExpeditionUnlocked(Store<EntityStore> store,
                                                Ref<EntityStore> playerRef,
                                                String expeditionId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || expeditionId == null || expeditionId.isBlank()) return;

        String key = "oneblock.announcements.expedition_unlocked." + expeditionId;

        // TODO: replace with the real translated-message constructor/method
        // when confirmed in the Hytale API.
        player.sendMessage(Message.raw("[Unlocked] " + readableExpeditionName(expeditionId)));

        // Intended later:
        // player.sendMessage(Message.translated(key));
    }

    public static void notifyExpeditionStarted(Store<EntityStore> store,
                                               Ref<EntityStore> playerRef,
                                               String expeditionId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || expeditionId == null || expeditionId.isBlank()) return;

        String key = "oneblock.announcements.expedition_started." + expeditionId;
        player.sendMessage(Message.raw(readableExpeditionName(expeditionId) + " expedition started."));

        // Intended later:
        // player.sendMessage(Message.translated(key));
    }

    public static void notifyExpeditionCompleted(Store<EntityStore> store,
                                                 Ref<EntityStore> playerRef,
                                                 String expeditionId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || expeditionId == null || expeditionId.isBlank()) return;

        String key = "oneblock.announcements.expedition_completed." + expeditionId;
        player.sendMessage(Message.raw(readableExpeditionName(expeditionId) + " expedition complete. The OneBlock has returned to default."));

        // Intended later:
        // player.sendMessage(Message.translated(key));
    }

    private static Player getPlayer(Store<EntityStore> store, Ref<EntityStore> playerRef)
    {
        if (store == null || playerRef == null) return null;
        return store.getComponent(playerRef, Player.getComponentType());
    }

    private static String readableExpeditionName(String expeditionId)
    {
        return expeditionId.replace("_", " ");
    }
}