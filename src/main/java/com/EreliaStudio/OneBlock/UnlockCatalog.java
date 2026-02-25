package com.EreliaStudio.OneBlock;

import java.util.List;

public final class UnlockCatalog
{
    public int version;
    public List<Category> categories;

    public static final class Category
    {
        public String id;
        public String nameKey;
        public String descriptionKey;
        public List<Entry> entries;
    }

    public static final class Entry
    {
        public String id;
        public String nameKey;
        public String descriptionKey;
        public String dropItemId;
        public List<Cost> cost;
    }

    public static final class Cost
    {
        public String itemId;
        public int quantity;
    }
}