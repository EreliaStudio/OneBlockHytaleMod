package com.EreliaStudio.OneBlockIslands;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public final class IslandRecord {
    String id;
    String worldName;
    UUID ownerUuid;
    Set<UUID> members = new LinkedHashSet<>();
    Set<UUID> pendingInvites = new LinkedHashSet<>();
    Set<UUID> bannedVisitors = new LinkedHashSet<>();
    boolean allowVisitors;
    String createdAt;

    IslandRecord() {}

    IslandRecord(UUID ownerUuid, String worldName) {
        this.id = UUID.randomUUID().toString();
        this.worldName = worldName;
        this.ownerUuid = ownerUuid;
        this.createdAt = Instant.now().toString();
    }

    public String id() { return id; }
    public String worldName() { return worldName; }
    public UUID ownerUuid() { return ownerUuid; }
    public Set<UUID> members() { return Set.copyOf(members); }
    public Set<UUID> pendingInvites() { return Set.copyOf(pendingInvites); }
    public boolean canEdit(UUID playerUuid) {
        return playerUuid != null && (playerUuid.equals(ownerUuid) || members.contains(playerUuid));
    }
    public boolean canEnter(UUID playerUuid) {
        return canEdit(playerUuid) || (allowVisitors && !bannedVisitors.contains(playerUuid));
    }
}
