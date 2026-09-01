package com.EreliaStudio.OneBlockIslands;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class IslandWorldBootstrapTest {
    @TempDir Path worlds;

    @Test void createsVoidConfigurationInsideTheIslandWorldDirectory() throws Exception {
        assertTrue(IslandWorldBootstrap.ensureConfig(worlds, "ob_test"));

        String config = Files.readString(worlds.resolve("ob_test").resolve("config.json"));
        assertTrue(config.contains("\"Type\": \"Void\""));
        assertTrue(config.contains("\"X\": 0.5"));
        assertTrue(config.contains("\"Y\": 102.0"));
    }
}
