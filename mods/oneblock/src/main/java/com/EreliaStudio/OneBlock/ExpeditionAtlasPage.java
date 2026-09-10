package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.ui.Anchor;
import com.hypixel.hytale.server.core.ui.PatchStyle;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Interactive, server-populated Atlas. IDs only select entries from the validated catalogue. */
final class ExpeditionAtlasPage extends InteractiveCustomUIPage<ExpeditionAtlasPage.AtlasEvent> {
    private final PlayerRef playerRef;
    private final ExpeditionCatalog catalog;
    private final AtlasCraftingService crafting;
    private final AtlasBenchWindow window;
    private String tab = "Explore", query = "", lootId = "", selected = "";
    private int quantity = 1, stateFilter = 0, tier = 0, category = 0;
    private boolean fullLoot;
    private final List<String> categories;
    private final Map<String, String> resultSelectors = new LinkedHashMap<>();
    private final Set<String> navigable = new HashSet<>();
    private String notice = "";

    ExpeditionAtlasPage(PlayerRef playerRef, ExpeditionCatalog catalog, AtlasBenchWindow window) {
        super(playerRef, CustomPageLifetime.CanDismiss, AtlasEvent.CODEC);
        this.playerRef = playerRef; this.catalog = catalog; this.window = window;
        this.crafting = new AtlasCraftingService(window);
        categories = catalog.all().stream().map(ExpeditionCatalog.Expedition::category).distinct().sorted().toList();
    }
    @Override public void build(Ref<EntityStore> ref, UICommandBuilder commands, UIEventBuilder events, Store<EntityStore> store) {
        commands.append("OneBlockAtlasPage.ui");
        for (String name : List.of("Explore", "Expeditions", "Loot", "Dungeons")) bind(events, "#" + name, "tab", name);
        bind(events, "#Close", "close", "");
        bind(events, "#StateFilter", "state", ""); bind(events, "#TierFilter", "tier", "");
        bind(events, "#CategoryFilter", "category", ""); bind(events, "#ToggleLoot", "full", "");
        bind(events, "#Refresh", "refresh", "");
        events.addEventBinding(CustomUIEventBindingType.ValueChanged, "#Search",
                EventData.of("Action", "search").append("@Query", "#Search.Value"), false);
        render(ref, store, commands, events);
    }
    private void render(Ref<EntityStore> ref, Store<EntityStore> store, UICommandBuilder c, UIEventBuilder events) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        crafting.invalidate(); navigable.clear(); resultSelectors.clear();
        Predicate<ExpeditionCatalog.Expedition> unlocked = e -> crafting.unlocked(player, e);
        var materials = crafting.materials(ref, store);
        Predicate<ExpeditionCatalog.Expedition> affordable = e -> crafting.affordable(player, e, materials, 1);
        for (String name : List.of("Explore", "Expeditions", "Loot", "Dungeons"))
            c.set("#" + name + ".Style", Value.ref("OneBlockAtlasStyles.ui", tab.equals(name) ? "GoldButton" : "Button"));
        c.set("#Filters.Visible", tab.equals("Expeditions") || tab.equals("Dungeons"));
        c.set("#StateFilter.Text", t("filter." + List.of("all", "unlocked", "locked", "affordable").get(stateFilter)));
        c.set("#TierFilter.Text", tier == 0 ? t("filter.tiers") : f("tier", "tier", tier));
        c.set("#CategoryFilter.Text", category == 0 ? t("filter.categories") : categoryName(categories.get(category - 1)));
        c.clear("#Results");
        boolean lootBrowser = tab.equals("Loot") && query.isBlank() && lootId.isEmpty();
        List<ExpeditionCatalog.Expedition> results;
        Map<String, ExpeditionCatalog.Source> sourceInfo = new LinkedHashMap<>();
        if (lootBrowser) {
            var ids = catalog.lootIds().stream().sorted(Comparator.comparing(this::lootName, String.CASE_INSENSITIVE_ORDER).thenComparing(i -> i)).toList();
            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i), row = "#Results[" + i + "]";
                c.append("#Results", "OneBlockAtlasLootSlot.ui");
                icon(c, row, id); c.set(row + " #Name.Text", lootName(id));
                c.set(row + " #Info.Text", t(id.startsWith("entity:") ? "creature" : "item") + " · "
                        + f("sources", "count", catalog.sources(id).stream().map(s -> s.expedition().id()).distinct().count()));
                c.set(row + ".TooltipText", lootName(id)); bind(events, row, "loot", id);
            }
            c.set("#ResultsHeading.Text", t("tab.loot")); c.set("#Empty.Visible", ids.isEmpty());
            c.set("#ResultsCount.Text", f("results", "count", ids.size()));
            results = List.of();
        } else {
            if (!query.isBlank() || !lootId.isEmpty()) {
                String needle = fold(query);
                Stream<String> lootMatches = !lootId.isEmpty() ? Stream.of(lootId) : catalog.lootIds().stream()
                        .filter(id -> matches(lootName(id), needle) || matches(id, needle));
                lootMatches.flatMap(id -> catalog.sources(id).stream())
                        .sorted(ExpeditionCatalog.sourceOrder(unlocked, affordable, this::name))
                        .forEach(s -> sourceInfo.putIfAbsent(s.expedition().id(), s));
                List<ExpeditionCatalog.Expedition> matches = new ArrayList<>(sourceInfo.values().stream().map(ExpeditionCatalog.Source::expedition).toList());
                if (lootId.isEmpty()) catalog.all().stream().filter(e -> !sourceInfo.containsKey(e.id())
                        && (matches(name(e), needle) || matches(e.id(), needle))).forEach(matches::add);
                results = matches.stream().sorted(Comparator.<ExpeditionCatalog.Expedition>comparingInt(e -> !unlocked.test(e) ? 2 : affordable.test(e) ? 0 : 1)
                        .thenComparing(Comparator.comparingDouble((ExpeditionCatalog.Expedition e) -> sourceInfo.containsKey(e.id()) ? sourceInfo.get(e.id()).loot().expected() : 0).reversed())
                        .thenComparing(this::name, String.CASE_INSENSITIVE_ORDER).thenComparing(ExpeditionCatalog.Expedition::id)).toList();
            } else if (tab.equals("Explore")) results = catalog.frontier(unlocked, this::name);
            else results = catalog.all().stream().filter(e -> e.dungeon() == tab.equals("Dungeons"))
                    .filter(e -> switch (stateFilter) { case 1 -> unlocked.test(e); case 2 -> !unlocked.test(e); case 3 -> unlocked.test(e) && affordable.test(e); default -> true; })
                    .filter(e -> tier == 0 || tier == e.tier())
                    .filter(e -> category == 0 || categories.get(category - 1).equals(e.category()))
                    .sorted(Comparator.comparing(this::name, String.CASE_INSENSITIVE_ORDER).thenComparing(ExpeditionCatalog.Expedition::id)).toList();
            c.set("#ResultsHeading.Text", !query.isBlank() || !lootId.isEmpty() ? t("searchResults") : t(tab.equals("Explore") ? "frontier" : "tab." + tab.toLowerCase(Locale.ROOT)));
            c.set("#Empty.Visible", results.isEmpty());
            c.set("#Empty.Text", t(tab.equals("Explore") && query.isBlank() ? "empty.frontier" : "empty.search"));
            c.set("#ResultsCount.Text", f("results", "count", results.size()));
            for (int i = 0; i < results.size(); i++) {
                var e = results.get(i); String row = "#Results[" + i + "]";
                c.append("#Results", "OneBlockAtlasExpeditionListItem.ui");
                c.set(row + " #Icon.ItemId", e.crystal()); c.set(row + " #Name.Text", name(e));
                var source = sourceInfo.get(e.id());
                c.set(row + ".TooltipText", name(e) + (source == null ? "" : "\n" + lootName(source.loot().id()) + "\n"
                        + (source.loot().kind().equals("block")
                        ? f("yield", "chance", percent(source.loot().chance()), "duration", e.duration(), "expected", decimal(source.loot().expected()))
                        : f("rewardChance", "count", source.loot().quantity(), "chance", percent(source.loot().chance())))));
                c.set(row + " #Selected.Visible", e.id().equals(selected));
                c.set(row + " #Dim.Visible", !unlocked.test(e));
                String subtitle = sourceInfo.containsKey(e.id()) ? lootName(sourceInfo.get(e.id()).loot().id()) + " · " + percent(sourceInfo.get(e.id()).loot().chance())
                        : tab.equals("Explore") ? f("discovered", "found", catalog.discovered(e, unlocked), "total", e.children().size())
                        : categoryName(e.category()) + " · " + f("tier", "tier", e.tier());
                c.set(row + " #Subtitle.Text", subtitle);
                c.set(row + " #State.Text", stateLabel(ref, store, player, e, materials));
                c.set(row + " #State.Style.TextColor", unlocked.test(e) ? affordable.test(e) ? "#8DDBA2" : "#D3BA84" : "#7F8D9F");
                bind(events, row, "select", e.id()); navigable.add(e.id()); resultSelectors.put(e.id(), row);
            }
        }
        if (selected.isEmpty() && !results.isEmpty()) selected = results.getFirst().id();
        renderDetail(ref, store, player, c, events);
    }
    private void renderDetail(Ref<EntityStore> ref, Store<EntityStore> store, Player player, UICommandBuilder c, UIEventBuilder events) {
        var e = catalog.get(selected);
        c.set("#NoSelection.Visible", e == null); c.set("#Detail.Visible", e != null); c.set("#CraftPanel.Visible", e != null);
        if (e == null) return;
        c.set("#DetailIcon.ItemId", e.crystal()); c.set("#DetailName.Text", name(e));
        c.set("#Facts.Text", categoryName(e.category()) + " · " + f("tier", "tier", e.tier()) + " · "
                + f(e.dungeon() ? "waves" : "blocks", "count", e.dungeon() ? e.waves().size() : e.duration()) + " · " + t("tool." + e.tool()));
        c.set("#UnlockState.Text", t(crafting.unlocked(player, e) ? "state.unlocked" : "state.locked"));
        c.set("#LootHeading.Text", t(e.dungeon() ? "enemies" : "featured"));
        c.clear("#Featured"); c.clear("#FullLoot");
        List<ExpeditionCatalog.Loot> drops = e.drops();
        if (e.dungeon()) {
            var waveDrops = new ArrayList<ExpeditionCatalog.Loot>();
            for (int wave = 0; wave < e.waves().size(); wave++) {
                Map<String, Integer> counts = new LinkedHashMap<>();
                e.waves().get(wave).forEach(m -> counts.merge("entity:" + m.replace("entity:", ""), 1, Integer::sum));
                int waveNumber = wave + 1;
                counts.forEach((id, count) -> waveDrops.add(new ExpeditionCatalog.Loot(id, count, 100, count, "wave:" + waveNumber)));
            }
            drops = waveDrops;
        }
        var featured = drops.stream().sorted(Comparator.comparingDouble(ExpeditionCatalog.Loot::chance).reversed()).limit(5).toList();
        for (int i = 0; i < featured.size(); i++) renderLoot("#Featured", i, featured.get(i), true, e, c, events);
        for (int i = 0; i < drops.size(); i++) renderLoot("#FullLoot", i, drops.get(i), false, e, c, events);
        c.set("#FullLoot.Visible", fullLoot); c.set("#ToggleLoot.Text", t(fullLoot ? "hideLoot" : "fullLoot"));
        renderRoute(e, player, c, events);
        c.clear("#Rewards");
        for (int i = 0; i < e.rewards().size(); i++) renderLoot("#Rewards", i, e.rewards().get(i), false, e, c, events);
        renderCraft(ref, store, player, c, events);
        resultSelectors.forEach((id, row) -> c.set(row + " #Selected.Visible", id.equals(selected)));
    }
    private void renderLoot(String container, int index, ExpeditionCatalog.Loot loot, boolean featured,
                            ExpeditionCatalog.Expedition e, UICommandBuilder c, UIEventBuilder events) {
        String row = container + "[" + index + "]";
        c.append(container, featured ? "OneBlockAtlasFeaturedLoot.ui" : "OneBlockAtlasLootSlot.ui");
        icon(c, row, loot.id());
        String info = loot.kind().startsWith("wave:") ? f("wave", "wave", loot.kind().substring(5), "count", loot.quantity())
                : loot.kind().equals("reward") ? f("rewardChance", "count", loot.quantity(), "chance", percent(loot.chance()))
                : f("yield", "chance", percent(loot.chance()), "duration", e.duration(), "expected", decimal(loot.expected()));
        if (!featured) c.set(row + " #Name.Text", lootName(loot.id()));
        c.set(row + " #Info.Text", featured ? loot.kind().startsWith("wave:") ? "×" + loot.quantity() : percent(loot.chance()) : info);
        c.set(row + ".TooltipText", lootName(loot.id()) + " · " + t(loot.creature() ? "creature" : "item") + "\n" + info);
        bind(events, row, "loot", loot.id());
    }
    private void renderRoute(ExpeditionCatalog.Expedition e, Player player, UICommandBuilder c, UIEventBuilder events) {
        c.clear("#Parents"); c.clear("#Current"); c.clear("#Children");
        var parents = catalog.parents(e.id());
        Anchor anchor = new Anchor(); anchor.setHeight(Value.of(Math.max(1, Math.max(parents.size(), e.children().size())) * 113));
        c.setObject("#Route.Anchor", anchor);
        for (int i = 0; i < parents.size(); i++) routeNode("#Parents", i, parents.get(i), player, c, events);
        routeNode("#Current", 0, new ExpeditionCatalog.Edge(e.id(), 0), player, c, events);
        for (int i = 0; i < e.children().size(); i++) routeNode("#Children", i, e.children().get(i), player, c, events);
    }
    private void routeNode(String container, int i, ExpeditionCatalog.Edge edge, Player player, UICommandBuilder c, UIEventBuilder events) {
        var e = catalog.get(edge.target()); String row = container + "[" + i + "]";
        c.append(container, "OneBlockAtlasRouteNode.ui");
        c.set(row + " #Icon.ItemId", e.crystal()); c.set(row + " #Name.Text", name(e));
        boolean locked = !crafting.unlocked(player, e);
        c.set(row + " #Dim.Visible", locked); c.set(row + " #Lock.Visible", locked);
        c.set(row + " #Selected.Visible", selected.equals(e.id()));
        c.set(row + " #Chance.Text", edge.chance() == 0 ? "" : percent(edge.chance()));
        c.set(row + ".TooltipText", name(e) + " · " + t(locked ? "state.locked" : "state.unlocked")
                + (edge.chance() == 0 ? "" : " · " + f("unlockChance", "chance", percent(edge.chance()))));
        bind(events, row, "select", e.id()); navigable.add(e.id());
    }
    private void renderCraft(Ref<EntityStore> ref, Store<EntityStore> store, Player player, UICommandBuilder c, UIEventBuilder events) {
        var e = catalog.get(selected); if (e == null) return;
        c.set("#CraftIcon.ItemId", e.crystal()); c.set("#Quantity.Text", Integer.toString(quantity));
        c.set("#Minus.Disabled", quantity <= 1); c.set("#Plus.Disabled", quantity >= AtlasCraftingService.MAX_QUANTITY);
        c.clear("#Costs"); var recipe = AtlasCraftingService.recipe(e); var materials = crafting.materials(ref, store);
        if (recipe != null && recipe.getInput() != null) {
            for (int i = 0; i < recipe.getInput().length; i++) {
                var cost = recipe.getInput()[i]; String row = "#Costs[" + i + "]";
                c.append("#Costs", "OneBlockAtlasLootSlot.ui");
                if (cost.getItemId() != null) icon(c, row, cost.getItemId());
                c.set(row + " #Name.Text", cost.getItemId() == null ? cost.getResourceTypeId() : lootName(cost.getItemId()));
                int owned = materials.countRemovableMaterial(cost);
                long required = (long) cost.getQuantity() * quantity;
                c.set(row + " #Info.Text", owned + " / " + required);
                c.set(row + " #Info.Style.TextColor", owned >= required ? "#8DDBA2" : "#D3BA84");
                c.set(row + ".TooltipText", t("cost"));
            }
        }
        var state = crafting.state(ref, store, player, e, quantity);
        c.set("#Craft.Disabled", state != AtlasCraftingService.State.ready);
        c.set("#CraftState.Text", (notice.isEmpty() ? "" : t("craftState." + notice) + "\n") + t("craftState." + state.name()));
        for (String action : List.of("Craft", "Minus", "Plus")) events.addEventBinding(CustomUIEventBindingType.Activating,
                "#" + action, EventData.of("Action", action.toLowerCase(Locale.ROOT)).append("Revision", crafting.revision()), false);
        // Affordability changed: patch badges in place; preserve route, loot, search and scroll positions.
        resultSelectors.forEach((id, row) -> c.set(row + " #State.Text", stateLabel(ref, store, player, catalog.get(id), materials)));
    }
    private String stateLabel(Ref<EntityStore> ref, Store<EntityStore> store, Player player, ExpeditionCatalog.Expedition e,
                              com.hypixel.hytale.server.core.inventory.container.ItemContainer materials) {
        var plugin = OneBlockPlugin.getInstance(); var world = store.getExternalData().getWorld();
        var owner = plugin.resolveOwner(world, playerRef.getUuid());
        if (plugin.getRootRegistry().hasRoots(world, owner)) {
            String active = e.dungeon() ? plugin.getRootRegistry().dungeonState(world, owner).getActiveDungeonId()
                    : plugin.getRootRegistry().expeditionState(world, owner).getActiveExpeditionId();
            if (e.id().equals(active)) return t("state.active");
        }
        return t(!crafting.unlocked(player, e) ? "state.locked" : crafting.affordable(player, e, materials, 1) ? "state.ready" : "state.materials");
    }
    private void icon(UICommandBuilder c, String row, String id) {
        boolean mob = id.startsWith("entity:"); c.set(row + " #Icon.Visible", !mob);
        var asset = mob ? catalog.mob(id) : null;
        c.set(row + " #MobIcon.Visible", asset != null);
        if (asset != null) c.setObject(row + " #MobIcon.Background", new PatchStyle(Value.of("../../" + asset.icon())));
        else if (!mob) c.set(row + " #Icon.ItemId", id);
    }
    private static void bind(UIEventBuilder events, String selector, String action, String id) {
        events.addEventBinding(CustomUIEventBindingType.Activating, selector, EventData.of("Action", action).append("Id", id), false);
    }
    @Override public void handleDataEvent(Ref<EntityStore> ref, Store<EntityStore> store, AtlasEvent event) {
        if (ref == null || !ref.isValid() || event == null || event.action == null) return;
        var player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getPageManager().getCustomPage() != this) return;
        if (event.action.equals("close")) { close(); return; }
        UICommandBuilder c = new UICommandBuilder(); UIEventBuilder events = new UIEventBuilder();
        try {
            if (event.action.equals("craft")) {
                notice = crafting.craft(ref, store, this, catalog.get(selected), quantity, event.revision).name();
                renderCraft(ref, store, player, c, events);
            } else if (event.action.equals("plus") || event.action.equals("minus")) {
                if (!crafting.revision().equals(event.revision)) return;
                quantity = Math.clamp(quantity + (event.action.equals("plus") ? 1 : -1), 1, AtlasCraftingService.MAX_QUANTITY);
                notice = ""; crafting.invalidate(); renderCraft(ref, store, player, c, events);
            } else {
                notice = "";
                switch (event.action) {
                    case "tab" -> { if (!List.of("Explore", "Expeditions", "Loot", "Dungeons").contains(event.id)) return; tab = event.id; lootId = ""; }
                    case "select" -> { if (catalog.get(event.id) == null || !navigable.contains(event.id)) return; selected = event.id; quantity = 1; }
                    case "loot" -> { if (!catalog.lootIds().contains(event.id)) return; lootId = event.id; query = ""; selected = ""; tab = "Loot"; c.set("#Search.Value", ""); }
                    case "search" -> { if (event.query == null || event.query.length() > 160) return; query = event.query.strip(); lootId = ""; selected = ""; }
                    case "state" -> stateFilter = (stateFilter + 1) % 4;
                    case "tier" -> tier = (tier + 1) % (catalog.all().stream().mapToInt(ExpeditionCatalog.Expedition::tier).max().orElse(5) + 1);
                    case "category" -> category = (category + 1) % (categories.size() + 1);
                    case "full" -> fullLoot = !fullLoot;
                    case "refresh" -> window.invalidateExtraResources();
                    default -> { return; }
                }
                render(ref, store, c, events);
            }
        } catch (RuntimeException error) {
            crafting.invalidate();
            com.hypixel.hytale.logger.HytaleLogger.forEnclosingClass().atWarning().withCause(error).log("Atlas action failed");
            c.set("#CraftState.Text", t("craftState.failed")); c.set("#Craft.Disabled", true);
        }
        sendUpdate(c, events, false);
    }
    @Override public void onDismiss(Ref<EntityStore> ref, Store<EntityStore> store) {
        crafting.invalidate();
        if (ref.isValid()) {
            var player = store.getComponent(ref, Player.getComponentType());
            if (player != null && player.getWindowManager().getWindow(window.getId()) == window)
                player.getWindowManager().closeWindow(ref, window.getId(), store);
        }
        super.onDismiss(ref, store);
    }
    private String name(ExpeditionCatalog.Expedition e) { return translate("server.expeditions." + e.id() + ".name", humanize(e.name())); }
    private String categoryName(String category) { return translate("server.benchCategories.OneBlockEnchanter_" + category, category); }
    private String lootName(String id) {
        if (id.startsWith("entity:")) {
            var mob = catalog.mob(id);
            return translate(mob == null ? "server.npcRoles." + id.substring(7) + ".name" : mob.nameKey(), humanize(id.substring(7)));
        }
        var item = Item.getAssetMap().getAsset(id);
        return item == null ? humanize(id) : translate(item.getTranslationKey(), humanize(id));
    }
    private String translate(String key, String fallback) {
        var i18n = I18nModule.get(); String value = i18n == null ? null : i18n.getMessage(playerRef.getLanguage(), key);
        return value == null || value.equals(key) ? fallback : value;
    }
    private String t(String key) { return translate("server.atlas." + key, key); }
    private String f(String key, Object... params) {
        String value = t(key); for (int i = 0; i < params.length; i += 2) value = value.replace("{" + params[i] + "}", String.valueOf(params[i + 1])); return value;
    }
    private String decimal(double value) { return String.format(Locale.forLanguageTag(playerRef.getLanguage()), "%.1f", value); }
    private String percent(double value) { return decimal(value) + "%"; }
    static String fold(String text) { return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT); }
    private static boolean matches(String text, String query) { return fold(text).contains(query); }
    private static String humanize(String id) { return id.replace('_', ' ').replaceAll("(?<=[a-z])(?=[A-Z])", " "); }
    static final class AtlasEvent {
        String action, id, query, revision;
        static final BuilderCodec<AtlasEvent> CODEC = BuilderCodec.builder(AtlasEvent.class, AtlasEvent::new)
                .append(new KeyedCodec<>("Action", Codec.STRING), (d,v) -> d.action=v, d -> d.action).add()
                .append(new KeyedCodec<>("Id", Codec.STRING), (d,v) -> d.id=v, d -> d.id).add()
                .append(new KeyedCodec<>("@Query", Codec.STRING), (d,v) -> d.query=v, d -> d.query).add()
                .append(new KeyedCodec<>("Revision", Codec.STRING), (d,v) -> d.revision=v, d -> d.revision).add().build();
    }
}
