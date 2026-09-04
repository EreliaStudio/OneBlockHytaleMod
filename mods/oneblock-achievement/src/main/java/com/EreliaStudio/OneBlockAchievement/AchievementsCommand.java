package com.EreliaStudio.OneBlockAchievement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

final class AchievementsCommand extends AbstractPlayerCommand {
    private final AchievementCatalog catalog;
    private final AchievementService service;
    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> achievementArg;
    private final OptionalArg<Integer> amountArg;

    AchievementsCommand(AchievementCatalog catalog, AchievementService service) {
        super("achievements", "View achievements, contribute costs, and select an unlocked title.");
        this.catalog = catalog;
        this.service = service;
        requireNoPermission();
        setAllowsExtraArguments(false);
        actionArg = withRequiredArg("action", "list|status|contribute|title|reload", ArgTypes.STRING);
        achievementArg = withOptionalArg("achievement", "Achievement id, or 'none' for title", ArgTypes.STRING);
        amountArg = withOptionalArg("money", "Maximum currency to contribute", ArgTypes.INTEGER);
    }

    @Override protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store,
                                     @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef,
                                     @Nonnull World world) {
        try {
            String action = actionArg.get(ctx).toLowerCase(Locale.ROOT);
            switch (action) {
                case "list" -> list(playerRef);
                case "status" -> status(playerRef, requiredAchievement(ctx));
                case "contribute", "give" -> contribute(ctx, store, ref, playerRef, requiredAchievement(ctx));
                case "title" -> selectTitle(playerRef, requiredAchievement(ctx));
                case "reload" -> reload(playerRef);
                default -> playerRef.sendMessage(Message.raw("Unknown action. Use list, status, contribute, title, or reload."));
            }
        } catch (Exception error) {
            playerRef.sendMessage(Message.raw("Achievement action failed: " + rootMessage(error)));
        }
    }

    private void list(PlayerRef player) {
        player.sendMessage(Message.raw("Achievements:"));
        for (AchievementDefinition achievement : service.achievements()) {
            player.sendMessage(Message.raw("- " + achievement.id + " | " + achievement.name + " | "
                    + service.state(player.getUuid(), achievement)));
        }
    }

    private void status(PlayerRef player, AchievementDefinition achievement) {
        UUID id = player.getUuid();
        player.sendMessage(Message.raw(achievement.name + " [" + achievement.id + "] - title [" + achievement.title + "]"));
        player.sendMessage(Message.raw("State: " + service.state(id, achievement)));
        if (!achievement.prerequisites.isEmpty()) player.sendMessage(Message.raw("Prerequisites: " + String.join(", ", achievement.prerequisites)));
        if (achievement.cost.currency > 0) player.sendMessage(Message.raw("Currency: " + service.contributedCurrency(id, achievement.id)
                + "/" + achievement.cost.currency));
        Map<String, Integer> given = service.contributedItems(id, achievement.id);
        for (AchievementDefinition.ItemCost item : achievement.cost.items)
            player.sendMessage(Message.raw("Item " + item.id + ": " + given.getOrDefault(item.id, 0) + "/" + item.quantity));
        if (!achievement.cost.expeditions.isEmpty())
            player.sendMessage(Message.raw("Required expedition knowledge: " + String.join(", ", achievement.cost.expeditions)
                    + (service.knowledgeMet(id, achievement) ? " (met)" : " (missing)")));
    }

    private void contribute(CommandContext ctx, Store<EntityStore> store, Ref<EntityStore> ref,
                            PlayerRef playerRef, AchievementDefinition achievement) {
        UUID id = playerRef.getUuid();
        long remainingMoney = service.remainingCurrency(id, achievement);
        long limit = amountArg.provided(ctx) ? amountArg.get(ctx) : remainingMoney;
        AchievementContribution.Result result = AchievementContribution.contribute(service, store, ref, playerRef, achievement, limit);
        if (!result.costsBypassed())
            playerRef.sendMessage(Message.raw("Contributed to " + achievement.name + ": "
                    + AchievementContribution.describe(result) + "."));
        if (result.unlocked()) playerRef.sendMessage(Message.raw("Achievement unlocked: " + achievement.name
                + ". Activate it with /achievements title " + achievement.id));
        else status(playerRef, achievement);
        OneBlockAchievementPlugin plugin = OneBlockAchievementPlugin.get();
        if (plugin != null) plugin.updateNameplate(playerRef);
    }

    private void selectTitle(PlayerRef player, AchievementDefinition achievement) {
        service.selectTitle(player.getUuid(), achievement == null ? null : achievement.id);
        OneBlockAchievementPlugin plugin = OneBlockAchievementPlugin.get();
        if (plugin != null) plugin.updateNameplate(player);
        player.sendMessage(Message.raw(achievement == null ? "Chat title disabled." : "Active chat title: [" + achievement.title + "]"));
    }

    private void reload(PlayerRef player) throws Exception {
        catalog.load();
        player.sendMessage(Message.raw("Reloaded " + catalog.all().size() + " achievements."));
    }

    private AchievementDefinition requiredAchievement(CommandContext ctx) {
        if (!achievementArg.provided(ctx)) throw new IllegalArgumentException("An achievement id is required");
        String id = achievementArg.get(ctx);
        if ("none".equalsIgnoreCase(id) && "title".equalsIgnoreCase(actionArg.get(ctx))) return null;
        AchievementDefinition achievement = service.achievement(id);
        if (achievement == null) throw new IllegalArgumentException("Unknown achievement id: " + id);
        return achievement;
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
