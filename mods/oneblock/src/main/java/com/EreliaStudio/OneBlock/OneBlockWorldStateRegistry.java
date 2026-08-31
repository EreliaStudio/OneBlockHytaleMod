package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.universe.world.World;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns the persistent state of every OneBlock world.
 *
 * <p>Only worlds explicitly created for OneBlock are managed. Their state is
 * stored below {@code worlds/} in an isolated directory per world.</p>
 */
public final class OneBlockWorldStateRegistry
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path dataDirectory;
    private final Path registryPath;
    private final Set<String> managedWorlds = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, WorldState> states = new ConcurrentHashMap<>();

    public OneBlockWorldStateRegistry(Path dataDirectory)
    {
        this.dataDirectory = dataDirectory;
        this.registryPath = dataDirectory.resolve("oneblock-worlds.json");
        loadManagedWorlds();

        // Migrate registries written by older versions, which automatically
        // treated the normal server world as a OneBlock world.
        if (managedWorlds.remove(World.DEFAULT))
        {
            saveManagedWorlds();
        }
    }

    public boolean isManaged(World world)
    {
        return world != null && isManaged(world.getName());
    }

    public boolean isManaged(String worldName)
    {
        return worldName != null && managedWorlds.contains(worldName);
    }

    public synchronized void registerWorld(String worldName)
    {
        if (worldName == null || worldName.isBlank())
        {
            throw new IllegalArgumentException("World name cannot be blank");
        }
        if (World.DEFAULT.equals(worldName))
        {
            throw new IllegalArgumentException("The default world cannot be managed by OneBlock");
        }

        if (managedWorlds.add(worldName))
        {
            saveManagedWorlds();
        }
    }

    public Set<String> getManagedWorldNames()
    {
        return Collections.unmodifiableSet(Set.copyOf(managedWorlds));
    }

    public OneBlockExpeditionStateProvider expeditionState(World world)
    {
        return stateFor(world).expeditionState();
    }

    public OneBlockDungeonStateProvider dungeonState(World world)
    {
        return stateFor(world).dungeonState();
    }

    public OneBlockExpeditionStateProvider expeditionState(String worldName)
    {
        return stateFor(worldName).expeditionState();
    }

    public OneBlockDungeonStateProvider dungeonState(String worldName)
    {
        return stateFor(worldName).dungeonState();
    }

    private WorldState stateFor(World world)
    {
        if (world == null)
        {
            throw new IllegalArgumentException("World cannot be null");
        }
        return stateFor(world.getName());
    }

    private WorldState stateFor(String worldName)
    {
        if (!isManaged(worldName))
        {
            throw new IllegalArgumentException("World is not managed by OneBlock: " + worldName);
        }
        return states.computeIfAbsent(worldName, this::createState);
    }

    private WorldState createState(String worldName)
    {
        String directoryName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(worldName.getBytes(StandardCharsets.UTF_8));
        Path worldStateDirectory = dataDirectory.resolve("worlds").resolve(directoryName);

        return new WorldState(
                new OneBlockExpeditionStateProvider(worldStateDirectory.resolve("expedition.json")),
                new OneBlockDungeonStateProvider(worldStateDirectory.resolve("dungeon.json"))
        );
    }

    private void loadManagedWorlds()
    {
        if (!Files.exists(registryPath))
        {
            return;
        }

        try (Reader reader = Files.newBufferedReader(registryPath, StandardCharsets.UTF_8))
        {
            RegistryData data = GSON.fromJson(reader, RegistryData.class);
            if (data != null && data.worlds != null)
            {
                for (String worldName : data.worlds)
                {
                    if (worldName != null && !worldName.isBlank())
                    {
                        managedWorlds.add(worldName);
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private void saveManagedWorlds()
    {
        try
        {
            Files.createDirectories(registryPath.getParent());
            RegistryData data = new RegistryData();
            data.worlds = managedWorlds.stream().sorted().toArray(String[]::new);

            try (Writer writer = Files.newBufferedWriter(registryPath, StandardCharsets.UTF_8))
            {
                GSON.toJson(data, writer);
            }
        }
        catch (IOException ignored)
        {
        }
    }

    private record WorldState(OneBlockExpeditionStateProvider expeditionState,
                              OneBlockDungeonStateProvider dungeonState)
    {
    }

    private static final class RegistryData
    {
        private String[] worlds;
    }
}
