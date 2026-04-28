package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OneBlockDungeonStateProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path filePath;
    private volatile SaveData state;

    public OneBlockDungeonStateProvider(Path filePath)
    {
        this.filePath = filePath;
        this.state = load();
    }

    public synchronized boolean isDungeonActive()
    {
        return state.dungeonId != null && !state.dungeonId.isEmpty();
    }

    public synchronized String getActiveDungeonId()
    {
        return state.dungeonId;
    }

    public synchronized int getCurrentWaveIndex()
    {
        return state.currentWaveIndex;
    }

    public synchronized void startDungeon(String dungeonId)
    {
        state.dungeonId = dungeonId;
        state.currentWaveIndex = 0;
        save();
    }

    /**
     * Advances to the next wave. Returns the completed dungeon ID if all waves are done, null otherwise.
     */
    public synchronized String onWaveCompleted()
    {
        if (!isDungeonActive()) return null;

        state.currentWaveIndex++;
        int totalWaves = OneBlockDungeonDefaults.getWaveCount(state.dungeonId);
        if (state.currentWaveIndex < totalWaves)
        {
            save();
            return null;
        }

        String completedDungeon = state.dungeonId;
        state.dungeonId = null;
        state.currentWaveIndex = 0;
        save();
        return completedDungeon;
    }

    public synchronized void endDungeon()
    {
        state.dungeonId = null;
        state.currentWaveIndex = 0;
        save();
    }

    private void save()
    {
        try
        {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))
            {
                GSON.toJson(state, writer);
            }
        }
        catch (IOException ignored) {}
    }

    private SaveData load()
    {
        if (filePath == null || !Files.exists(filePath)) return new SaveData();
        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8))
        {
            SaveData loaded = GSON.fromJson(reader, SaveData.class);
            return loaded != null ? loaded : new SaveData();
        }
        catch (Exception ignored) { return new SaveData(); }
    }

    private static final class SaveData
    {
        private String dungeonId;
        private int currentWaveIndex;
    }
}
