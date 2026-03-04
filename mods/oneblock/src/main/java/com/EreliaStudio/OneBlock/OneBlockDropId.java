package com.EreliaStudio.OneBlock;

import java.util.Locale;

public final class OneBlockDropId
{
    public enum Kind
    {
        ITEM,
        ENTITY
    }

    private static final String ENTITY_PREFIX = "entity:";
    private static final String NPC_PREFIX = "npc:";
    private static final String MOB_PREFIX = "mob:";
    private static final String ITEM_PREFIX = "item:";

    private final Kind kind;
    private final String id;

    private OneBlockDropId(Kind kind, String id)
    {
        this.kind = kind;
        this.id = id;
    }

    public Kind getKind()
    {
        return kind;
    }

    public String getId()
    {
        return id;
    }

    public boolean isEntity()
    {
        return kind == Kind.ENTITY;
    }

    public static OneBlockDropId parse(String raw)
    {
        if (raw == null)
        {
            return new OneBlockDropId(Kind.ITEM, "");
        }

        String trimmed = raw.trim();
        if (trimmed.isEmpty())
        {
            return new OneBlockDropId(Kind.ITEM, "");
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.startsWith(ENTITY_PREFIX))
        {
            return new OneBlockDropId(Kind.ENTITY, trimmed.substring(ENTITY_PREFIX.length()).trim());
        }
        if (lower.startsWith(NPC_PREFIX))
        {
            return new OneBlockDropId(Kind.ENTITY, trimmed.substring(NPC_PREFIX.length()).trim());
        }
        if (lower.startsWith(MOB_PREFIX))
        {
            return new OneBlockDropId(Kind.ENTITY, trimmed.substring(MOB_PREFIX.length()).trim());
        }
        if (lower.startsWith(ITEM_PREFIX))
        {
            return new OneBlockDropId(Kind.ITEM, trimmed.substring(ITEM_PREFIX.length()).trim());
        }

        return new OneBlockDropId(Kind.ITEM, trimmed);
    }

    public static String entityDropId(String entityId)
    {
        if (entityId == null)
        {
            return null;
        }
        String trimmed = entityId.trim();
        if (trimmed.isEmpty())
        {
            return null;
        }
        return ENTITY_PREFIX + trimmed;
    }
}
