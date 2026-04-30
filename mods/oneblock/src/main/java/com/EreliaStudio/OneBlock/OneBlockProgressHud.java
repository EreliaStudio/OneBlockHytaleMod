package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

public final class OneBlockProgressHud extends CustomUIHud
{
    private static final String UI_FILE = "OneBlockHud.ui";

    public OneBlockProgressHud(PlayerRef playerRef)
    {
        super(playerRef);
    }

    @Override
    protected void build(UICommandBuilder builder)
    {
        builder.append(UI_FILE);
    }

    public void clear()
    {
        UICommandBuilder builder = new UICommandBuilder();

        builder.set("#OneBlockBarTitle.Text", "");
        builder.set("#OneBlockBar.Value", 0.0f);

        update(false, builder);
    }

    public void setTitle(String title)
    {
        UICommandBuilder builder = new UICommandBuilder();

        builder.set("#OneBlockBarTitle.Text", title == null ? "" : title);

        update(false, builder);
    }

    public void setProgress(float progress)
    {
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));

        UICommandBuilder builder = new UICommandBuilder();

        builder.set("#OneBlockBar.Value", clampedProgress);

        update(false, builder);
    }

    public void setProgress(int currentValue, int maximumValue)
    {
        if (maximumValue <= 0)
        {
            setProgress(0.0f);
            return;
        }

        float progress = (float) currentValue / (float) maximumValue;
        setProgress(progress);
    }

    public void setRemainingProgress(int currentValue, int maximumValue)
    {
        if (maximumValue <= 0)
        {
            setProgress(0.0f);
            return;
        }

        float progress = 1.0f - ((float) currentValue / (float) maximumValue);
        setProgress(progress);
    }

    public void setTitleAndProgress(String title, float progress)
    {
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));

        UICommandBuilder builder = new UICommandBuilder();

        builder.set("#OneBlockBarTitle.Text", title == null ? "" : title);
        builder.set("#OneBlockBar.Value", clampedProgress);

        update(false, builder);
    }
}