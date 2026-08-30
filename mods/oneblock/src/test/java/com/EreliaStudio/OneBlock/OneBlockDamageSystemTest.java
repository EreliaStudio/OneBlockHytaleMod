package com.EreliaStudio.OneBlock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OneBlockDamageSystemTest
{
    private static final float DELTA = 0.000001F;

    @Test
    void oneTickBlockTakesFullDamageAtFullHealth()
    {
        assertEquals(1.0F, OneBlockDamageCalculator.forRemainingHealth(1.0F, 1), DELTA);
    }

    @Test
    void configuredTicksApplyAnExactFractionOfHealth()
    {
        assertEquals(0.25F, OneBlockDamageCalculator.forRemainingHealth(1.0F, 4), DELTA);
    }

    @Test
    void finalHitIsClampedToTheRemainingHealth()
    {
        assertEquals(0.125F, OneBlockDamageCalculator.forRemainingHealth(0.125F, 4), DELTA);
    }

    @Test
    void destroyedBlockDoesNotReceiveNegativeDamage()
    {
        assertEquals(0.0F, OneBlockDamageCalculator.forRemainingHealth(0.0F, 4), DELTA);
    }

    @Test
    void crudePickaxePreservesTheConfiguredTickCount()
    {
        float multiplier = OneBlockToolStrength.calculateMultiplier(5, 0.25F, 0.25F);

        assertEquals(1.0F, multiplier, DELTA);
        assertEquals(
                0.125F,
                OneBlockDamageCalculator.forRemainingHealth(1.0F, 8, multiplier),
                DELTA
        );
    }

    @Test
    void adamantitePickaxeIsFourTimesFasterThanCrude()
    {
        float multiplier = OneBlockToolStrength.calculateMultiplier(40, 1.0F, 0.25F);

        assertEquals(4.0F, multiplier, DELTA);
        assertEquals(
                0.5F,
                OneBlockDamageCalculator.forRemainingHealth(1.0F, 8, multiplier),
                DELTA
        );
    }

    @Test
    void itemLevelDistinguishesToolsWhenNativePowerPlateaus()
    {
        float adamantite = OneBlockToolStrength.calculateMultiplier(40, 1.0F, 0.25F);
        float mithril = OneBlockToolStrength.calculateMultiplier(50, 1.0F, 0.25F);

        assertEquals(4.0F, adamantite, DELTA);
        assertEquals(5.0F, mithril, DELTA);
    }

    @Test
    void highToolDamageStillClampsToRemainingHealth()
    {
        assertEquals(
                0.2F,
                OneBlockDamageCalculator.forRemainingHealth(0.2F, 3, 4.0F),
                DELTA
        );
    }
}
