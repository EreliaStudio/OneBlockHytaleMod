package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.Locale;

public final class OneBlockProgressHud extends CustomUIHud
{
    public static final String HUD_KEY = "OneBlockProgressHud";
    private static final String UI_FILE = "OneBlockHud.ui";
    private static final int TITLE_FONT_SIZE = 22;
    private static final float TITLE_LETTER_SPACING = 1.2f;
    private static final int FRAME_A_WIDTH = 76;
    private static final int FRAME_B_MIN_WIDTH = 56;
    private static final int FRAME_C_WIDTH = 340;
    private static final int FRAME_D_MIN_WIDTH = 56;
    private static final int FRAME_E_WIDTH = 76;
    private static final int FRAME_MIN_WIDTH = FRAME_A_WIDTH + FRAME_B_MIN_WIDTH + FRAME_C_WIDTH
            + FRAME_D_MIN_WIDTH + FRAME_E_WIDTH;
    private static final int FRAME_HEIGHT = 108;
    private static final int TITLE_HORIZONTAL_PADDING = 24;
    private static final int BAR_LEFT_INSET = 19;
    private static final int BAR_RIGHT_INSET = 19;
    private static final int BAR_TOP = 70;
    private static final int BAR_HEIGHT = 21;
    private static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, TITLE_FONT_SIZE);
    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, true);

    private final PlayerRef playerRef;

    public OneBlockProgressHud(PlayerRef playerRef)
    {
        super(playerRef, HUD_KEY);
        this.playerRef = playerRef;
    }

    @Override
    protected void build(UICommandBuilder builder)
    {
        builder.append(UI_FILE);
        updateLayout(builder, "");
    }

    public void clear()
    {
        UICommandBuilder builder = new UICommandBuilder();

        setTitle(builder, "", "");
        builder.set("#OneBlockBar.Value", 0.0f);

        update(false, builder);
    }

    public void setTitle(String title)
    {
        UICommandBuilder builder = new UICommandBuilder();

        String safeTitle = title == null ? "" : title;
        setTitle(builder, safeTitle, safeTitle);

        update(false, builder);
    }

    public void setTitle(Message title)
    {
        UICommandBuilder builder = new UICommandBuilder();

        Message safeTitle = title == null ? Message.empty() : title;
        setTitle(builder, safeTitle, resolveTitle(safeTitle));

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

        String safeTitle = title == null ? "" : title;
        setTitle(builder, safeTitle, safeTitle);
        builder.set("#OneBlockBar.Value", clampedProgress);

        update(false, builder);
    }

    public void setTitleAndProgress(Message title, float progress)
    {
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));

        UICommandBuilder builder = new UICommandBuilder();

        Message safeTitle = title == null ? Message.empty() : title;
        setTitle(builder, safeTitle, resolveTitle(safeTitle));
        builder.set("#OneBlockBar.Value", clampedProgress);

        update(false, builder);
    }

    private static void setTitle(UICommandBuilder builder, String title, String textForSizing)
    {
        builder.set("#OneBlockBarTitle.Text", title);
        updateLayout(builder, textForSizing);
    }

    private static void setTitle(UICommandBuilder builder, Message title, String textForSizing)
    {
        builder.set("#OneBlockBarTitle.Text", title);
        updateLayout(builder, textForSizing);
    }

    private static void updateLayout(UICommandBuilder builder, String title)
    {
        HudLayout layout = layoutForTitle(title);

        Anchor frameAnchor = new Anchor();
        frameAnchor.setWidth(Value.of(layout.frameWidth()));
        frameAnchor.setHeight(Value.of(FRAME_HEIGHT));
        builder.setObject("#OneBlockHudContent.Anchor", frameAnchor);

        setSliceWidth(builder, "#OneBlockHudFrameB.Anchor", layout.frameBWidth());
        setSliceWidth(builder, "#OneBlockHudFrameD.Anchor", layout.frameDWidth());

        Anchor barAnchor = new Anchor();
        barAnchor.setTop(Value.of(BAR_TOP));
        barAnchor.setLeft(Value.of(BAR_LEFT_INSET));
        barAnchor.setWidth(Value.of(layout.barWidth()));
        barAnchor.setHeight(Value.of(BAR_HEIGHT));
        builder.setObject("#OneBlockBar.Anchor", barAnchor);
    }

    private static void setSliceWidth(UICommandBuilder builder, String selector, int width)
    {
        Anchor anchor = new Anchor();
        anchor.setWidth(Value.of(width));
        anchor.setHeight(Value.of(FRAME_HEIGHT));
        builder.setObject(selector, anchor);
    }

    static HudLayout layoutForTitle(String title)
    {
        String renderedTitle = title == null ? "" : title.toUpperCase(Locale.ROOT);
        int characterCount = renderedTitle.codePointCount(0, renderedTitle.length());
        var textBounds = TITLE_FONT.getStringBounds(renderedTitle, FONT_RENDER_CONTEXT);
        double glyphWidth = textBounds.getWidth();
        double spacingWidth = Math.max(0, characterCount - 1) * TITLE_LETTER_SPACING;
        int titleWidth = (int) Math.ceil(glyphWidth + spacingWidth);
        int fixedOuterWidth = FRAME_A_WIDTH + FRAME_E_WIDTH;
        int requiredFrameWidth = fixedOuterWidth + titleWidth + TITLE_HORIZONTAL_PADDING;
        int frameWidth = Math.max(FRAME_MIN_WIDTH, requiredFrameWidth);
        int extraWidth = frameWidth - FRAME_MIN_WIDTH;
        int frameBWidth = FRAME_B_MIN_WIDTH + (extraWidth / 2);
        int frameDWidth = FRAME_D_MIN_WIDTH + extraWidth - (extraWidth / 2);
        int barWidth = frameWidth - BAR_LEFT_INSET - BAR_RIGHT_INSET;
        return new HudLayout(titleWidth, frameWidth, frameBWidth, frameDWidth, barWidth);
    }

    private String resolveTitle(Message title)
    {
        String rawText = title.getRawText();
        if (rawText != null)
        {
            return rawText;
        }

        String messageId = title.getMessageId();
        if (messageId == null)
        {
            return "";
        }

        String localized = I18nModule.get().getMessage(playerRef.getLanguage(), messageId);
        return localized == null ? messageId : localized;
    }

    record HudLayout(int titleWidth, int frameWidth, int frameBWidth, int frameDWidth, int barWidth)
    {
    }
}
