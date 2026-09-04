package com.EreliaStudio.OneBlockAchievement;

import com.hypixel.hytale.server.core.Message;

import java.util.ArrayList;
import java.util.List;

public final class AchievementDefinition {
    public String id;
    public String name;
    public String title;
    public List<String> prerequisites = new ArrayList<>();
    public Cost cost = new Cost();

    String nameTranslationKey() { return "server.achievement.definition." + id + ".name"; }
    String titleTranslationKey() { return "server.achievement.definition." + id + ".title"; }
    Message localizedName() { return Message.translation(nameTranslationKey()); }
    Message localizedTitle() { return Message.translation(titleTranslationKey()); }

    public static final class Cost {
        public long currency;
        public List<ItemCost> items = new ArrayList<>();
        public List<String> expeditions = new ArrayList<>();
    }

    public static final class ItemCost {
        public String id;
        public int quantity;
    }
}
