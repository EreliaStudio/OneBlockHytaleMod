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
    public synchronized List<String> getEnabledDrops(UUID playerId, String chapterId)
    {
        OneBlockPlayerChapterDropsState s = state(playerId, chapterId);
        if (s == null || s.enabledDrops.isEmpty())
        {
            return new ArrayList<>(OneBlockChapterDefaults.getDefaultDropIds(chapterId));
        }

        return new ArrayList<>(s.enabledDrops);
    }

    @Override
    public synchronized boolean isUnlocked(UUID playerId, String chapterId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerChapterDropsState s = state(playerId, chapterId);
        return s != null && s.unlockedDrops.contains(dropItemId);
    }

    @Override
    public synchronized boolean unlock(UUID playerId, String chapterId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerChapterDropsState s = state(playerId, chapterId);
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
    public synchronized boolean lock(UUID playerId, String chapterId, String dropItemId)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        if (OneBlockChapterDefaults.isDefaultDrop(chapterId, dropItemId))
        {
            return false;
        }

        OneBlockPlayerChapterDropsState s = state(playerId, chapterId);
        if (s == null)
        {
            return false;
        }

        boolean removed = s.unlockedDrops.remove(dropItemId);
        s.enabledDrops.remove(dropItemId);
        ensureDefaults(chapterId, s);

        if (removed)
        {
            markDirty();
        }

        return removed;
    }

    @Override
    public synchronized boolean setEnabled(UUID playerId, String chapterId, String dropItemId, boolean enabled)
    {
        if (dropItemId == null || dropItemId.isEmpty())
        {
            return false;
        }

        OneBlockPlayerChapterDropsState s = state(playerId, chapterId);
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
            ensureDefaults(chapterId, s);
        }

        if (changed)
        {
            markDirty();
        }

        return true;
    }

    public synchronized void resetEnabledToUnlocked(UUID playerId, String chapterId)
    {
        OneBlockPlayerChapterDropsState s = state(playerId, chapterId);
        if (s == null)
        {
            return;
        }

        s.enabledDrops.clear();
        s.enabledDrops.addAll(s.unlockedDrops);
        if (s.enabledDrops.isEmpty())
        {
            s.enabledDrops.addAll(OneBlockChapterDefaults.getDefaultDropIds(chapterId));
        }

        markDirty();
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
            for (Map.Entry<String, OneBlockPlayerChapterDropsState> chapterEntry : entry.getValue().chapters.entrySet())
            {
                ChapterData chapter = new ChapterData();
                chapter.unlocked = new ArrayList<>(chapterEntry.getValue().unlockedDrops);
                chapter.enabled = new ArrayList<>(chapterEntry.getValue().enabledDrops);
                out.chapters.put(chapterEntry.getKey(), chapter);
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

                if (raw != null && raw.chapters != null && !raw.chapters.isEmpty())
                {
                    for (Map.Entry<String, ChapterData> chapterEntry : raw.chapters.entrySet())
                    {
                        String chapterId = normalizeChapter(chapterEntry.getKey());
                        ChapterData rawChapter = chapterEntry.getValue();
                        OneBlockPlayerChapterDropsState chapterState = new OneBlockPlayerChapterDropsState();
                        if (rawChapter != null)
                        {
                            if (rawChapter.unlocked != null)
                            {
                                chapterState.unlockedDrops.addAll(rawChapter.unlocked);
                            }
                            if (rawChapter.enabled != null)
                            {
                                chapterState.enabledDrops.addAll(rawChapter.enabled);
                            }
                        }
                        ensureDefaults(chapterId, chapterState);
                        playerState.chapters.put(chapterId, chapterState);
                    }
                }
                else if (raw != null && (raw.unlocked != null || raw.enabled != null))
                {
                    // Legacy format: map old data into the default chapter.
                    String chapterId = OneBlockChapterResolver.DEFAULT_CHAPTER;
                    OneBlockPlayerChapterDropsState chapterState = new OneBlockPlayerChapterDropsState();
                    if (raw.unlocked != null)
                    {
                        chapterState.unlockedDrops.addAll(raw.unlocked);
                    }
                    if (raw.enabled != null)
                    {
                        chapterState.enabledDrops.addAll(raw.enabled);
                    }
                    ensureDefaults(chapterId, chapterState);
                    playerState.chapters.put(chapterId, chapterState);
                }

                if (!playerState.chapters.isEmpty())
                {
                    stateByPlayer.put(playerId, playerState);
                }
            }
        }
        catch (Exception ignored)
        {
        }
    }

    private OneBlockPlayerChapterDropsState state(UUID playerId, String chapterId)
    {
        if (playerId == null)
        {
            return null;
        }

        String chapterKey = normalizeChapter(chapterId);
        OneBlockPlayerDropsState playerState = stateByPlayer.computeIfAbsent(playerId, id -> new OneBlockPlayerDropsState());
        return playerState.chapters.computeIfAbsent(chapterKey, key ->
        {
            OneBlockPlayerChapterDropsState s = new OneBlockPlayerChapterDropsState();
            OneBlockChapterDefaults.ensureDefaults(chapterKey, s);
            markDirty();
            return s;
        });
    }

    private static void ensureDefaults(String chapterId, OneBlockPlayerChapterDropsState s)
    {
        OneBlockChapterDefaults.ensureDefaults(chapterId, s);
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
        private Map<String, ChapterData> chapters = new HashMap<>();

        // Legacy fields
        private List<String> unlocked;
        private List<String> enabled;
    }

    private static final class ChapterData
    {
        private List<String> unlocked;
        private List<String> enabled;
    }

    private static String normalizeChapter(String chapterId)
    {
        if (chapterId == null || chapterId.isEmpty())
        {
            return OneBlockChapterResolver.DEFAULT_CHAPTER;
        }

        return chapterId;
    }
}
