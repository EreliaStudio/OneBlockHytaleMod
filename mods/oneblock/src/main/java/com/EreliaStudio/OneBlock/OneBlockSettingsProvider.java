package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OneBlockSettingsProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
    }
}
