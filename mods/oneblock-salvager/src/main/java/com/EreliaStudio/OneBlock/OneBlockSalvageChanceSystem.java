package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.crafting.component.ProcessingBenchBlock;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;

import java.io.Reader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class OneBlockSalvageChanceSystem extends ArchetypeTickingSystem<ChunkStore>
{
    private static final String TARGET_BENCH_ID = "OneBlockSalvager";
    private static final String TARGET_OUTPUT_ITEM_ID = "Ingredient_Crystal_White";
    private static final String RANDOM_CRYSTAL_TOKEN = "randomcrystal";
    private static final String EMPTY_TOKEN = "empty";
    private static final String[] CRYSTAL_OUTPUT_IDS = new String[] {
            "Ingredient_Crystal_Blue",
            "Ingredient_Crystal_Red",
            "Ingredient_Crystal_Yellow"
    };

    private static final SalvageConfig SALVAGE_CONFIG =
            SalvageConfig.load(OneBlockSalvageChanceSystem.class, "/oneblock-salvager-drops.json");

    private static final Field OUTPUT_CONTAINER_FIELD = findOutputContainerField();

    private final Map<Archetype<ChunkStore>, ComponentType<ChunkStore, ProcessingBenchBlock>> componentTypeCache =
            new IdentityHashMap<>();

    @Override
    public Query<ChunkStore> getQuery()
    {
        return Query.any();
    }

    @Override
    public void tick(float delta, ArchetypeChunk<ChunkStore> chunk, Store<ChunkStore> store, CommandBuffer<ChunkStore> buffer)
    {
        if (chunk == null)
        {
            return;
        }

        ComponentType<ChunkStore, ProcessingBenchBlock> componentType = getProcessingBenchComponentType(chunk.getArchetype());
        if (componentType == null)
        {
            return;
        }

        int size = chunk.size();
        for (int i = 0; i < size; i++)
        {
            ProcessingBenchBlock state = chunk.getComponent(i, componentType);
            if (state == null)
            {
                continue;
            }

            if (!isTargetBench(state))
            {
                continue;
            }

            SalvageTier tier = SALVAGE_CONFIG.resolveTier(1);

            CraftingRecipe recipe = state.getRecipe();
            if (recipe != null && !usesRubbleInput(recipe))
            {
                continue;
            }

            ItemContainer outputContainer = getOutputContainer(state);
            if (outputContainer == null)
            {
                continue;
            }

            processOutputContainer(outputContainer, tier);
        }
    }

    private static void processOutputContainer(ItemContainer outputContainer, SalvageTier tier)
    {
        short capacity = outputContainer.getCapacity();
        for (short slot = 0; slot < capacity; slot++)
        {
            ItemStack stack = outputContainer.getItemStack(slot);
            if (stack == null)
            {
                continue;
            }

            if (!TARGET_OUTPUT_ITEM_ID.equals(stack.getItemId()))
            {
                continue;
            }

            int quantity = Math.max(1, stack.getQuantity());
            String chosen = pickOutputId(tier);
            if (isEmptyOutput(chosen))
            {
                outputContainer.removeItemStackFromSlot(slot, false);
                continue;
            }

            outputContainer.removeItemStackFromSlot(slot, false);

            String outputId = resolveOutputId(chosen);
            int remaining = mergeIntoExistingStacks(outputContainer, outputId, quantity);
            remaining = placeIntoSlotIfEmpty(outputContainer, slot, outputId, remaining);
            if (remaining > 0)
            {
                placeIntoEmptySlots(outputContainer, outputId, remaining);
            }
        }
    }

    private static int mergeIntoExistingStacks(ItemContainer outputContainer, String crystalId, int quantity)
    {
        if (quantity <= 0)
        {
            return 0;
        }

        short capacity = outputContainer.getCapacity();
        for (short slot = 0; slot < capacity; slot++)
        {
            ItemStack existing = outputContainer.getItemStack(slot);
            if (existing == null || existing.isEmpty())
            {
                continue;
            }

            if (!crystalId.equals(existing.getItemId()))
            {
                continue;
            }

            int maxStack = getMaxStack(existing);
            int room = maxStack - existing.getQuantity();
            if (room <= 0)
            {
                continue;
            }

            int add = Math.min(room, quantity);
            outputContainer.setItemStackForSlot(
                    slot,
                    new ItemStack(crystalId, existing.getQuantity() + add, existing.getMetadata()),
                    false);
            quantity -= add;
            if (quantity <= 0)
            {
                return 0;
            }
        }

        return quantity;
    }

    private static int placeIntoSlotIfEmpty(ItemContainer outputContainer, short slot, String crystalId, int quantity)
    {
        if (quantity <= 0)
        {
            return 0;
        }

        ItemStack current = outputContainer.getItemStack(slot);
        if (current != null && !current.isEmpty())
        {
            return quantity;
        }

        int maxStack = getMaxStack(crystalId);
        int add = Math.min(maxStack, quantity);
        outputContainer.setItemStackForSlot(slot, new ItemStack(crystalId, add), false);
        return quantity - add;
    }

    private static void placeIntoEmptySlots(ItemContainer outputContainer, String crystalId, int quantity)
    {
        if (quantity <= 0)
        {
            return;
        }

        int maxStack = getMaxStack(crystalId);
        short capacity = outputContainer.getCapacity();
        for (short slot = 0; slot < capacity && quantity > 0; slot++)
        {
            ItemStack existing = outputContainer.getItemStack(slot);
            if (existing != null && !existing.isEmpty())
            {
                continue;
            }

            int add = Math.min(maxStack, quantity);
            outputContainer.setItemStackForSlot(slot, new ItemStack(crystalId, add), false);
            quantity -= add;
        }
    }

    private static int getMaxStack(ItemStack stack)
    {
        if (stack == null)
        {
            return 1;
        }

        com.hypixel.hytale.server.core.asset.type.item.config.Item item = stack.getItem();
        if (item == null)
        {
            return 1;
        }

        int maxStack = item.getMaxStack();
        return maxStack > 0 ? maxStack : 1;
    }

    private static int getMaxStack(String itemId)
    {
        return getMaxStack(new ItemStack(itemId, 1));
    }

    private static boolean isTargetBench(ProcessingBenchBlock state)
    {
        Bench bench = state.getBench();
        return bench != null && TARGET_BENCH_ID.equals(bench.getId());
    }

    private static ItemContainer getOutputContainer(ProcessingBenchBlock state)
    {
        if (OUTPUT_CONTAINER_FIELD == null || state == null)
        {
            return null;
        }

        try
        {
            return (ItemContainer) OUTPUT_CONTAINER_FIELD.get(state);
        }
        catch (IllegalAccessException ignored)
        {
            return null;
        }
    }

    private static Field findOutputContainerField()
    {
        for (Field field : ProcessingBenchBlock.class.getDeclaredFields())
        {
            if (!ItemContainer.class.isAssignableFrom(field.getType()))
            {
                continue;
            }

            String name = field.getName().toLowerCase();
            if (!name.contains("output"))
            {
                continue;
            }

            field.setAccessible(true);
            return field;
        }

        return null;
    }

    private ComponentType<ChunkStore, ProcessingBenchBlock> getProcessingBenchComponentType(Archetype<ChunkStore> archetype)
    {
        if (archetype == null)
        {
            return null;
        }

        if (componentTypeCache.containsKey(archetype))
        {
            return componentTypeCache.get(archetype);
        }

        ComponentType<ChunkStore, ProcessingBenchBlock> resolved = resolveProcessingBenchComponentType(archetype);
        componentTypeCache.put(archetype, resolved);
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static ComponentType<ChunkStore, ProcessingBenchBlock> resolveProcessingBenchComponentType(Archetype<ChunkStore> archetype)
    {
        if (archetype == null)
        {
            return null;
        }

        int length = archetype.length();
        for (int i = 0; i < length; i++)
        {
            ComponentType<ChunkStore, ?> type = archetype.get(i);
            if (type == null)
            {
                continue;
            }

            Class<?> typeClass = type.getTypeClass();
            if (typeClass == ProcessingBenchBlock.class)
            {
                return (ComponentType<ChunkStore, ProcessingBenchBlock>) type;
            }
        }

        return null;
    }

    private static boolean usesRubbleInput(CraftingRecipe recipe)
    {
        if (recipe == null)
        {
            return false;
        }

        MaterialQuantity[] inputs = recipe.getInput();
        if (inputs == null || inputs.length == 0)
        {
            return false;
        }

        for (MaterialQuantity input : inputs)
        {
            if (input == null)
            {
                continue;
            }

            String resourceTypeId = input.getResourceTypeId();
            if ("Rubble".equals(resourceTypeId))
            {
                return true;
            }

            String itemId = input.getItemId();
            if (itemId != null && itemId.startsWith("Rubble_"))
            {
                return true;
            }
        }

        return false;
    }

    private static String pickOutputId(SalvageTier tier)
    {
        if (tier == null || tier.entries.isEmpty() || tier.totalWeight <= 0)
        {
            return RANDOM_CRYSTAL_TOKEN;
        }

        int roll = ThreadLocalRandom.current().nextInt(tier.totalWeight);
        int cursor = 0;
        for (SalvageEntry entry : tier.entries)
        {
            if (entry == null || entry.weight <= 0)
            {
                continue;
            }

            cursor += entry.weight;
            if (roll < cursor)
            {
                return entry.itemId;
            }
        }

        return tier.entries.get(0).itemId;
    }

    private static String resolveOutputId(String itemId)
    {
        if (isRandomCrystalOutput(itemId))
        {
            return pickRandomCrystalId();
        }

        return itemId;
    }

    private static boolean isEmptyOutput(String itemId)
    {
        if (itemId == null)
        {
            return true;
        }

        String token = normalizeToken(itemId);
        return token.isEmpty()
                || EMPTY_TOKEN.equals(token)
                || "none".equals(token)
                || "-".equals(token);
    }

    private static boolean isRandomCrystalOutput(String itemId)
    {
        if (itemId == null)
        {
            return false;
        }

        String token = normalizeToken(itemId);
        return RANDOM_CRYSTAL_TOKEN.equals(token)
                || "random_crystal".equals(token)
                || "crystal:random".equals(token);
    }

    private static String normalizeToken(String value)
    {
        if (value == null)
        {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static final class SalvageConfig
    {
        private static final Gson GSON = new Gson();

        private final List<SalvageTier> tiers;

        private SalvageConfig(List<SalvageTier> tiers)
        {
            this.tiers = tiers == null ? List.of() : tiers;
        }

        static SalvageConfig load(Class<?> owner, String resourcePath)
        {
            if (owner == null || resourcePath == null || resourcePath.isEmpty())
            {
                return defaultConfig();
            }

            try (var in = owner.getResourceAsStream(resourcePath))
            {
                if (in == null)
                {
                    return defaultConfig();
                }

                try (Reader reader = new java.io.InputStreamReader(in, StandardCharsets.UTF_8))
                {
                    JsonObject root = GSON.fromJson(reader, JsonObject.class);
                    if (root == null)
                    {
                        return defaultConfig();
                    }

                    JsonArray tiersArray = root.getAsJsonArray("Tiers");
                    if (tiersArray == null)
                    {
                        tiersArray = root.getAsJsonArray("tiers");
                    }

                    if (tiersArray == null)
                    {
                        return defaultConfig();
                    }

                    List<SalvageTier> tiers = new ArrayList<>();
                    for (JsonElement element : tiersArray)
                    {
                        if (element == null || !element.isJsonObject())
                        {
                            continue;
                        }

                        JsonObject tierObj = element.getAsJsonObject();
                        int tierLevel = readInt(tierObj, "TierLevel", "tierLevel", 1);
                        if (tierLevel < 1)
                        {
                            continue;
                        }

                        JsonArray entriesArray = tierObj.getAsJsonArray("Entries");
                        if (entriesArray == null)
                        {
                            entriesArray = tierObj.getAsJsonArray("entries");
                        }

                        if (entriesArray == null)
                        {
                            continue;
                        }

                        List<SalvageEntry> entries = new ArrayList<>();
                        for (JsonElement entryElement : entriesArray)
                        {
                            if (entryElement == null || !entryElement.isJsonObject())
                            {
                                continue;
                            }

                            JsonObject entryObj = entryElement.getAsJsonObject();
                            String itemId = readString(entryObj, "ItemId", "itemId", "Id", "id");
                            if (itemId == null || itemId.isEmpty())
                            {
                                continue;
                            }

                            int weight = readInt(entryObj, "Weight", "weight", 1);
                            if (weight < 1)
                            {
                                weight = 1;
                            }

                            entries.add(new SalvageEntry(itemId, weight));
                        }

                        if (!entries.isEmpty())
                        {
                            tiers.add(new SalvageTier(tierLevel, entries));
                        }
                    }

                    if (tiers.isEmpty())
                    {
                        return defaultConfig();
                    }

                    tiers.sort(Comparator.comparingInt(tier -> tier.tierLevel));
                    return new SalvageConfig(tiers);
                }
            }
            catch (Exception ignored)
            {
                return defaultConfig();
            }
        }

        SalvageTier resolveTier(int tierLevel)
        {
            if (tiers.isEmpty())
            {
                return null;
            }

            SalvageTier selected = tiers.get(0);
            for (SalvageTier tier : tiers)
            {
                if (tierLevel >= tier.tierLevel)
                {
                    selected = tier;
                }
                else
                {
                    break;
                }
            }

            return selected;
        }

        private static SalvageConfig defaultConfig()
        {
            List<SalvageEntry> entries = List.of(new SalvageEntry("RandomCrystal", 100));
            List<SalvageTier> tiers = List.of(new SalvageTier(1, entries));
            return new SalvageConfig(tiers);
        }

        private static String readString(JsonObject obj, String... keys)
        {
            if (obj == null || keys == null)
            {
                return null;
            }

            for (String key : keys)
            {
                if (key == null || key.isEmpty() || !obj.has(key))
                {
                    continue;
                }
                JsonElement e = obj.get(key);
                if (e != null && e.isJsonPrimitive())
                {
                    String value = e.getAsString();
                    if (value != null && !value.isEmpty())
                    {
                        return value;
                    }
                }
            }

            return null;
        }

        private static int readInt(JsonObject obj, String key1, String key2, int defaultValue)
        {
            if (obj == null)
            {
                return defaultValue;
            }

            JsonElement e = null;
            if (key1 != null && obj.has(key1))
            {
                e = obj.get(key1);
            }
            else if (key2 != null && obj.has(key2))
            {
                e = obj.get(key2);
            }

            if (e == null || !e.isJsonPrimitive())
            {
                return defaultValue;
            }

            try
            {
                return e.getAsInt();
            }
            catch (Exception ignored)
            {
                return defaultValue;
            }
        }
    }

    private static final class SalvageTier
    {
        private final int tierLevel;
        private final List<SalvageEntry> entries;
        private final int totalWeight;

        private SalvageTier(int tierLevel, List<SalvageEntry> entries)
        {
            this.tierLevel = tierLevel;
            this.entries = entries == null ? List.of() : entries;
            int total = 0;
            for (SalvageEntry entry : this.entries)
            {
                if (entry == null || entry.itemId == null || entry.itemId.isEmpty())
                {
                    continue;
                }
                total += Math.max(0, entry.weight);
            }
            this.totalWeight = total;
        }
    }

    private static final class SalvageEntry
    {
        private final String itemId;
        private final int weight;

        private SalvageEntry(String itemId, int weight)
        {
            this.itemId = itemId;
            this.weight = weight;
        }
    }

    private static String pickRandomCrystalId()
    {
        int idx = ThreadLocalRandom.current().nextInt(CRYSTAL_OUTPUT_IDS.length);
        return CRYSTAL_OUTPUT_IDS[idx];
    }
}
