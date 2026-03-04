package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

/**
 * Unlocks OneBlock drops when a player consumes one of the unlock consumable items.
 *
 * This implementation intentionally resolves the consume event class and consumed item accessors
 * via reflection so it remains compatible with SDK variants where item interaction APIs differ.
 */
public final class OneBlockUnlockConsumeSystem extends EntityEventSystem<EntityStore, EcsEvent>
{
    private static final String[] CONSUME_EVENT_CLASS_CANDIDATES = new String[] {
            "com.hypixel.hytale.server.core.event.events.ecs.ConsumeItemEvent",
            "com.hypixel.hytale.server.core.event.events.ecs.ItemConsumeEvent",
            "com.hypixel.hytale.server.core.event.events.ecs.UseItemEvent"
    };

    private final OneBlockUnlockService unlockService;

    public OneBlockUnlockConsumeSystem(OneBlockUnlockService unlockService)
    {
        super(castConsumeEventClass(resolveConsumeEventClass()));
        this.unlockService = unlockService;
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
                       @Nonnull EcsEvent event)
    {
        if (unlockService == null)
        {
            return;
        }

        String consumedItemId = resolveConsumedItemId(event);
        if (consumedItemId == null || consumedItemId.isEmpty())
        {
            return;
        }

        Ref<EntityStore> playerEntity = chunk.getReferenceTo(entityIndex);
        PlayerRef playerRef = commandBuffer.getComponent(playerEntity, PlayerRef.getComponentType());
        Player player = commandBuffer.getComponent(playerEntity, Player.getComponentType());
        if (playerRef == null || player == null)
        {
            return;
        }

        OneBlockUnlockService.UnlockConsumeResult result = unlockService.consume(playerRef.getUuid(), consumedItemId);
        switch (result)
        {
            case UNLOCKED:
            {
                String dropId = unlockService.getDropItemIdForConsumable(consumedItemId);
                player.sendMessage(Message.raw("Unlocked OneBlock drop: " + dropId));
                return;
            }

            case ALREADY_UNLOCKED:
                player.sendMessage(Message.raw("OneBlock unlock already known: " + consumedItemId));
                return;

            case INVALID_ITEM:
                // not a OneBlock unlock consumable; ignore silently
                return;

            case UNLOCK_FAILED:
            default:
                player.sendMessage(Message.raw("Failed to unlock OneBlock drop from: " + consumedItemId));
        }
    }

    private static Class<? extends EcsEvent> resolveConsumeEventClass()
    {
        for (String className : CONSUME_EVENT_CLASS_CANDIDATES)
        {
            try
            {
                Class<?> candidate = Class.forName(className);
                if (EcsEvent.class.isAssignableFrom(candidate))
                {
                    @SuppressWarnings("unchecked")
                    Class<? extends EcsEvent> typed = (Class<? extends EcsEvent>) candidate;
                    return typed;
                }
            }
            catch (ClassNotFoundException ignored)
            {
            }
        }

        throw new IllegalStateException("No supported consume event class found in this SDK. Tried: "
                + String.join(", ", CONSUME_EVENT_CLASS_CANDIDATES));
    }

    @SuppressWarnings("unchecked")
    private static Class<EcsEvent> castConsumeEventClass(Class<? extends EcsEvent> eventClass)
    {
        return (Class<EcsEvent>) eventClass;
    }

    private static String resolveConsumedItemId(Object event)
    {
        if (event == null)
        {
            return null;
        }

        Object value = invokeNoArg(event, "getItemStack");
        String idFromStack = extractItemId(value);
        if (idFromStack != null)
        {
            return idFromStack;
        }

        value = invokeNoArg(event, "getHeldItem");
        idFromStack = extractItemId(value);
        if (idFromStack != null)
        {
            return idFromStack;
        }

        value = invokeNoArg(event, "getItem");
        idFromStack = extractItemId(value);
        if (idFromStack != null)
        {
            return idFromStack;
        }

        value = invokeNoArg(event, "getItemId");
        if (value instanceof String s && !s.isEmpty())
        {
            return s;
        }

        return null;
    }

    private static String extractItemId(Object maybeItem)
    {
        if (maybeItem == null)
        {
            return null;
        }

        if (maybeItem instanceof ItemStack stack)
        {
            return stack.getItemId();
        }

        Object itemId = invokeNoArg(maybeItem, "getItemId");
        if (itemId instanceof String s && !s.isEmpty())
        {
            return s;
        }

        return null;
    }

    private static Object invokeNoArg(Object target, String methodName)
    {
        try
        {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }
}
