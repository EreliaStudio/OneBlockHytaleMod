package com.EreliaStudio.OneBlockIslands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

final class HubService {
    static final String WORLD_NAME = "spawn";
    private static final String PLATFORM_MARKER = "oneblock-islands-platform-v1.marker";
    private static final Transform SPAWN = new Transform(new Vector3d(0.5, 102, 0.5), new Rotation3f(0,0,0));
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    CompletableFuture<World> ensureLoaded() {
        Universe universe = Universe.get();
        if (universe == null) return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));
        try { ensureConfig(universe.getWorldsPath()); }
        catch (Exception e) { return CompletableFuture.failedFuture(e); }
        World loaded = universe.getWorld(WORLD_NAME);
        CompletableFuture<World> future = loaded == null ? universe.loadWorld(WORLD_NAME) : CompletableFuture.completedFuture(loaded);
        return future.thenApply(world -> { ensurePlatform(world); return world; });
    }

    CompletableFuture<Void> transfer(PlayerRef player) {
        if (player == null || player.getReference() == null || !player.getReference().isValid()) return CompletableFuture.completedFuture(null);
        World source = player.getReference().getStore().getExternalData().getWorld();
        return ensureLoaded().thenCompose(target -> {
            if (target.equals(source)) return CompletableFuture.completedFuture(null);
            return Universe.transferPlayerAsync(player, source, CompletableFuture.completedFuture(target), ignored -> SPAWN).thenApply(p -> null);
        });
    }

    private static void ensureConfig(Path worldsPath) throws Exception {
        Path directory = worldsPath.resolve(WORLD_NAME);
        Path config = directory.resolve("config.json");
        if (Files.exists(config)) return;
        Files.createDirectories(directory);
        try (InputStream input = HubService.class.getResourceAsStream("/spawn-world-config-template.json")) {
            if (input == null) throw new IllegalStateException("Missing spawn world template");
            JsonObject root = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            UUID uuid = UUID.randomUUID();
            ByteBuffer bytes = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
            JsonObject encoded = new JsonObject();
            encoded.addProperty("$binary", Base64.getEncoder().encodeToString(bytes.array()));
            encoded.addProperty("$type", "04");
            root.add("UUID", encoded);
            root.addProperty("Seed", ThreadLocalRandom.current().nextLong());
            Files.writeString(config, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);
            LOGGER.at(Level.INFO).log("Created void spawn world configuration at " + config);
        }
    }

    private static void ensurePlatform(World world) {
        Path marker = world.getSavePath().resolve(PLATFORM_MARKER);
        if (Files.exists(marker)) return;
        world.getChunkAsync(ChunkUtil.indexChunkFromBlock(0, 0)).thenRun(() -> world.execute(() -> {
            try {
                for (int x=-4; x<=4; x++) for (int z=-4; z<=4; z++) world.setBlock(x, 100, z, "Rock_Stone");
                world.setBlock(0, 101, 0, "Rock_Stone");
                Files.writeString(marker, "Platform generated once. Replace freely with a custom hub.", StandardCharsets.UTF_8);
                LOGGER.at(Level.INFO).log("Created minimal 9x9 spawn platform in '" + WORLD_NAME + "'.");
            } catch (Exception e) { LOGGER.at(Level.SEVERE).withCause(e).log("Could not initialize spawn platform"); }
        }));
    }
}
