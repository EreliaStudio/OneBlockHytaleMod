package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** Generated from expeditions.json by the same calculations used by the wiki and runtime. */
public final class ExpeditionCatalog {
    public record Loot(String id, int quantity, double chance, double expected, String kind) {
        public boolean creature() { return id.startsWith("entity:"); }
    }
    public record Edge(String target, double chance) {}
    public record Mob(String icon, String nameKey) {}
    public record Expedition(String id, String name, String crystal, String category, int tier,
                             int duration, String tool, boolean dungeon, List<Loot> drops,
                             List<Loot> rewards, List<List<String>> waves, List<Edge> children) {
        public Expedition {
            drops = List.copyOf(drops); rewards = List.copyOf(rewards);
            waves = waves.stream().map(List::copyOf).toList(); children = List.copyOf(children);
        }
    }
    public record Source(Expedition expedition, Loot loot) {}
    private record Document(int version, List<Expedition> expeditions, Map<String, Mob> mobs) {}
    private final Map<String, Expedition> expeditions;
    private final Map<String, List<Edge>> incoming = new LinkedHashMap<>();
    private final Map<String, List<Source>> reverse = new TreeMap<>();
    private final Map<String, Mob> mobs;
    private final Map<String, Integer> depths = new HashMap<>();

    public static ExpeditionCatalog load() {
        var stream = ExpeditionCatalog.class.getResourceAsStream("/ExpeditionAtlas.json");
        if (stream == null) throw new IllegalStateException("Missing generated ExpeditionAtlas.json");
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return read(reader);
        } catch (java.io.IOException error) { throw new IllegalStateException(error); }
    }
    static ExpeditionCatalog read(Reader reader) {
        Document document = new Gson().fromJson(reader, Document.class);
        if (document.version != 1) throw new IllegalArgumentException("Unsupported Atlas catalogue version");
        return new ExpeditionCatalog(document.expeditions, document.mobs);
    }
    ExpeditionCatalog(List<Expedition> definitions, Map<String, Mob> mobs) {
        Map<String, Expedition> values = new LinkedHashMap<>();
        for (var e : definitions) {
            if (values.putIfAbsent(e.id, e) != null) throw new IllegalArgumentException("Duplicate expedition " + e.id);
        }
        expeditions = Collections.unmodifiableMap(values);
        this.mobs = Map.copyOf(mobs);
        for (var e : definitions) {
            Set<String> ids = new HashSet<>();
            for (var drop : e.drops) {
                if (!ids.add(drop.id) || drop.quantity < 1 || !Double.isFinite(drop.chance) || drop.chance <= 0)
                    throw new IllegalArgumentException("Invalid normalized drop in " + e.id);
                index(e, drop);
            }
            e.rewards.forEach(r -> index(e, r));
            Map<String, Integer> enemies = new LinkedHashMap<>();
            e.waves.forEach(w -> w.forEach(m -> enemies.merge("entity:" + m.replace("entity:", ""), 1, Integer::sum)));
            enemies.forEach((id, count) -> index(e, new Loot(id, count, 100, count, "wave")));
            for (var edge : e.children) {
                if (!values.containsKey(edge.target) || !Double.isFinite(edge.chance) || edge.chance <= 0 || edge.chance > 100)
                    throw new IllegalArgumentException("Invalid route from " + e.id);
                incoming.computeIfAbsent(edge.target, k -> new ArrayList<>()).add(new Edge(e.id, edge.chance));
            }
        }
        incoming.replaceAll((k,v) -> List.copyOf(v));
        reverse.replaceAll((k,v) -> List.copyOf(v));
        // Longest path through the DAG; a cycle is a catalogue error, not an infinite UI traversal.
        for (String id : values.keySet()) depth(id, new HashSet<>());
    }
    private int depth(String id, Set<String> visiting) {
        if (depths.containsKey(id)) return depths.get(id);
        if (!visiting.add(id)) throw new IllegalArgumentException("Progression cycle: " + id);
        int depth = parents(id).stream().mapToInt(e -> depth(e.target, visiting) + 1).max().orElse(0);
        visiting.remove(id); depths.put(id, depth); return depth;
    }
    private void index(Expedition e, Loot loot) { reverse.computeIfAbsent(loot.id, k -> new ArrayList<>()).add(new Source(e, loot)); }
    public Collection<Expedition> all() { return expeditions.values(); }
    public Expedition get(String id) { return expeditions.get(id); }
    public List<Edge> parents(String id) { return incoming.getOrDefault(id, List.of()); }
    public Set<String> lootIds() { return Collections.unmodifiableSet(reverse.keySet()); }
    public Mob mob(String id) { return mobs.get(id.replace("entity:", "")); }
    public List<Source> sources(String id) { return reverse.getOrDefault(id, List.of()); }
    public int dropQuantity(String expedition, String drop) {
        var e = get(expedition);
        return e == null ? 1 : e.drops.stream().filter(d -> d.id.equals(drop)).mapToInt(Loot::quantity).findFirst().orElse(1);
    }
    public long discovered(Expedition e, Predicate<Expedition> unlocked) {
        return e.children.stream().filter(edge -> unlocked.test(get(edge.target))).count();
    }
    public List<Expedition> frontier(Predicate<Expedition> unlocked, Function<Expedition, String> name) {
        return all().stream().filter(unlocked)
                .filter(e -> e.children.stream().anyMatch(edge -> !unlocked.test(get(edge.target))))
                .sorted(Comparator.<Expedition>comparingInt(e -> depths.get(e.id)).reversed()
                        .thenComparing(name, String.CASE_INSENSITIVE_ORDER).thenComparing(Expedition::id)).toList();
    }
    public static Comparator<Source> sourceOrder(Predicate<Expedition> unlocked, Predicate<Expedition> affordable,
                                                Function<Expedition, String> name) {
        return Comparator.<Source>comparingInt(s -> !unlocked.test(s.expedition) ? 2 : affordable.test(s.expedition) ? 0 : 1)
                .thenComparing(Comparator.comparingDouble((Source s) -> s.loot.expected).reversed())
                .thenComparing(Comparator.comparingDouble((Source s) -> s.loot.chance).reversed())
                .thenComparing(s -> name.apply(s.expedition), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(s -> s.expedition.id);
    }
}
