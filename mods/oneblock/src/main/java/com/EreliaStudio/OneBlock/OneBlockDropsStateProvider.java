package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OneBlockDropsStateProvider
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<UUID, OneBlockPlayerDropsState> stateByPlayer = new HashMap<>();
    private final Path filePath;
    private boolean dirty = false;

    public OneBlockDropsStateProvider(Path filePath)
    {
        this.filePath = filePath;
        load();
    }

    public synchronized List<String> getUnlockedDrops(UUID playerId, String expeditionId)
    {
        return getUnlockedDropsInternal(playerId, expeditionId);
    }

    public synchronized boolean isUnlocked(UUID playerId, String expeditionId, String dropItemId)
    {
        return isUnlockedInternal(playerId, expeditionId, dropItemId);
    }

    public synchronized boolean unlock(UUID playerId, String expeditionId, String dropItemId)
    {
        return unlockInternal(playerId, expeditionId, dropItemId);
    }

    public synchronized boolean lock(UUID playerId, String expeditionId, String dropItemId)
    {
        return lockInternal(playerId, expeditionId, dropItemId);
    }

    public List<String> getUnlockedDrops(UUID playerId)
    {
        return getUnlockedDrops(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION);
    }

    public boolean isUnlocked(UUID playerId, String dropItemId)
    {
        return isUnlocked(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);
    }

    public boolean unlock(UUID playerId, String dropItemId)
    {
        return unlock(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);
    }

    public boolean lock(UUID playerId, String dropItemId)
    {
        return lock(playerId, OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);
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
            for (Map.Entry<String, OneBlockPlayerExpeditionDropsState> expeditionEntry : entry.getValue().expeditions.entrySet())
            {
                ExpeditionData expedition = new ExpeditionData();
                expedition.unlocked = new ArrayList<>(expeditionEntry.getValue().unlockedDrops);
                out.expeditions.put(expeditionEntry.getKey(), expedition);
            }
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
                OneBlockPlayerDropsState playerState = new OneBlockPlayerDropsState();

                Map<String, ExpeditionData> expeditions = null;
                if (raw != null && raw.expeditions != null && !raw.expeditions.isEmpty())
                {
                    expeditions = raw.expeditions;
                }

                if (expeditions != null)
                {
                    for (Map.Entry<String, ExpeditionData> expeditionEntry : expeditions.entrySet())
                    {
                        String expeditionId = OneBlockExpeditionResolver.normalizeExpedition(expeditionEntry.getKey());
                        ExpeditionData rawExpedition = expeditionEntry.getValue();
                        OneBlockPlayerExpeditionDropsState expeditionState = new OneBlockPlayerExpeditionDropsState();
                        if (rawExpedition != null)
                        {
                            if (rawExpedition.unlocked != null)
                            {
                                expeditionState.unlockedDrops.addAll(rawExpedition.unlocked);
                            }
                            addLegacyEnabled(rawExpedition.enabled, expeditionState);
                        }
                        OneBlockExpeditionDefaults.ensureDefaults(expeditionId, expeditionState);
                        playerState.expeditions.put(expeditionId, expeditionState);
                    }
                }
                else if (raw != null && (raw.unlocked != null || raw.enabled != null))
                {
                    // Legacy format: map old data into the default expedition.
                    String expeditionId = OneBlockExpeditionResolver.DEFAULT_EXPEDITION;
                    OneBlockPlayerExpeditionDropsState expeditionState = new OneBlockPlayerExpeditionDropsState();
                    if (raw.unlocked != null)
                    {
                        expeditionState.unlockedDrops.addAll(raw.unlocked);
                    }
                    addLegacyEnabled(raw.enabled, expeditionState);
                    OneBlockExpeditionDefaults.ensureDefaults(expeditionId, expeditionState);
                    playerState.expeditions.put(expeditionId, expeditionState);
                }

                if (!playerState.expeditions.isEmpty())
                {
                    stateByPlayer.put(playerId, playerState);
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private void onStateChanged()
    {
        dirty = true;
        saveIfDirty();
    }

    private OneBlockPlayerExpeditionDropsState state(UUID playerId, String expeditionId)
    {
        if (playerId == null)
        {
            return null;
        }

        String expeditionKey = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);
        OneBlockPlayerDropsState playerState = stateByPlayer.computeIfAbsent(playerId, id -> new OneBlockPlayerDropsState());
        return playerState.expeditions.computeIfAbsent(expeditionKey, key ->
        {
            OneBlockPlayerExpeditionDropsState s = new OneBlockPlayerExpeditionDropsState();
            OneBlockExpeditionDefaults.ensureDefaults(expeditionKey, s);
            onStateChanged();
            return s;
        });
    }

    private List<String> getUnlockedDropsInternal(UUID playerId, String expeditionId)
    {
        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        if (s == null || s.unlockedDrops.isEmpty())
        {
            return new ArrayList<>(OneBlockExpeditionDefaults.getDefaultDropIds(expeditionId));
        }

        return new ArrayList<>(s.unlockedDrops);
    }

    private boolean isUnlockedInternal(UUID playerId, String expeditionId, String dropItemId)
    {
        if (isBlank(dropItemId))
        {
            return false;
        }

        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        return s != null && s.unlockedDrops.contains(dropItemId);
    }

    private boolean unlockInternal(UUID playerId, String expeditionId, String dropItemId)
    {
        if (isBlank(dropItemId))
        {
            return false;
        }

        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        if (s == null || s.unlockedDrops.contains(dropItemId))
        {
            return false;
        }

        s.unlockedDrops.add(dropItemId);
        onStateChanged();
        return true;
    }

    private boolean lockInternal(UUID playerId, String expeditionId, String dropItemId)
    {
        if (isBlank(dropItemId))
        {
            return false;
        }

        if (OneBlockExpeditionDefaults.isDefaultDrop(expeditionId, dropItemId))
        {
            return false;
        }

        OneBlockPlayerExpeditionDropsState s = state(playerId, expeditionId);
        if (s == null)
        {
            return false;
        }

        boolean removed = s.unlockedDrops.remove(dropItemId);
        OneBlockExpeditionDefaults.ensureDefaults(expeditionId, s);

        if (removed)
        {
            onStateChanged();
        }

        return removed;
    }

    private static boolean isBlank(String value)
    {
        return value == null || value.isEmpty();
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
        private Map<String, ExpeditionData> expeditions = new HashMap<>();

        // Legacy fields
        private List<String> unlocked;
        private List<String> enabled;
    }

    private static final class ExpeditionData
    {
        private List<String> unlocked;
        private List<String> enabled;
    }

    private static void addLegacyEnabled(List<String> enabled, OneBlockPlayerExpeditionDropsState expeditionState)
    {
        if (enabled != null && expeditionState != null)
        {
            expeditionState.unlockedDrops.addAll(enabled);
        }
    }
}
