package com.EreliaStudio.OneBlock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class OneBlockDisplayNamesTest
{
    @Test
    void starterExpeditionUsesItsConfiguredDisplayName()
    {
        assertEquals("Meadow", OneBlockDisplayNames.get("Default"));
    }

    @Test
    void otherIdsKeepTheirReadableFallback()
    {
        assertEquals("Forest Edge", OneBlockDisplayNames.get("ForestEdge"));
    }
}
