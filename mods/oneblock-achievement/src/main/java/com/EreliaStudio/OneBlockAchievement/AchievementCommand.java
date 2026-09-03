package com.EreliaStudio.OneBlockAchievement;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

final class AchievementCommand extends AbstractPlayerCommand {
    private final AchievementService service;

    AchievementCommand(AchievementService service) {
        super("achievement", "Open the achievement and title selector.");
        this.service = service;
        requireNoPermission();
        setAllowsExtraArguments(false);
    }

    @Override protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store,
                                     @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef,
                                     @Nonnull World world) {
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player != null) player.getPageManager().openCustomPage(ref, store, new AchievementPage(playerRef, service));
    }
}
