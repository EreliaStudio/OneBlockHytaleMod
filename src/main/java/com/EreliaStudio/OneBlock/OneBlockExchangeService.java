package com.EreliaStudio.OneBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OneBlockExchangeService
{
    public static final String EXCHANGE_UNLOCK_PREFIX = "Exchange:";

    private final OneBlockDropsStateProvider stateProvider;
    private final Map<String, ExchangeDefinition> definitionByItemId = new HashMap<>();

    public OneBlockExchangeService(OneBlockDropsStateProvider stateProvider,
                                   Map<String, ExchangeDefinition> exchangeMap)
    {
        this.stateProvider = stateProvider;

        if (exchangeMap == null || exchangeMap.isEmpty())
        {
            return;
        }

        definitionByItemId.putAll(exchangeMap);
    }

    public ExchangeConsumeResult canConsume(UUID playerId, String exchangeItemId)
    {
        if (playerId == null || exchangeItemId == null || exchangeItemId.isEmpty())
        {
            return ExchangeConsumeResult.INVALID_ITEM;
        }

        ExchangeDefinition definition = definitionByItemId.get(exchangeItemId);
        if (definition == null)
        {
            return ExchangeConsumeResult.INVALID_ITEM;
        }

        if (definition.unlockId != null && !definition.unlockId.isEmpty())
        {
            if (stateProvider == null)
            {
                return ExchangeConsumeResult.LOCKED;
            }

            String expeditionId = OneBlockExpeditionResolver.normalizeExpedition(definition.expeditionId);
            if (!stateProvider.isUnlocked(playerId, expeditionId, definition.unlockId))
            {
                return ExchangeConsumeResult.LOCKED;
            }
        }

        return ExchangeConsumeResult.READY;
    }

    public ExchangeDefinition getDefinition(String exchangeItemId)
    {
        if (exchangeItemId == null || exchangeItemId.isEmpty())
        {
            return null;
        }

        return definitionByItemId.get(exchangeItemId);
    }

    public enum ExchangeConsumeResult
    {
        INVALID_ITEM,
        LOCKED,
        READY
    }

    public static final class ExchangeDefinition
    {
        public final String expeditionId;
        public final String outputId;
        public final int outputQuantity;
        public final String unlockId;

        public ExchangeDefinition(String expeditionId, String outputId, int outputQuantity, String unlockId)
        {
            this.expeditionId = expeditionId;
            this.outputId = outputId;
            this.outputQuantity = outputQuantity < 1 ? 1 : outputQuantity;
            this.unlockId = unlockId;
        }
    }

    public static boolean isExchangeUnlockId(String value)
    {
        return value != null && value.startsWith(EXCHANGE_UNLOCK_PREFIX);
    }
}
