package com.EreliaStudio.OneBlockAchievement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

final class AchievementContribution {
    private AchievementContribution() {}

    static Result contribute(AchievementService service, Store<EntityStore> store, Ref<EntityStore> ref,
                             PlayerRef playerRef, AchievementDefinition achievement, long moneyLimit) {
        UUID playerId = playerRef.getUuid();
        if (service.unlocked(playerId, achievement.id)) throw new IllegalStateException("That achievement is already unlocked");
        if (!service.prerequisitesMet(playerId, achievement)) throw new IllegalStateException("Unlock its prerequisites first");
        if (moneyLimit < 0) throw new IllegalArgumentException("Money amount cannot be negative");

        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) throw new IllegalStateException("Player inventory is unavailable");
        ItemContainer inventory = player.getInventory().getCombinedBackpackStorageHotbarFirst();
        Map<String, Integer> itemsTaken = new LinkedHashMap<>();
        for (AchievementDefinition.ItemCost item : achievement.cost.items) {
            int remaining = service.remainingItem(playerId, achievement, item);
            int take = Math.min(remaining, count(inventory, item.id));
            if (take <= 0) continue;
            ItemStackTransaction transaction = inventory.removeItemStack(new ItemStack(item.id, take), true, true);
            if (transaction.succeeded()) itemsTaken.put(item.id, take);
        }

        long moneyTaken = 0;
        long remainingMoney = service.remainingCurrency(playerId, achievement);
        if (remainingMoney > 0 && moneyLimit > 0) {
            GlymeraEconomy economy = GlymeraEconomy.discover();
            if (economy == null) {
                if (itemsTaken.isEmpty()) throw new IllegalStateException(
                        "GlymeraMerchant is required for this achievement's currency cost");
            } else {
                moneyTaken = Math.min(remainingMoney, Math.min(moneyLimit, economy.balance(playerId)));
                if (moneyTaken > 0 && !economy.withdraw(playerId, moneyTaken)) moneyTaken = 0;
            }
        }

        if (itemsTaken.isEmpty() && moneyTaken == 0) {
            if (!service.knowledgeMet(playerId, achievement))
                throw new IllegalStateException("You have none of the remaining costs; expedition knowledge is still missing");
            throw new IllegalStateException("You have none of the remaining costs to contribute");
        }

        service.contribute(playerId, achievement.id, moneyTaken, itemsTaken);
        boolean unlocked = service.tryUnlock(playerId, achievement);
        return new Result(Map.copyOf(itemsTaken), moneyTaken, unlocked);
    }

    static String describe(Result result) {
        StringBuilder text = new StringBuilder();
        for (Map.Entry<String, Integer> item : result.items().entrySet()) {
            if (!text.isEmpty()) text.append(", ");
            text.append(item.getValue()).append(' ').append(item.getKey());
        }
        if (result.currency() > 0) {
            if (!text.isEmpty()) text.append(", ");
            text.append(result.currency()).append(" currency");
        }
        return text.toString();
    }

    private static int count(ItemContainer inventory, String itemId) {
        int total = 0;
        for (short slot = 0; slot < inventory.getCapacity(); slot++) {
            ItemStack stack = inventory.getItemStack(slot);
            if (stack != null && !stack.isEmpty() && itemId.equals(stack.getItemId())) total += stack.getQuantity();
        }
        return total;
    }

    record Result(Map<String, Integer> items, long currency, boolean unlocked) {}
}
