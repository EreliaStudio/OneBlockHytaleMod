package com.EreliaStudio.OneBlockIslands;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import org.joml.Vector3d;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

final class HubService {
    static final String WORLD_NAME = "spawn";
    private static final String PLATFORM_MARKER = "oneblock-islands-platform-v1.marker";
    private static final String VOID_WORLD_GEN = "Void";
    private static final String VOID_ENVIRONMENT = "Env_Default_Void";
    private static final String VOID_TINT_HEX = "#5a992b";
    private static final Color VOID_TINT = new Color((byte) 0x5a, (byte) 0x99, (byte) 0x2b);
    private static final Transform SPAWN = new Transform(new Vector3d(0.5, 102, 0.5), new Rotation3f(0,0,0));
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final ConcurrentHashMap<World, CompletableFuture<World>> startedWorlds = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<World, CompletableFuture<Void>> platformInitializations = new ConcurrentHashMap<>();

    CompletableFuture<World> ensureLoaded() {
        Universe universe = Universe.get();
        if (universe == null) return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));
        try { ensureConfig(universe.getWorldsPath()); }
        catch (Exception e) { return CompletableFuture.failedFuture(e); }
        World loaded = universe.getWorld(WORLD_NAME);
        if (loaded != null) {
            try { configureVoidWorld(loaded); }
            catch (Exception e) { return CompletableFuture.failedFuture(e); }
        }
        CompletableFuture<World> future = loaded == null ? universe.loadWorld(WORLD_NAME) : awaitStarted(loaded);
        return future.thenCompose(this::awaitStarted)
                .thenCompose(world -> ensurePlatform(world).thenApply(ignored -> world));
    }

    void worldStarted(World world) {
        if (world == null || !WORLD_NAME.equalsIgnoreCase(world.getName())) return;
        startedWorlds.computeIfAbsent(world, ignored -> new CompletableFuture<>()).complete(world);
    }

    void worldAdded(World world) {
        if (world == null || !WORLD_NAME.equalsIgnoreCase(world.getName())) return;
        try { configureVoidWorld(world); }
        catch (Exception e) { throw new IllegalStateException("Could not convert spawn to a void world", e); }
    }

    CompletableFuture<Void> transfer(PlayerRef player) {
        if (player == null || player.getReference() == null || !player.getReference().isValid()) return CompletableFuture.completedFuture(null);
        World source = player.getReference().getStore().getExternalData().getWorld();
        return ensureLoaded().thenCompose(target -> {
            if (target.equals(source)) return CompletableFuture.completedFuture(null);
            return Universe.transferPlayerAsync(player, source, CompletableFuture.completedFuture(target), ignored -> SPAWN).thenApply(p -> null);
        });
    }

    private static synchronized void ensureConfig(Path worldsPath) throws Exception {
        Path directory = worldsPath.resolve(WORLD_NAME);
        Path config = directory.resolve("config.json");
        JsonObject root;
        if (Files.exists(config)) {
            root = JsonParser.parseString(Files.readString(config, StandardCharsets.UTF_8)).getAsJsonObject();
        } else {
            Files.createDirectories(directory);
            try (InputStream input = HubService.class.getResourceAsStream("/spawn-world-config-template.json")) {
                if (input == null) throw new IllegalStateException("Missing spawn world template");
                root = JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8)).getAsJsonObject();
            }
            UUID uuid = UUID.randomUUID();
            ByteBuffer bytes = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits());
            JsonObject encoded = new JsonObject();
            encoded.addProperty("$binary", Base64.getEncoder().encodeToString(bytes.array()));
            encoded.addProperty("$type", "04");
            root.add("UUID", encoded);
            root.addProperty("Seed", ThreadLocalRandom.current().nextLong());
        }

        JsonObject worldGen = root.has("WorldGen") && root.get("WorldGen").isJsonObject()
                ? root.getAsJsonObject("WorldGen") : new JsonObject();
        String previousType = worldGen.has("Type") ? worldGen.get("Type").getAsString() : null;
        boolean converting = !VOID_WORLD_GEN.equals(previousType);
        worldGen.addProperty("Type", VOID_WORLD_GEN);
        worldGen.addProperty("Environment", VOID_ENVIRONMENT);
        worldGen.addProperty("Tint", VOID_TINT_HEX);
        root.add("WorldGen", worldGen);
        root.addProperty("IsSpawnMarkersEnabled", false);
        Files.writeString(config, new GsonBuilder().setPrettyPrinting().create().toJson(root), StandardCharsets.UTF_8);

        if (converting) {
            resetGeneratedWorldData(directory);
            LOGGER.at(Level.INFO).log("Converted '" + WORLD_NAME + "' from " + previousType + " to a void world.");
        } else {
            LOGGER.at(Level.INFO).log("Void spawn world configuration ready at " + config);
        }
    }

    private static synchronized void configureVoidWorld(World world) throws Exception {
        WorldConfig config = world.getWorldConfig();
        if (config == null) throw new IllegalStateException("Spawn world has no world configuration");
        boolean converting = !(config.getWorldGenProvider() instanceof VoidWorldGenProvider);
        if (converting) resetGeneratedWorldData(world.getSavePath());
        config.setWorldGenProvider(new VoidWorldGenProvider(VOID_TINT, VOID_ENVIRONMENT));
        config.setSpawnProvider(new GlobalSpawnProvider(SPAWN));
        config.markChanged();
    }

    private static void resetGeneratedWorldData(Path worldDirectory) throws Exception {
        deleteDirectoryIfPresent(worldDirectory.resolve("chunks"));
        deleteDirectoryIfPresent(worldDirectory.resolve("resources"));
        Files.deleteIfExists(worldDirectory.resolve(PLATFORM_MARKER));
    }

    private static void deleteDirectoryIfPresent(Path directory) throws Exception {
        if (!Files.exists(directory)) return;
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws java.io.IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override public FileVisitResult postVisitDirectory(Path current, java.io.IOException error) throws java.io.IOException {
                if (error != null) throw error;
                Files.delete(current);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private CompletableFuture<World> awaitStarted(World world) {
        if (world.getChunkStore().getStore() != null) return CompletableFuture.completedFuture(world);

        CompletableFuture<World> started = startedWorlds.computeIfAbsent(world, ignored -> new CompletableFuture<>());
        // StartWorldEvent may have fired between the first check and insertion.
        if (world.getChunkStore().getStore() != null) started.complete(world);
        return started;
    }

    private CompletableFuture<Void> ensurePlatform(World world) {
        Path marker = world.getSavePath().resolve(PLATFORM_MARKER);
        if (Files.exists(marker)) return CompletableFuture.completedFuture(null);
        return platformInitializations.computeIfAbsent(world, ignored -> initializePlatform(world, marker));
    }

    private static CompletableFuture<Void> initializePlatform(World world, Path marker) {
        CompletableFuture<Void> initialized = new CompletableFuture<>();
        world.getChunkAsync(ChunkUtil.indexChunkFromBlock(0, 0)).whenComplete((chunk, loadError) -> {
            if (loadError != null) {
                initialized.completeExceptionally(loadError);
                return;
            }
            if (chunk == null) {
                initialized.completeExceptionally(new IllegalStateException("Spawn chunk could not be loaded"));
                return;
            }
            try {
                world.execute(() -> {
                    try {
                        for (int x=-4; x<=4; x++) for (int z=-4; z<=4; z++) world.setBlock(x, 100, z, "Rock_Stone");
                        world.setBlock(0, 101, 0, "Rock_Stone");
                        Files.writeString(marker, "Platform generated once. Replace freely with a custom hub.", StandardCharsets.UTF_8);
                        LOGGER.at(Level.INFO).log("Created minimal 9x9 spawn platform in '" + WORLD_NAME + "'.");
                        initialized.complete(null);
                    } catch (Exception e) {
                        initialized.completeExceptionally(e);
                    }
                });
            } catch (Exception e) {
                initialized.completeExceptionally(e);
            }
        });
        return initialized;
    }
}
