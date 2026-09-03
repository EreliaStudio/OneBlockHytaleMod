package com.EreliaStudio.OneBlockAchievement;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class AchievementPage extends InteractiveCustomUIPage<AchievementPage.AchievementEvent> {
    private static final Comparator<AchievementDefinition> ALPHABETICAL = Comparator
            .comparing((AchievementDefinition achievement) -> achievement.name, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(achievement -> achievement.id);

    private final PlayerRef player;
    private final AchievementService service;

    AchievementPage(PlayerRef player, AchievementService service) {
        super(player, CustomPageLifetime.CanDismiss, AchievementEvent.CODEC);
        this.player = player;
        this.service = service;
    }

    @Override public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commands,
                                @Nonnull UIEventBuilder events, @Nonnull Store<EntityStore> store) {
        commands.append("AchievementPage.ui");
        populate(commands, events);
    }

    private void populate(UICommandBuilder commands, UIEventBuilder events) {
        UUID playerId = player.getUuid();
        service.refreshUnlocks(playerId);
        String activeTitle = service.activeTitle(playerId);
        commands.set("#AchievementLevel.Text", "Lv " + service.level(playerId));
        commands.set("#ActiveTitle.Text", activeTitle == null ? "No title selected" : "Active: [" + activeTitle + "]");

        List<AchievementDefinition> currentlyUnlocking = service.achievements().stream()
                .filter(achievement -> service.state(playerId, achievement) == AchievementService.State.CURRENTLY_UNLOCKING)
                .sorted(ALPHABETICAL).toList();
        List<AchievementDefinition> unlocked = service.achievements().stream()
                .filter(achievement -> service.state(playerId, achievement) == AchievementService.State.UNLOCKED)
                .sorted(ALPHABETICAL).toList();

        commands.clear("#UnlockingCards");
        commands.clear("#UnlockedCards");
        commands.set("#NoUnlockingMessage.Visible", currentlyUnlocking.isEmpty());
        commands.set("#NoUnlockedMessage.Visible", unlocked.isEmpty());
        renderCards("#UnlockingCards", currentlyUnlocking, false, activeTitle, commands, events);
        renderCards("#UnlockedCards", unlocked, true, activeTitle, commands, events);
        events.addEventBinding(CustomUIEventBindingType.Activating, "#ClearTitleButton",
                EventData.of("Achievement", "").append("Action", "clear"));
    }

    private void renderCards(String container, List<AchievementDefinition> achievements, boolean unlocked, String activeTitle,
                             UICommandBuilder commands, UIEventBuilder events) {
        UUID playerId = player.getUuid();
        for (int index = 0; index < achievements.size(); index++) {
            AchievementDefinition achievement = achievements.get(index);
            String card = container + "[" + index + "]";
            commands.append(container, "AchievementCard.ui");
            commands.set(card + " #CardTitle.Text", achievement.name);
            commands.set(card + " #CardSubtitle.Text", unlocked
                    ? "Title: [" + achievement.title + "]"
                    : "Unlocks title: [" + achievement.title + "]");
            commands.set(card + " #CardState.Text", unlocked ? "UNLOCKED" : "CURRENTLY UNLOCKING");
            commands.set(card + " #ParticipateButton.Visible", !unlocked);
            commands.set(card + " #ActivateButton.Visible", unlocked);
            commands.set(card + " #ActivateButton.Text", achievement.title.equals(activeTitle) ? "ACTIVE" : "ACTIVATE");
            commands.set(card + " #ActivateButton.Disabled", achievement.title.equals(activeTitle));

            if (unlocked) {
                events.addEventBinding(CustomUIEventBindingType.Activating, card + " #ActivateButton",
                        EventData.of("Achievement", achievement.id).append("Action", "activate"));
            } else {
                events.addEventBinding(CustomUIEventBindingType.Activating, card + " #ParticipateButton",
                        EventData.of("Achievement", achievement.id).append("Action", "participate"), false);
            }
            renderCosts(card, achievement, commands);
        }
    }

    private void renderCosts(String card, AchievementDefinition achievement, UICommandBuilder commands) {
        UUID playerId = player.getUuid();
        String costs = card + " #CardCosts";
        int costIndex = 0;
        Map<String, Integer> contributed = service.contributedItems(playerId, achievement.id);
        for (AchievementDefinition.ItemCost item : achievement.cost.items) {
            String cost = costs + "[" + costIndex++ + "]";
            commands.append(costs, "AchievementItemCost.ui");
            commands.set(cost + " #CostIcon.ItemId", item.id);
            commands.set(cost + " #CostProgress.Text", contributed.getOrDefault(item.id, 0) + " / " + item.quantity);
        }
        if (achievement.cost.currency > 0) {
            String cost = costs + "[" + costIndex++ + "]";
            commands.append(costs, "AchievementBadgeCost.ui");
            commands.set(cost + " #CostBadge.Text", "G");
            commands.set(cost + " #CostName.Text", "GLYMERA");
            commands.set(cost + " #CostProgress.Text", service.contributedCurrency(playerId, achievement.id)
                    + " / " + achievement.cost.currency);
        }
        for (String expedition : achievement.cost.expeditions) {
            String cost = costs + "[" + costIndex++ + "]";
            commands.append(costs, "AchievementBadgeCost.ui");
            commands.set(cost + " #CostBadge.Text", "E");
            commands.set(cost + " #CostName.Text", expedition);
            commands.set(cost + " #CostProgress.Text", service.knowsExpedition(playerId, expedition) ? "1 / 1" : "0 / 1");
        }
        commands.set(card + " #NoCostLabel.Visible", costIndex == 0);
    }

    @Override public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store,
                                          AchievementEvent event) {
        try {
            if ("clear".equals(event.action)) {
                service.selectTitle(player.getUuid(), null);
            } else {
                AchievementDefinition achievement = service.achievement(event.achievement);
                if (achievement == null) throw new IllegalArgumentException("Unknown achievement: " + event.achievement);
                if ("activate".equals(event.action)) {
                    service.selectTitle(player.getUuid(), achievement.id);
                } else if ("participate".equals(event.action)) {
                    AchievementContribution.Result result = AchievementContribution.contribute(
                            service, store, ref, player, achievement, Long.MAX_VALUE);
                    player.sendMessage(Message.raw("Contributed to " + achievement.name + ": "
                            + AchievementContribution.describe(result) + "."));
                    if (result.unlocked()) player.sendMessage(Message.raw("Achievement unlocked: " + achievement.name + "."));
                }
            }
            OneBlockAchievementPlugin plugin = OneBlockAchievementPlugin.get();
            if (plugin != null) plugin.updateNameplate(player);
            UICommandBuilder commands = new UICommandBuilder();
            UIEventBuilder events = new UIEventBuilder();
            populate(commands, events);
            sendUpdate(commands, events, false);
        } catch (RuntimeException error) {
            player.sendMessage(Message.raw("Achievement action failed: " + rootMessage(error)));
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }

    static final class AchievementEvent {
        static final BuilderCodec<AchievementEvent> CODEC = BuilderCodec.builder(AchievementEvent.class, AchievementEvent::new)
                .addField(new KeyedCodec<>("Achievement", Codec.STRING), (data, value) -> data.achievement = value, data -> data.achievement)
                .addField(new KeyedCodec<>("Action", Codec.STRING), (data, value) -> data.action = value, data -> data.action)
                .build();
        String achievement;
        String action;
    }
}
