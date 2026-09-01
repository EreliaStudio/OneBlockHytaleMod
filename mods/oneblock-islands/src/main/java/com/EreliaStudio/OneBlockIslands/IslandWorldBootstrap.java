package com.EreliaStudio.OneBlockIslands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Creates the persistent void-world configuration owned by the islands module. */
final class IslandWorldBootstrap {
    private IslandWorldBootstrap() {}

    static boolean ensureConfig(Path worldsPath, String worldName) {
        if (worldsPath == null || worldName == null || worldName.isBlank()) return false;
        Path directory = worldsPath.resolve(worldName);
        Path config = directory.resolve("config.json");
        if (Files.exists(config)) return true;

        try {
            Files.createDirectories(directory);
            try (InputStream input = IslandWorldBootstrap.class.getResourceAsStream("/island-world-config-template.json")) {
                if (input == null) return false;
                JsonObject root = JsonParser.parseString(
                        new String(input.readAllBytes(), StandardCharsets.UTF_8)
                ).getAsJsonObject();
                root.addProperty("Seed", ThreadLocalRandom.current().nextLong());
                root.add("UUID", encodedUuid(UUID.randomUUID()));
                Files.writeString(
                        config,
                        new GsonBuilder().setPrettyPrinting().create().toJson(root),
                        StandardCharsets.UTF_8
                );
                return true;
            }
        } catch (Exception ignored) {
            return false;
        }
    }

    private static JsonObject encodedUuid(UUID uuid) {
        ByteBuffer bytes = ByteBuffer.allocate(16)
                .putLong(uuid.getMostSignificantBits())
                .putLong(uuid.getLeastSignificantBits());
        JsonObject encoded = new JsonObject();
        encoded.addProperty("$binary", Base64.getEncoder().encodeToString(bytes.array()));
        encoded.addProperty("$type", "04");
        return encoded;
    }
}
