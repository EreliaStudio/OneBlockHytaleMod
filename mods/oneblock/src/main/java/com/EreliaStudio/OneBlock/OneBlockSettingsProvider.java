package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class OneBlockSettingsProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final double DEFAULT_FALLOFF_HEIGHT = -20.0;

    private final Path filePath;
    private volatile SaveData state;

    public OneBlockSettingsProvider(Path filePath)
    {
        this.filePath = filePath;
        this.state = load();
    }

    public synchronized boolean isFallProtectionEnabled()
    {
        return state.fallProtection;
    }

    public synchronized void setFallProtectionEnabled(boolean enabled)
    {
        state.fallProtection = enabled;
        save();
    }

    public synchronized double getFalloffHeight(String worldName)
    {
        if (worldName == null || state.falloffHeights == null)
        {
            return DEFAULT_FALLOFF_HEIGHT;
        }

        Double height = state.falloffHeights.get(worldName);
        return height == null || !Double.isFinite(height) ? DEFAULT_FALLOFF_HEIGHT : height;
    }

    public synchronized void setFalloffHeight(String worldName, double height)
    {
        if (worldName == null || worldName.isBlank())
        {
            throw new IllegalArgumentException("World name cannot be blank");
        }
        if (!Double.isFinite(height))
        {
            throw new IllegalArgumentException("Falloff height must be a finite number");
        }
        if (state.falloffHeights == null)
        {
            state.falloffHeights = new HashMap<>();
        }

        state.falloffHeights.put(worldName, height);
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
        catch (Exception ignored)
        {
            return new SaveData();
        }
    }

    private static final class SaveData
    {
        private boolean fallProtection = true;
        private Map<String, Double> falloffHeights = new HashMap<>();
    }
}
