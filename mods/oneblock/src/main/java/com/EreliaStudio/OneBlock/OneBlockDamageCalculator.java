package com.EreliaStudio.OneBlock;

/** Pure deterministic OneBlock damage calculation, independent of the server API. */
final class OneBlockDamageCalculator
{
    private OneBlockDamageCalculator() {}

    /**
     * Returns one configured damage fraction, clamped to the block's remaining
     * normalized health so the final hit reaches exactly zero.
     */
    static float forRemainingHealth(float remainingHealth, int ticks)
    {
        return forRemainingHealth(remainingHealth, ticks, 1.0F);
    }

    /**
     * Applies the configured base resistance with a multiplier supplied by
     * the held tool. A multiplier of 1 preserves the configured tick count.
     */
    static float forRemainingHealth(float remainingHealth,
                                    int ticks,
                                    float toolMultiplier)
    {
        float safeRemainingHealth = Math.max(0.0F, remainingHealth);
        float safeMultiplier = Math.max(0.0F, toolMultiplier);
        float damagePerTick = safeMultiplier / Math.max(1, ticks);
        return Math.min(damagePerTick, safeRemainingHealth);
    }
}
