package com.EreliaStudio.OneBlockAchievement;

import java.util.ArrayList;
import java.util.List;

public final class AchievementDefinition {
    public String id;
    public String name;
    public String title;
    public List<String> prerequisites = new ArrayList<>();
    public Cost cost = new Cost();

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
