package com.EreliaStudio.OneBlockAchievement;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AchievementCatalog {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    private final boolean bundledCatalogIsAuthoritative;
    private volatile Map<String, AchievementDefinition> achievements = Map.of();

    AchievementCatalog(Path path) { this(path, false); }

    AchievementCatalog(Path path, boolean bundledCatalogIsAuthoritative) {
        this.path = path;
        this.bundledCatalogIsAuthoritative = bundledCatalogIsAuthoritative;
    }

    synchronized void load() throws IOException {
        installBundledCatalog(bundledCatalogIsAuthoritative);
        CatalogFile file;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            file = GSON.fromJson(reader, CatalogFile.class);
        }
        if (file == null || file.schemaVersion != 1 || file.achievements == null)
            throw new IOException("achievements.json must use schemaVersion 1 and contain an achievements array");

        LinkedHashMap<String, AchievementDefinition> validated = new LinkedHashMap<>();
        for (AchievementDefinition achievement : file.achievements) {
            normalize(achievement);
            if (validated.putIfAbsent(achievement.id, achievement) != null)
                throw new IOException("Duplicate achievement id: " + achievement.id);
        }
        for (AchievementDefinition achievement : validated.values()) {
            for (String prerequisite : achievement.prerequisites)
                if (!validated.containsKey(prerequisite))
                    throw new IOException("Achievement " + achievement.id + " has unknown prerequisite " + prerequisite);
            detectCycle(achievement.id, validated, new LinkedHashSet<>(), new LinkedHashSet<>());
        }
        achievements = Collections.unmodifiableMap(new LinkedHashMap<>(validated));
    }

    AchievementDefinition get(String id) { return id == null ? null : achievements.get(id); }
    List<AchievementDefinition> all() { return List.copyOf(achievements.values()); }
    Path path() { return path; }

    private void installBundledCatalog(boolean replaceExisting) throws IOException {
        if (!replaceExisting && Files.exists(path)) return;
        Files.createDirectories(path.getParent());
        try (InputStream source = AchievementCatalog.class.getResourceAsStream("/achievements.json")) {
            if (source == null) throw new IOException("Bundled achievements.json is missing");
            Files.copy(source, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void normalize(AchievementDefinition achievement) throws IOException {
        if (achievement == null || achievement.id == null || !achievement.id.matches("[a-z0-9][a-z0-9_-]*"))
            throw new IOException("Every achievement needs a lowercase id containing only a-z, 0-9, _ or -");
        if (achievement.name == null || achievement.name.isBlank()) throw new IOException("Achievement " + achievement.id + " needs a name");
        if (achievement.title == null || achievement.title.isBlank()) throw new IOException("Achievement " + achievement.id + " needs a title");
        if (achievement.title.length() > 64 || achievement.title.indexOf('\n') >= 0 || achievement.title.indexOf('\r') >= 0)
            throw new IOException("Achievement " + achievement.id + " title must be one line and at most 64 characters");
        if (achievement.prerequisites == null) achievement.prerequisites = new ArrayList<>();
        if (achievement.cost == null) achievement.cost = new AchievementDefinition.Cost();
        if (achievement.cost.items == null) achievement.cost.items = new ArrayList<>();
        if (achievement.cost.expeditions == null) achievement.cost.expeditions = new ArrayList<>();
        if (achievement.cost.currency < 0) throw new IOException("Achievement " + achievement.id + " has a negative currency cost");
        Set<String> itemIds = new LinkedHashSet<>();
        for (AchievementDefinition.ItemCost item : achievement.cost.items) {
            if (item == null || item.id == null || item.id.isBlank() || item.quantity <= 0)
                throw new IOException("Achievement " + achievement.id + " has an invalid item cost");
            if (!itemIds.add(item.id)) throw new IOException("Achievement " + achievement.id + " repeats item " + item.id);
        }
        if (new LinkedHashSet<>(achievement.cost.expeditions).size() != achievement.cost.expeditions.size())
            throw new IOException("Achievement " + achievement.id + " repeats expedition knowledge");
    }

    private static void detectCycle(String id, Map<String, AchievementDefinition> achievements, Set<String> visiting, Set<String> done)
            throws IOException {
        if (done.contains(id)) return;
        if (!visiting.add(id)) throw new IOException("Prerequisite cycle contains " + id);
        for (String parent : achievements.get(id).prerequisites) detectCycle(parent, achievements, visiting, done);
        visiting.remove(id);
        done.add(id);
    }

    private static final class CatalogFile {
        int schemaVersion;
        List<AchievementDefinition> achievements;
    }
}
