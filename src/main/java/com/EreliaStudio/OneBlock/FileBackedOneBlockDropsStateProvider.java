package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class FileBackedOneBlockDropsStateProvider implements OneBlockDropsStateProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path filePath;
    private final Map<UUID, OneBlockPlayerDropsState> stateByPlayer = new HashMap<>();
    private boolean dirty = false;

    public FileBackedOneBlockDropsStateProvider(Path filePath)
    {
        this.filePath = filePath;
        load();
    }

    @Override
    public synchronized List<String> getEnabledDrops(UUID playerId)
    {
        OneBlockPlayerDropsState s = state(playerId);
        if (s == null || s.enabledDrops.isEmpty())
        {
            return Collections.singletonList(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }

        return new ArrayList<>(s.enabledDrops);
    }

    @Override
    public synchronized boolean isUnlocked(UUID playerId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        return s != null && s.unlockedDrops.contains(dropItemId);
    }

    @Override
    public synchronized boolean unlock(UUID playerId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        if (s == null)
        {
            return false;
        }

        if (s.unlockedDrops.contains(dropItemId))
        {
            return false;
        }

        s.unlockedDrops.add(dropItemId);
        s.enabledDrops.add(dropItemId);
        markDirty();
        return true;
    }

    @Override
    public synchronized boolean lock(UUID playerId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        if (OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId))
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        if (s == null)
        {
            return false;
        }

        boolean removed = s.unlockedDrops.remove(dropItemId);
        s.enabledDrops.remove(dropItemId);
        ensureDefaults(s);

        if (removed)
        {
            markDirty();
        }

        return removed;
    }

    @Override
    public synchronized boolean setEnabled(UUID playerId, String dropItemId, boolean enabled)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerDropsState s = state(playerId);
        if (s == null)
        {
            return false;
        }

        if (!s.unlockedDrops.contains(dropItemId))
        {
            return false;
        }

        boolean changed;
        if (enabled)
        {
            changed = s.enabledDrops.add(dropItemId);
        }
        else
        {
            changed = s.enabledDrops.remove(dropItemId);
            ensureDefaults(s);
        }

        if (changed)
        {
            markDirty();
        }

        return true;
    }

    public synchronized void saveIfDirty()
    {
        if (!dirty)
        {
            return;
        }

        SaveData data = new SaveData();
        for (Map.Entry<UUID, OneBlockPlayerDropsState> entry : stateByPlayer.entrySet())
        {
            PlayerData out = new PlayerData();
            out.unlocked = new ArrayList<>(entry.getValue().unlockedDrops);
            out.enabled = new ArrayList<>(entry.getValue().enabledDrops);
            data.players.put(entry.getKey().toString(), out);
        }

        try
        {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8))
            {
                GSON.toJson(data, writer);
            }
            dirty = false;
        }
        catch (IOException ignored)
        {
        }
    }

    private synchronized void load()
    {
        if (filePath == null || !Files.exists(filePath))
        {
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8))
        {
            SaveData data = GSON.fromJson(reader, SaveData.class);
            if (data == null || data.players == null)
            {
                return;
            }

            for (Map.Entry<String, PlayerData> entry : data.players.entrySet())
            {
                UUID playerId = parseUuid(entry.getKey());
                if (playerId == null)
                {
                    continue;
                }

                PlayerData raw = entry.getValue();
                OneBlockPlayerDropsState state = new OneBlockPlayerDropsState();
                if (raw != null)
                {
                    if (raw.unlocked != null)
                    {
                        state.unlockedDrops.addAll(raw.unlocked);
                    }
                    if (raw.enabled != null)
                    {
                        state.enabledDrops.addAll(raw.enabled);
                    }
                }

                ensureDefaults(state);
                stateByPlayer.put(playerId, state);
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private OneBlockPlayerDropsState state(UUID playerId)
    {
        if (playerId == null)
        {
            return null;
        }

        return stateByPlayer.computeIfAbsent(playerId, id ->
        {
            OneBlockPlayerDropsState s = new OneBlockPlayerDropsState();
            s.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
            s.enabledDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
            markDirty();
            return s;
        });
    }

    private static void ensureDefaults(OneBlockPlayerDropsState s)
    {
        if (s == null)
        {
            return;
        }

        s.unlockedDrops.removeIf(item -> item == null || item.isEmpty());
        s.enabledDrops.removeIf(item -> item == null || item.isEmpty());

        if (s.unlockedDrops.isEmpty())
        {
            s.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }
        if (s.enabledDrops.isEmpty())
        {
            s.enabledDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }
    }

    private void markDirty()
    {
        dirty = true;
        saveIfDirty();
    }

    private static UUID parseUuid(String raw)
    {
        if (raw == null || raw.isEmpty())
        {
            return null;
        }

        try
        {
            return UUID.fromString(raw);
        }
        catch (IllegalArgumentException ignored)
        {
            return null;
        }
    }

    private static final class SaveData
    {
        private final Map<String, PlayerData> players = new HashMap<>();
    }

    private static final class PlayerData
    {
        private List<String> unlocked;
        private List<String> enabled;
    }
}
