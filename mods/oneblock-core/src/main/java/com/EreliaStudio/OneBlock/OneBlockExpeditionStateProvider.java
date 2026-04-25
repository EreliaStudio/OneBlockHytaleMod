package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OneBlockExpeditionStateProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path filePath;
    private volatile SaveData state;
    public OneBlockExpeditionStateProvider(Path filePath)
    {
        this.filePath = filePath;
        this.state = load();
    }

    public synchronized boolean hasActiveExpedition()
    {
        return state.expeditionId != null && !state.expeditionId.isEmpty();
    }

    public synchronized String getActiveExpeditionId()
    {
        return state.expeditionId;
    }

    public synchronized int getTicksRemaining()
    {
        return state.ticksRemaining;
    }

    public synchronized void startExpedition(String expeditionId, int ticks)
    {
        state.expeditionId = expeditionId;
        state.ticksRemaining = ticks;
        state.timeBased = false;
        state.endTimeMs = 0;
        save();
    }

    public synchronized void startTimedExpedition(String expeditionId, long durationMs)
    {
        state.expeditionId = expeditionId;
        state.ticksRemaining = 0;
        state.timeBased = true;
        state.endTimeMs = System.currentTimeMillis() + durationMs;
        save();
    }

    /**
     * Called on each OneBlock break. Returns the completed expedition ID if the expedition
     * just finished, or null if the expedition is still ongoing or was not active.
     */
    public synchronized String onBreak()
    {
        if (!hasActiveExpedition()) return null;

        if (state.timeBased)
        {
            if (System.currentTimeMillis() < state.endTimeMs) return null;
        }
        else
        {
            state.ticksRemaining--;
            save();
            if (state.ticksRemaining > 0) return null;
        }

        String completedExpedition = state.expeditionId;
        state.expeditionId = null;
        state.ticksRemaining = 0;
        state.endTimeMs = 0;
        save();

        return completedExpedition;
    }

    public synchronized void endExpedition()
    {
        state.expeditionId = null;
        state.ticksRemaining = 0;
        state.endTimeMs = 0;
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
        private String expeditionId;
        private int ticksRemaining;
        private boolean timeBased;
        private long endTimeMs;
    }
}
