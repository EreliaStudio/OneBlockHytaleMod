package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("removal")
public final class OneBlockHudService
{
    private final Map<PlayerRef, OneBlockProgressHud> hudsByPlayer = new ConcurrentHashMap<>();

    public void show(Player player)
    {
        if (player == null)
        {
            return;
        }

        PlayerRef playerRef = getPlayerRef(player);
        if (playerRef == null)
        {
            return;
        }

        OneBlockProgressHud hud = hudsByPlayer.computeIfAbsent(
                playerRef,
                OneBlockProgressHud::new
        );

        player.getHudManager().addCustomHud(playerRef, hud);
    }

    public void hide(Player player)
    {
        if (player == null)
        {
            return;
        }

        PlayerRef playerRef = getPlayerRef(player);
        if (playerRef == null)
        {
            return;
        }

        hudsByPlayer.remove(playerRef);
        player.getHudManager().removeCustomHud(playerRef, OneBlockProgressHud.HUD_KEY);
    }

    public void clear(Player player)
    {
        hide(player);
    }

    public void setTitle(Player player, String title)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        hud.setTitle(title);
    }

    public void setProgress(Player player, float progress)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        hud.setProgress(progress);
    }

    public void showExpeditionStarted(Player player, String expeditionId, int totalTicks)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        hud.setTitleAndProgress(localizedName(expeditionId), 1.0f);
    }

    public void updateExpeditionTicks(Player player, String expeditionId, int ticksRemaining, int totalTicks)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        if (totalTicks <= 0)
        {
            hud.setProgress(0.0f);
            return;
        }

        float progress = (float) ticksRemaining / (float) totalTicks;
        hud.setProgress(progress);
    }

    public void restoreExpeditionHud(Player player, String expeditionId, int ticksRemaining, int totalTicks)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        float progress = totalTicks > 0 ? (float) ticksRemaining / (float) totalTicks : 0.0f;
        hud.setTitleAndProgress(localizedName(expeditionId), progress);
    }

    public void showExpeditionCompleted(Player player, String expeditionId)
    {
        hide(player);
    }

    public void showDungeonStarted(Player player, String dungeonId, int totalWaves)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        hud.setTitleAndProgress(localizedName(dungeonId), 0.0f);
    }

    public void updateDungeonWave(Player player, String dungeonId, int completedWaves, int totalWaves)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        if (totalWaves <= 0)
        {
            hud.setProgress(0.0f);
            return;
        }

        float progress = (float) completedWaves / (float) totalWaves;
        hud.setProgress(progress);
    }

    public void showDungeonCompleted(Player player, String dungeonId)
    {
        hide(player);
    }

    public void showExpeditionUnlocked(Player player, String expeditionId)
    {
        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null)
        {
            return;
        }

        hud.setTitleAndProgress(Message.translation("server.announcements.expedition_unlocked." + expeditionId), 1.0f);
    }

    private OneBlockProgressHud getOrShow(Player player)
    {
        if (player == null)
        {
            return null;
        }

        show(player);

        PlayerRef playerRef = getPlayerRef(player);
        if (playerRef == null)
        {
            return null;
        }

        return hudsByPlayer.get(playerRef);
    }

    private static PlayerRef getPlayerRef(Player player)
    {
        if (player == null)
        {
            return null;
        }

        return player.getPlayerRef();
    }

    private static Message localizedName(String id)
    {
        return Message.translation("server.expeditions." + id + ".name");
    }

    /** Applies an atomic title/progress update, preventing stale multiplayer titles. */
    public void apply(Player player, OneBlockTrigger trigger)
    {
        if (player == null || trigger == null) return;
        if (!trigger.active())
        {
            hide(player);
            return;
        }

        OneBlockProgressHud hud = getOrShow(player);
        if (hud == null) return;
        hud.setTitleAndProgress(localizedName(trigger.expeditionId()), trigger.progress());
    }
}
