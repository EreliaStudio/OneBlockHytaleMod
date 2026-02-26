package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OneBlockUnlockRecipeLoader
{
    private static final Gson GSON = new Gson();

    private OneBlockUnlockRecipeLoader()
    {
    }

    public static Map<String, OneBlockUnlockService.UnlockDefinition> loadConsumableToDropMap(Class<?> owner,
                                                                                               String benchResourcePath,
                                                                                               String recipeFolderResourcePath)
    {
        JsonObject benchJson = loadJsonObject(owner, benchResourcePath);
        Map<String, OneBlockUnlockService.UnlockDefinition> result = new HashMap<>();

        JsonArray categories = benchJson
                .getAsJsonObject("BlockType")
                .getAsJsonObject("Bench")
                .getAsJsonArray("Categories");

        if (categories == null)
        {
            return result;
        }

        for (JsonElement categoryElement : categories)
        {
            if (!categoryElement.isJsonObject())
            {
                continue;
            }

            JsonObject categoryJson = categoryElement.getAsJsonObject();
            String categoryId = readString(categoryJson, "Id");
            String categoryExpeditionId = expeditionFromCategoryId(categoryId);
            JsonArray recipes = categoryJson.getAsJsonArray("Recipes");
            if (recipes == null)
            {
                continue;
            }

            for (JsonElement recipeIdElement : recipes)
            {
                if (!recipeIdElement.isJsonPrimitive())
                {
                    continue;
                }

                String recipeItemId = recipeIdElement.getAsString();
                if (recipeItemId == null || recipeItemId.isEmpty())
                {
                    continue;
                }

                JsonObject recipeJson;
                try
                {
                    recipeJson = loadRecipeJson(owner, recipeFolderResourcePath, categoryExpeditionId, recipeItemId);
                }
                catch (RuntimeException ignored)
                {
                    // Not an unlock recipe stored in the unlock folders.
                    continue;
                }

                String recipeConsumableId = readString(recipeJson, "Id");
                if (recipeConsumableId == null || recipeConsumableId.isEmpty())
                {
                    continue;
                }

                JsonObject tags = recipeJson.getAsJsonObject("Tags");
                if (tags == null)
                {
                    continue;
                }

                String dropItemId = readStringFromTags(tags, "OneBlockUnlockDropId");
                if (dropItemId == null || dropItemId.isEmpty())
                {
                    String entityId = readStringFromTags(tags, "OneBlockUnlockEntityId");
                    if (entityId == null || entityId.isEmpty())
                    {
                        continue;
                    }
                    dropItemId = OneBlockDropId.entityDropId(entityId);
                    if (dropItemId == null || dropItemId.isEmpty())
                    {
                        continue;
                    }
                }

                String expeditionId = readStringFromTags(tags, "OneBlockUnlockExpedition");
                if (expeditionId == null || expeditionId.isEmpty())
                {
                    expeditionId = (categoryExpeditionId == null || categoryExpeditionId.isEmpty())
                            ? OneBlockExpeditionResolver.DEFAULT_EXPEDITION
                            : categoryExpeditionId;
                }

                int weight = readIntFromTags(tags, "OneBlockUnlockWeight", 1);
                if (weight < 1)
                {
                    weight = 1;
                }

                result.put(recipeConsumableId, new OneBlockUnlockService.UnlockDefinition(expeditionId, dropItemId, weight));
            }
        }

        return result;
    }

    private static JsonObject loadJsonObject(Class<?> owner, String resourcePath)
    {
        try (var in = owner.getResourceAsStream(resourcePath))
        {
            if (in == null)
            {
                throw new IllegalStateException("Missing resource: " + resourcePath);
            }

            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject out = GSON.fromJson(json, JsonObject.class);

            if (out == null)
            {
                throw new IllegalStateException("Invalid JSON object in resource: " + resourcePath);
            }

            return out;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load JSON resource: " + resourcePath, e);
        }
    }

    private static String readString(JsonObject obj, String key)
    {
        if (obj == null || key == null || key.isEmpty() || !obj.has(key))
        {
            return null;
        }

        JsonElement e = obj.get(key);
        if (e == null || !e.isJsonPrimitive())
        {
            return null;
        }

        return e.getAsString();
    }

    private static JsonObject loadRecipeJson(Class<?> owner,
                                             String baseFolder,
                                             String expeditionId,
                                             String recipeItemId)
    {
        List<String> candidates = new ArrayList<>();
        if (expeditionId != null && !expeditionId.isEmpty())
        {
            candidates.add(baseFolder + "/Expedition_" + expeditionId + "/" + recipeItemId + ".json");
        }
        candidates.add(baseFolder + "/" + recipeItemId + ".json");

        RuntimeException last = null;
        for (String path : candidates)
        {
            try
            {
                return loadJsonObject(owner, path);
            }
            catch (RuntimeException ex)
            {
                last = ex;
            }
        }

        if (last != null)
        {
            throw last;
        }

        throw new RuntimeException("Missing unlock recipe: " + recipeItemId);
    }

    private static String expeditionFromCategoryId(String categoryId)
    {
        if (categoryId == null || categoryId.isEmpty())
        {
            return null;
        }

        int idx = categoryId.lastIndexOf("Expedition_");
        if (idx >= 0)
        {
            return categoryId.substring(idx + "Expedition_".length());
        }

        return null;
    }

    private static String readStringFromTags(JsonObject tags, String key)
    {
        if (tags == null || key == null || key.isEmpty() || !tags.has(key))
        {
            return null;
        }

        JsonElement e = tags.get(key);
        if (e == null)
        {
            return null;
        }

        if (e.isJsonPrimitive())
        {
            return e.getAsString();
        }

        if (e.isJsonArray())
        {
            JsonArray array = e.getAsJsonArray();
            if (!array.isEmpty())
            {
                JsonElement first = array.get(0);
                if (first != null && first.isJsonPrimitive())
                {
                    return first.getAsString();
                }
            }
        }

        return null;
    }

    private static int readIntFromTags(JsonObject tags, String key, int defaultValue)
    {
        if (tags == null || key == null || key.isEmpty() || !tags.has(key))
        {
            return defaultValue;
        }

        JsonElement e = tags.get(key);
        if (e == null)
        {
            return defaultValue;
        }

        if (e.isJsonPrimitive())
        {
            try
            {
                return e.getAsInt();
            }
            catch (Exception ignored)
            {
                return defaultValue;
            }
        }

        if (e.isJsonArray())
        {
            JsonArray array = e.getAsJsonArray();
            if (!array.isEmpty())
            {
                JsonElement first = array.get(0);
                if (first != null && first.isJsonPrimitive())
                {
                    try
                    {
                        return first.getAsInt();
                    }
                    catch (Exception ignored)
                    {
                        return defaultValue;
                    }
                }
            }
        }

        return defaultValue;
    }
}
