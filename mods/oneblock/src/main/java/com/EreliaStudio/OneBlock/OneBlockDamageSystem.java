package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/** Applies deterministic OneBlock damage and rejects the wrong tool. */
public final class OneBlockDamageSystem extends EntityEventSystem<EntityStore, DamageBlockEvent>
{
    private final OneBlockRootRegistry rootRegistry;

    public OneBlockDamageSystem(OneBlockRootRegistry rootRegistry)
    {
        super(DamageBlockEvent.class);
        this.rootRegistry = rootRegistry;
    }

    @Override
    public Query<EntityStore> getQuery()
    {
        return Query.any();
    }

    @Override
    public void handle(int entityIndex,
                       ArchetypeChunk<EntityStore> chunk,
                       Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull DamageBlockEvent event)
    {
        Ref<EntityStore> ref = chunk.getReferenceTo(entityIndex);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (!isManagedOneBlockDamage(player, event)) return;

        EntityStore entityStore = store.getExternalData();
        if (entityStore == null) return;

        World world = entityStore.getWorld();
        if (world == null) return;

        OneBlockRootRegistry.RootEntry root = rootRegistry.find(world, event.getTargetBlock());
        if (root == null) return;
        com.hypixel.hytale.server.core.universe.PlayerRef playerRef =
                store.getComponent(ref, com.hypixel.hytale.server.core.universe.PlayerRef.getComponentType());
        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin != null && (playerRef == null
                || !plugin.mayUse(world, playerRef.getUuid(), root)))
        {
            event.setDamage(0.0F);
            event.setCancelled(true);
            return;
        }
        if (plugin != null)
            plugin.listenToOneBlock(playerRef, world, event.getTargetBlock());

        if (isRemovalTool(event.getItemInHand()))
        {
            // The extractor deliberately bypasses expedition tool rules and
            // destroys the root in a single completed break.
            event.setDamage(1.0F);
            return;
        }

        OneBlockSolidityDefaults.SolidityDefinition solidity =
                OneBlockSolidityDefaults.get(event.getBlockType().getId());

        if (!hasRequiredTool(event.getItemInHand(), solidity.requiredTool()))
        {
            event.setDamage(0.0F);
            event.setCancelled(true);
            return;
        }

        // Despite its name, getCurrentDamage() contains remaining normalized
        // block health: 1 for a fresh block and 0 for a destroyed block.
        float toolMultiplier = OneBlockToolStrength.multiplier(
                event.getItemInHand(),
                solidity.requiredTool()
        );
        event.setDamage(OneBlockDamageCalculator.forRemainingHealth(
                event.getCurrentDamage(),
                solidity.ticks(),
                toolMultiplier
        ));
    }

    static boolean isRemovalTool(ItemStack itemStack)
    {
        return !ItemStack.isEmpty(itemStack)
                && OneBlockBlockIds.ROOT_REMOVAL_TOOL_ID.equals(itemStack.getItemId());
    }

    private static boolean isManagedOneBlockDamage(Player player, DamageBlockEvent event)
    {
        if (player == null || event == null) return false;

        Object gameMode = player.getGameMode();
        if (gameMode != null && "Creative".equalsIgnoreCase(gameMode.toString())) return false;

        return OneBlockBlockUtil.isOneBlock(event.getBlockType());
    }

    static boolean hasRequiredTool(ItemStack itemStack,
                                   OneBlockSolidityDefaults.RequiredTool requiredTool)
    {
        if (requiredTool == null || requiredTool == OneBlockSolidityDefaults.RequiredTool.HAND)
            return true;
        if (ItemStack.isEmpty(itemStack)) return false;

        Item item = itemStack.getItem();
        if (item == null) return false;

        String animationId = item.getPlayerAnimationsId();
        if (animationId == null) return false;

        return switch (requiredTool)
        {
            case PICKAXE -> "Pickaxe".equalsIgnoreCase(animationId);
            case AXE -> "Hatchet".equalsIgnoreCase(animationId)
                    || "Axe".equalsIgnoreCase(animationId);
            case HAND -> true;
        };
    }
}
