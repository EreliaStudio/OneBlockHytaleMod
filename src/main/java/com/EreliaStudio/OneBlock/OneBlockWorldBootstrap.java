package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
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
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

final class OneBlockWorldBootstrap
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String VOID_WORLD_GEN = "Void";
    private static final String VOID_ENVIRONMENT = "Env_Default_Void";
    private static final String VOID_TINT = "#5a992b";
    private static final String WORLD_NAME = "default";
    private static final String MARKER_FILE = "void-world-ready.marker";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private OneBlockWorldBootstrap()
    {
    }

    static void ensureVoidDefaultWorldConfig(Path dataDir)
    {
        Path serverRoot = resolveServerRoot(dataDir);
        if (serverRoot == null)
        {
            LOGGER.at(Level.WARNING).log("Could not resolve server root to configure void world.");
            return;
        }

        Path worldDir = serverRoot.resolve("universe").resolve("worlds").resolve(WORLD_NAME);
        boolean worldDirMissing = !Files.exists(worldDir);
        Path configPath = worldDir.resolve("config.json");
        if (worldDirMissing || !Files.exists(configPath))
        {
            if (!createWorldConfigFromTemplate(worldDir, configPath))
            {
                LOGGER.at(Level.WARNING).log("World config not found at " + configPath);
                return;
            }
        }

        boolean updated = updateWorldGenToVoid(configPath);
        if (updated)
        {
            LOGGER.at(Level.INFO).log("WorldGen for '" + WORLD_NAME + "' set to Void.");
        }

        Path marker = worldDir.resolve(MARKER_FILE);
        if (Files.exists(marker))
        {
            return;
        }

        Path chunksDir = worldDir.resolve("chunks");
        Path resourcesDir = worldDir.resolve("resources");
        if (Files.exists(chunksDir) || Files.exists(resourcesDir))
        {
            try
            {
                if (Files.exists(chunksDir))
                {
                    deleteDirectory(chunksDir);
                }
                if (Files.exists(resourcesDir))
                {
                    deleteDirectory(resourcesDir);
                }
                LOGGER.at(Level.INFO).log("Cleared world data for a fresh void world.");
            }
            catch (IOException e)
            {
                LOGGER.at(Level.WARNING).log("Failed to clear world chunks: " + e.getMessage());
            }
        }

        try
        {
            Files.writeString(marker, "ready", StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            LOGGER.at(Level.WARNING).log("Failed to write world reset marker: " + e.getMessage());
        }
    }

    private static boolean createWorldConfigFromTemplate(Path worldDir, Path configPath)
    {
        try
        {
            Files.createDirectories(worldDir);
        }
        catch (IOException e)
        {
            LOGGER.at(Level.WARNING).log("Failed to create world directory: " + e.getMessage());
            return false;
        }

        try (InputStream in = OneBlockWorldBootstrap.class.getResourceAsStream("/oneblock-world-config-template.json"))
        {
            if (in == null)
            {
                LOGGER.at(Level.WARNING).log("Missing world config template resource.");
                return false;
            }

            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            // Randomize seed + UUID for clean worlds
            root.addProperty("Seed", ThreadLocalRandom.current().nextLong());
            root.add("UUID", createUuidBinary(UUID.randomUUID()));

            // Ensure spawn matches our desired coordinates
            JsonObject spawnProvider = getOrCreateObject(root, "SpawnProvider");
            spawnProvider.addProperty("Id", "Global");
            JsonObject spawnPoint = getOrCreateObject(spawnProvider, "SpawnPoint");
            spawnPoint.addProperty("X", 0.0);
            spawnPoint.addProperty("Y", 102.0);
            spawnPoint.addProperty("Z", 0.0);
            spawnPoint.addProperty("Pitch", 0.0);
            spawnPoint.addProperty("Yaw", 0.0);
            spawnPoint.addProperty("Roll", 0.0);
            spawnProvider.add("SpawnPoint", spawnPoint);
            root.add("SpawnProvider", spawnProvider);
            root.addProperty("IsSpawnMarkersEnabled", false);

            JsonObject worldGen = getOrCreateObject(root, "WorldGen");
            worldGen.addProperty("Type", VOID_WORLD_GEN);
            worldGen.addProperty("Environment", VOID_ENVIRONMENT);
            worldGen.addProperty("Tint", VOID_TINT);
            root.add("WorldGen", worldGen);

            Files.writeString(configPath, GSON.toJson(root), StandardCharsets.UTF_8);
            LOGGER.at(Level.INFO).log("Created world config at " + configPath);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.at(Level.WARNING).log("Failed to create world config: " + e.getMessage());
            return false;
        }
    }

    private static JsonObject createUuidBinary(UUID uuid)
    {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        String base64 = Base64.getEncoder().encodeToString(buffer.array());

        JsonObject uuidObj = new JsonObject();
        uuidObj.addProperty("$binary", base64);
        uuidObj.addProperty("$type", "04");
        return uuidObj;
    }

    private static boolean updateWorldGenToVoid(Path configPath)
    {
        try
        {
            String content = Files.readString(configPath, StandardCharsets.UTF_8);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            JsonObject worldGen = getOrCreateObject(root, "WorldGen");
            String currentType = worldGen.has("Type") ? worldGen.get("Type").getAsString() : null;
            boolean changed = false;

            if (!VOID_WORLD_GEN.equals(currentType))
            {
                worldGen.addProperty("Type", VOID_WORLD_GEN);
                changed = true;
            }

            String currentEnvironment = worldGen.has("Environment") ? worldGen.get("Environment").getAsString() : null;
            if (!VOID_ENVIRONMENT.equals(currentEnvironment))
            {
                worldGen.addProperty("Environment", VOID_ENVIRONMENT);
                changed = true;
            }

            String currentTint = worldGen.has("Tint") ? worldGen.get("Tint").getAsString() : null;
            if (!VOID_TINT.equalsIgnoreCase(currentTint))
            {
                worldGen.addProperty("Tint", VOID_TINT);
                changed = true;
            }

            Boolean spawnMarkers = root.has("IsSpawnMarkersEnabled") ? root.get("IsSpawnMarkersEnabled").getAsBoolean() : null;
            if (spawnMarkers == null || spawnMarkers)
            {
                root.addProperty("IsSpawnMarkersEnabled", false);
                changed = true;
            }

            if (!changed)
            {
                return false;
            }

            root.add("WorldGen", worldGen);
            Files.writeString(configPath, GSON.toJson(root), StandardCharsets.UTF_8);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.at(Level.WARNING).log("Failed to update world config: " + e.getMessage());
            return false;
        }
    }

    private static JsonObject getOrCreateObject(JsonObject root, String key)
    {
        JsonElement element = root.get(key);
        if (element != null && element.isJsonObject())
        {
            return element.getAsJsonObject();
        }
        return new JsonObject();
    }

    private static Path resolveServerRoot(Path dataDir)
    {
        Path fromCodeSource = resolveServerRootFromCodeSource();
        if (fromCodeSource != null)
        {
            return fromCodeSource;
        }

        if (dataDir == null)
        {
            return null;
        }

        Path modsDir = dataDir.getParent();
        if (modsDir == null)
        {
            return null;
        }

        Path serverRoot = modsDir.getParent();
        if (serverRoot == null)
        {
            return null;
        }

        return serverRoot;
    }

    private static Path resolveServerRootFromCodeSource()
    {
        var location = OneBlockWorldBootstrap.class.getProtectionDomain().getCodeSource();
        if (location == null)
        {
            return null;
        }
        try
        {
            Path codePath = Path.of(location.getLocation().toURI());
            if (Files.isRegularFile(codePath))
            {
                Path modsDir = codePath.getParent();
                return modsDir != null ? modsDir.getParent() : null;
            }
            if (Files.isDirectory(codePath))
            {
                Path modsDir = codePath.getParent();
                return modsDir != null ? modsDir.getParent() : null;
            }
        }
        catch (Exception ignored)
        {
        }
        return null;
    }

    private static void deleteDirectory(Path path) throws IOException
    {
        Files.walkFileTree(path, new SimpleFileVisitor<>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                Files.deleteIfExists(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
            {
                Files.deleteIfExists(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
