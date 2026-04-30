package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("removal")
public final class OneBlockHudService
{
    private final Map<PlayerRef, OneBlockProgressHud> hudsByPlayer = new HashMap<>();

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

        player.getHudManager().setCustomHud(playerRef, hud);
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
        player.getHudManager().resetHud(playerRef);
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

        hud.setTitleAndProgress(readableName(expeditionId), 1.0f);
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
        hud.setTitleAndProgress(readableName(expeditionId), progress);
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

        hud.setTitleAndProgress(readableName(dungeonId), 0.0f);
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

        hud.setTitleAndProgress("New expedition unlocked: " + readableName(expeditionId), 1.0f);
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

    private static String readableName(String id)
    {
        if (id == null || id.isBlank())
        {
            return "Unknown";
        }

        return id.replace("_", " ");
    }
}