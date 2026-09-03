package com.EreliaStudio.OneBlockAchievement;

import com.EreliaStudio.OneBlock.OneBlockPlugin;
import com.EreliaStudio.OneBlock.OneBlockProgressListener;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public final class OneBlockAchievementPlugin extends JavaPlugin implements OneBlockProgressListener {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockAchievementPlugin instance;
    private AchievementCatalog catalog;
    private AchievementService service;
    private OneBlockPlugin oneBlock;

    public OneBlockAchievementPlugin(@Nonnull JavaPluginInit init) { super(init); instance = this; }

    static OneBlockAchievementPlugin get() { return instance; }

    @Override protected void setup() {
        catalog = new AchievementCatalog(getDataDirectory().resolve("achievements.json"));
        PlayerProgressStore progress = new PlayerProgressStore(getDataDirectory().resolve("players.json"));
        try {
            catalog.load();
            progress.load();
        } catch (IOException error) {
            throw new IllegalStateException("Cannot load achievement data: " + error.getMessage(), error);
        }
        service = new AchievementService(catalog, progress);
        getCommandRegistry().registerCommand(new AchievementsCommand(catalog, service));
        getCommandRegistry().registerCommand(new AchievementCommand(service));
        getEventRegistry().registerGlobal(PlayerChatEvent.class, this::formatChat);
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> updateNameplate(event.getPlayerRef()));
        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> {
            PlayerRef player = event.getHolder() == null ? null : event.getHolder().getComponent(PlayerRef.getComponentType());
            updateNameplate(player);
        });

        oneBlock = OneBlockPlugin.getInstance();
        if (oneBlock != null) oneBlock.addProgressListener(this);
        LOGGER.at(Level.INFO).log("Loaded " + catalog.all().size() + " achievement definitions from " + catalog.path());
    }

    @Override protected void shutdown() {
        if (oneBlock != null) oneBlock.removeProgressListener(this);
        oneBlock = null;
        service = null;
        catalog = null;
        instance = null;
    }

    private void formatChat(PlayerChatEvent event) {
        if (service == null || event.getSender() == null) return;
        String title = service.activeTitle(event.getSender().getUuid());
        int level = service.level(event.getSender().getUuid());
        event.setFormatter((sender, content) -> Message.raw(prefix(title, level) + " "
                + sender.getUsername() + " : " + content));
    }

    void updateNameplate(PlayerRef player) {
        if (player == null || player.getReference() == null || !player.getReference().isValid() || service == null) return;
        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref.getStore();
        String title = service.activeTitle(player.getUuid());
        String text = prefix(title, service.level(player.getUuid())) + "\n" + player.getUsername();
        store.ensureAndGetComponent(ref, Nameplate.getComponentType()).setText(text);
    }

    private void updateNameplate(Ref<EntityStore> ref) {
        if (ref == null || !ref.isValid()) return;
        updateNameplate(ref.getStore().getComponent(ref, PlayerRef.getComponentType()));
    }

    static String prefix(String title, int level) {
        return title == null ? "[Lv " + level + "]" : "[" + title + " - Lv " + level + "]";
    }

    @Override public void onExpeditionUnlocked(UUID playerId, String expeditionId) {
        if (service == null) return;
        int before = service.level(playerId);
        service.learnExpedition(playerId, expeditionId);
        Universe universe = Universe.get();
        PlayerRef player = universe == null ? null : universe.getPlayer(playerId);
        if (player != null) {
            updateNameplate(player);
            if (service.level(playerId) > before)
                player.sendMessage(Message.raw("An achievement was unlocked by learning expedition " + expeditionId + "."));
        }
    }
}
