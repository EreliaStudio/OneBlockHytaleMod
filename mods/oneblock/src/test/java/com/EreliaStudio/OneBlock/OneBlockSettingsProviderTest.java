package com.EreliaStudio.OneBlock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class OneBlockSettingsProviderTest
{
    @TempDir
    Path dataDirectory;

    @Test
    void falloffHeightDefaultsToOneForEveryWorld()
    {
        OneBlockSettingsProvider settings = settings();

        assertEquals(1.0, settings.getFalloffHeight("default"));
        assertEquals(1.0, settings.getFalloffHeight("oneblock-party"));
    }

    @Test
    void falloffHeightsAreIndependentAndPersistentPerWorld()
    {
        OneBlockSettingsProvider settings = settings();
        settings.setFalloffHeight("default", -35.0);
        settings.setFalloffHeight("oneblock-party", 12.5);

        OneBlockSettingsProvider reloaded = settings();
        assertEquals(-35.0, reloaded.getFalloffHeight("default"));
        assertEquals(12.5, reloaded.getFalloffHeight("oneblock-party"));
        assertEquals(1.0, reloaded.getFalloffHeight("another-world"));
    }

    @Test
    void legacySettingsWithoutWorldHeightsUseTheNewDefault() throws Exception
    {
        Files.writeString(dataDirectory.resolve("oneblock-settings.json"), "{\"fallProtection\":false}");

        OneBlockSettingsProvider settings = settings();

        assertFalse(settings.isFallProtectionEnabled());
        assertEquals(1.0, settings.getFalloffHeight("default"));
    }

    private OneBlockSettingsProvider settings()
    {
        return new OneBlockSettingsProvider(dataDirectory.resolve("oneblock-settings.json"));
    }
}
