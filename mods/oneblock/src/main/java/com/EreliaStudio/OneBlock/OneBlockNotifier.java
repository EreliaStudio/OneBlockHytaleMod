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

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin != null)
        {
            plugin.getHudService().showExpeditionUnlocked(player, expeditionId);
        }

        player.sendMessage(Message.raw("[Unlocked] " + readableExpeditionName(expeditionId)));

        // Intended later:
        // player.sendMessage(Message.translated("server.announcements.expedition_unlocked." + expeditionId));
    }

    public static void notifyExpeditionStarted(Store<EntityStore> store,
                                               Ref<EntityStore> playerRef,
                                               String expeditionId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || expeditionId == null || expeditionId.isBlank()) return;

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin != null)
        {
            int ticks = OneBlockExpeditionDefaults.getTicks(expeditionId);
            plugin.getHudService().showExpeditionStarted(player, expeditionId, ticks);
        }

        player.sendMessage(Message.raw(readableExpeditionName(expeditionId) + " expedition started."));

        // Intended later:
        // player.sendMessage(Message.translated("server.announcements.expedition_started." + expeditionId));
    }

    public static void notifyExpeditionCompleted(Store<EntityStore> store,
                                                 Ref<EntityStore> playerRef,
                                                 String expeditionId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || expeditionId == null || expeditionId.isBlank()) return;

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin != null)
        {
            plugin.getHudService().showExpeditionCompleted(player, expeditionId);
        }

        player.sendMessage(Message.raw(readableExpeditionName(expeditionId) + " expedition complete. The OneBlock has returned to default."));

        // Intended later:
        // player.sendMessage(Message.translated("server.announcements.expedition_completed." + expeditionId));
    }

    public static void notifyDungeonStarted(Store<EntityStore> store,
                                            Ref<EntityStore> playerRef,
                                            String dungeonId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || dungeonId == null || dungeonId.isBlank()) return;

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin != null)
        {
            int waves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
            plugin.getHudService().showDungeonStarted(player, dungeonId, waves);
        }

        player.sendMessage(Message.raw(readableExpeditionName(dungeonId) + " dungeon started."));
    }

    public static void notifyDungeonCompleted(Store<EntityStore> store,
                                              Ref<EntityStore> playerRef,
                                              String dungeonId)
    {
        Player player = getPlayer(store, playerRef);
        if (player == null || dungeonId == null || dungeonId.isBlank()) return;

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin != null)
        {
            plugin.getHudService().showDungeonCompleted(player, dungeonId);
        }

        player.sendMessage(Message.raw(readableExpeditionName(dungeonId) + " dungeon complete. The OneBlock has returned to default."));
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