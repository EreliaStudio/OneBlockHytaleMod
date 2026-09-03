package com.EreliaStudio.OneBlockAchievement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class PlayerProgressStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    private SaveData data = new SaveData();

    PlayerProgressStore(Path path) { this.path = path; }

    synchronized void load() throws IOException {
        if (!Files.exists(path)) return;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            SaveData loaded = GSON.fromJson(reader, SaveData.class);
            data = loaded == null ? new SaveData() : loaded;
            if (data.players == null) data.players = new LinkedHashMap<>();
        }
    }

    synchronized PlayerData player(UUID id) {
        PlayerData result = data.players.computeIfAbsent(id.toString(), ignored -> new PlayerData());
        result.normalize();
        for (AchievementProgress progress : result.progress.values()) if (progress != null) progress.normalize();
        return result;
    }

    synchronized void mutate(UUID id, java.util.function.Consumer<PlayerData> mutation) {
        mutation.accept(player(id));
        save();
    }

    private void save() {
        try {
            Files.createDirectories(path.getParent());
            Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                GSON.toJson(data, writer);
            }
            try {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException unsupported) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException error) {
            throw new IllegalStateException("Could not save achievement progress: " + error.getMessage(), error);
        }
    }

    static final class PlayerData {
        @SerializedName(value = "unlocked", alternate = "completed")
        Set<String> unlocked = new LinkedHashSet<>();
        String activeTitle;
        Set<String> knownExpeditions = new LinkedHashSet<>();
        Map<String, AchievementProgress> progress = new LinkedHashMap<>();

        void normalize() {
            if (unlocked == null) unlocked = new LinkedHashSet<>();
            if (knownExpeditions == null) knownExpeditions = new LinkedHashSet<>();
            if (progress == null) progress = new LinkedHashMap<>();
        }
    }

    static final class AchievementProgress {
        long currency;
        Map<String, Integer> items = new LinkedHashMap<>();

        void normalize() { if (items == null) items = new LinkedHashMap<>(); }
    }

    private static final class SaveData {
        int schemaVersion = 1;
        Map<String, PlayerData> players = new LinkedHashMap<>();
    }
}
