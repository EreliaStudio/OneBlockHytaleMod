package com.EreliaStudio.OneBlock;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class OneBlockTriggerTest
{
    @Test
    void expeditionCarriesNameAndCompletedProgress()
    {
        OneBlockTrigger trigger = OneBlockTrigger.expedition(
                "shared", new Vector3i(1, 2, 3), UUID.randomUUID(),
                "Default", 15, 20, true);

        assertEquals("Meadow", trigger.name());
        assertEquals(0.25f, trigger.progress());
        assertTrue(trigger.active());
    }

    @Test
    void subscriptionsAreWorldAndNodeSpecificAndSwitchAtomically()
    {
        OneBlockSubscriptions subscriptions = new OneBlockSubscriptions();
        UUID player = UUID.randomUUID();
        Vector3i first = new Vector3i(1, 2, 3);
        Vector3i second = new Vector3i(4, 5, 6);

        subscriptions.listen(player, "shared", first);
        assertTrue(subscriptions.isListening(player, "shared", first));
        assertFalse(subscriptions.isListening(player, "other", first));

        subscriptions.listen(player, "shared", second);
        assertFalse(subscriptions.isListening(player, "shared", first));
        assertTrue(subscriptions.isListening(player, "shared", second));
    }
}
