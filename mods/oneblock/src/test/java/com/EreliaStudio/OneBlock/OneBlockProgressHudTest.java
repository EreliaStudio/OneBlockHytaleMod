package com.EreliaStudio.OneBlock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OneBlockProgressHudTest
{
    @Test
    void keepsTheProgressBarInsideTheMinimumFrame()
    {
        OneBlockProgressHud.HudLayout layout = OneBlockProgressHud.layoutForTitle("");

        assertEquals(604, layout.frameWidth());
        assertEquals(56, layout.frameBWidth());
        assertEquals(56, layout.frameDWidth());
        assertEquals(layout.frameWidth() - 38, layout.barWidth());
    }

    @Test
    void growsTheFrameAndBarTogetherForLongTitles()
    {
        OneBlockProgressHud.HudLayout shortLayout = OneBlockProgressHud.layoutForTitle("Meadow");
        OneBlockProgressHud.HudLayout longLayout = OneBlockProgressHud.layoutForTitle(
                "Citadelle des squelettes calcinés"
        );

        assertTrue(longLayout.titleWidth() > shortLayout.titleWidth());
        assertTrue(longLayout.frameWidth() > shortLayout.frameWidth());
        assertEquals(longLayout.titleWidth() + 176, longLayout.frameWidth());
        assertEquals(longLayout.frameWidth() - 38, longLayout.barWidth());
        assertEquals(
                longLayout.frameWidth() - 492,
                longLayout.frameBWidth() + longLayout.frameDWidth()
        );
        assertTrue(Math.abs(longLayout.frameBWidth() - longLayout.frameDWidth()) <= 1);
    }
}
