package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.builtin.crafting.state.ProcessingBenchState;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer.ItemContainerChangeEvent;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.SlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class OneBlockSalvageChanceSystem extends HolderSystem
{
    private static final String TARGET_BENCH_ID = "OneBlockSalvager";
    private static final String TARGET_OUTPUT_ITEM_ID = "Ingredient_Crystal_White";
    private static final double TARGET_SUCCESS_RATE = 0.10;
    private static final String[] CRYSTAL_OUTPUT_IDS = new String[] {
            "Ingredient_Crystal_Blue",
            "Ingredient_Crystal_Cyan",
            "Ingredient_Crystal_Green",
            "Ingredient_Crystal_Purple",
            "Ingredient_Crystal_Red",
            "Ingredient_Crystal_White",
            "Ingredient_Crystal_Yellow"
    };

    private static final ThreadLocal<Boolean> SUPPRESS_EVENTS = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private static final Field OUTPUT_CONTAINER_FIELD = findOutputContainerField();

    private final Set<ItemContainer> registeredContainers = Collections.newSetFromMap(new IdentityHashMap<>());
    private ComponentType processingBenchComponentType;

    @Override
    public Query<EntityStore> getQuery()
    {
        return Query.any();
    }

    @Override
    public void onEntityAdd(Holder holder, AddReason reason, Store store)
    {
        ProcessingBenchState state = getProcessingBenchState(holder);
        if (state == null)
        {
            return;
        }

        if (!isTargetBench(state))
        {
            return;
        }

        ItemContainer outputContainer = getOutputContainer(state);
        if (outputContainer == null || !registeredContainers.add(outputContainer))
        {
            return;
        }

        outputContainer.registerChangeEvent(event -> onOutputChanged(state, event));
    }

    @Override
    public void onEntityRemoved(Holder holder, RemoveReason reason, Store store)
    {
        // No explicit unregister API. Container registrations are cleaned up by the engine when the
        // container is destroyed; the identity set prevents duplicate registrations while active.
    }

    private ProcessingBenchState getProcessingBenchState(Holder holder)
    {
        ComponentType componentType = resolveProcessingBenchComponentType();
        if (componentType == null)
        {
            return null;
        }

        return (ProcessingBenchState) holder.getComponent(componentType);
    }

    private ComponentType resolveProcessingBenchComponentType()
    {
        if (processingBenchComponentType != null)
        {
            return processingBenchComponentType;
        }

        BlockStateModule module = BlockStateModule.get();
        if (module == null)
        {
            return null;
        }

        processingBenchComponentType = module.getComponentType(ProcessingBenchState.class);
        return processingBenchComponentType;
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

    private void onOutputChanged(ProcessingBenchState state, ItemContainerChangeEvent event)
    {
        if (state == null || event == null)
        {
            return;
        }

        if (Boolean.TRUE.equals(SUPPRESS_EVENTS.get()))
        {
            return;
        }

        CraftingRecipe recipe = state.getRecipe();
        if (recipe == null)
        {
            return;
        }

        MaterialQuantity primaryOutput = recipe.getPrimaryOutput();
        if (primaryOutput == null || !TARGET_OUTPUT_ITEM_ID.equals(primaryOutput.getItemId()))
        {
            return;
        }

        if (!usesRubbleInput(recipe))
        {
            return;
        }

        Transaction transaction = event.transaction();
        if (transaction == null)
        {
            return;
        }

        List<SlotTransaction> slotTransactions = new ArrayList<>();
        collectSlotTransactions(transaction, slotTransactions);
        List<SlotTransaction> targetSlots = findTargetOutputSlots(slotTransactions);
        if (targetSlots.isEmpty())
        {
            return;
        }

        boolean success = ThreadLocalRandom.current().nextDouble() < TARGET_SUCCESS_RATE;

        SUPPRESS_EVENTS.set(Boolean.TRUE);
        try
        {
            ItemContainer container = event.container();
            for (SlotTransaction slotTransaction : targetSlots)
            {
                if (slotTransaction == null)
                {
                    continue;
                }

                if (!success)
                {
                    container.setItemStackForSlot(slotTransaction.getSlot(), slotTransaction.getSlotBefore());
                    continue;
                }

                ItemStack after = slotTransaction.getSlotAfter();
                int quantity = after == null ? 1 : Math.max(1, after.getQuantity());
                String crystalId = pickRandomCrystalId();
                container.setItemStackForSlot(slotTransaction.getSlot(), new ItemStack(crystalId, quantity));
            }
        }
        finally
        {
            SUPPRESS_EVENTS.set(Boolean.FALSE);
        }
    }

    private static List<SlotTransaction> findTargetOutputSlots(List<SlotTransaction> slotTransactions)
    {
        List<SlotTransaction> matches = new ArrayList<>();
        for (SlotTransaction slotTransaction : slotTransactions)
        {
            if (slotTransaction == null)
            {
                continue;
            }

            ItemStack after = slotTransaction.getSlotAfter();
            if (after == null || !TARGET_OUTPUT_ITEM_ID.equals(after.getItemId()))
            {
                continue;
            }

            ItemStack before = slotTransaction.getSlotBefore();
            if (before == null || !TARGET_OUTPUT_ITEM_ID.equals(before.getItemId()))
            {
                matches.add(slotTransaction);
                continue;
            }

            if (after.getQuantity() > before.getQuantity())
            {
                matches.add(slotTransaction);
            }
        }

        return matches;
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

    private static void collectSlotTransactions(Transaction transaction, List<SlotTransaction> out)
    {
        if (transaction == null)
        {
            return;
        }

        if (transaction instanceof SlotTransaction slotTransaction)
        {
            out.add(slotTransaction);
            return;
        }

        if (transaction instanceof ItemStackTransaction itemStackTransaction)
        {
            out.addAll(itemStackTransaction.getSlotTransactions());
            return;
        }

        if (transaction instanceof ListTransaction listTransaction)
        {
            for (Object entry : listTransaction.getList())
            {
                if (entry instanceof Transaction child)
                {
                    collectSlotTransactions(child, out);
                }
            }
            return;
        }

        if (transaction instanceof MoveTransaction moveTransaction)
        {
            collectSlotTransactions(moveTransaction.getAddTransaction(), out);
            collectSlotTransactions(moveTransaction.getRemoveTransaction(), out);
        }
    }
}
