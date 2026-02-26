package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
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

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class OneBlockSalvageChanceSystem extends ArchetypeTickingSystem<ChunkStore>
{
    private static final String TARGET_BENCH_ID = "OneBlockSalvager";
    private static final String TARGET_OUTPUT_ITEM_ID = "Ingredient_Crystal_White";
    private static final double TARGET_SUCCESS_RATE = 0.10;
    private static final String[] CRYSTAL_OUTPUT_IDS = new String[] {
            "Ingredient_Crystal_Blue",
            "Ingredient_Crystal_Red",
            "Ingredient_Crystal_Yellow"
    };

    private static final Field OUTPUT_CONTAINER_FIELD = findOutputContainerField();

    private final Map<Archetype<ChunkStore>, ComponentType<ChunkStore, ProcessingBenchState>> componentTypeCache =
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

        ComponentType<ChunkStore, ProcessingBenchState> componentType = getProcessingBenchComponentType(chunk.getArchetype());
        if (componentType == null)
        {
            return;
        }

        int size = chunk.size();
        for (int i = 0; i < size; i++)
        {
            ProcessingBenchState state = chunk.getComponent(i, componentType);
            if (state == null)
            {
                continue;
            }

            if (!isTargetBench(state))
            {
                continue;
            }

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

            processOutputContainer(outputContainer);
        }
    }

    private static void processOutputContainer(ItemContainer outputContainer)
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

            boolean success = ThreadLocalRandom.current().nextDouble() < TARGET_SUCCESS_RATE;
            int quantity = Math.max(1, stack.getQuantity());
            if (!success)
            {
                outputContainer.removeItemStackFromSlot(slot, false);
                continue;
            }

            outputContainer.removeItemStackFromSlot(slot, false);

            String crystalId = pickRandomCrystalId();
            int remaining = mergeIntoExistingStacks(outputContainer, crystalId, quantity);
            remaining = placeIntoSlotIfEmpty(outputContainer, slot, crystalId, remaining);
            if (remaining > 0)
            {
                placeIntoEmptySlots(outputContainer, crystalId, remaining);
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

    private static boolean isTargetBench(ProcessingBenchState state)
    {
        Bench bench = state.getBench();
        return bench != null && TARGET_BENCH_ID.equals(bench.getId());
    }

    private static ItemContainer getOutputContainer(ProcessingBenchState state)
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
        for (Field field : ProcessingBenchState.class.getDeclaredFields())
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

    private ComponentType<ChunkStore, ProcessingBenchState> getProcessingBenchComponentType(Archetype<ChunkStore> archetype)
    {
        if (archetype == null)
        {
            return null;
        }

        if (componentTypeCache.containsKey(archetype))
        {
            return componentTypeCache.get(archetype);
        }

        ComponentType<ChunkStore, ProcessingBenchState> resolved = resolveProcessingBenchComponentType(archetype);
        componentTypeCache.put(archetype, resolved);
        return resolved;
    }

    @SuppressWarnings("unchecked")
    private static ComponentType<ChunkStore, ProcessingBenchState> resolveProcessingBenchComponentType(Archetype<ChunkStore> archetype)
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
            if (typeClass == ProcessingBenchState.class)
            {
                return (ComponentType<ChunkStore, ProcessingBenchState>) type;
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

    private static String pickRandomCrystalId()
    {
        int idx = ThreadLocalRandom.current().nextInt(CRYSTAL_OUTPUT_IDS.length);
        return CRYSTAL_OUTPUT_IDS[idx];
    }
}
