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

public final class OneBlockExchangeRecipeLoader
{
    private static final Gson GSON = new Gson();

    private OneBlockExchangeRecipeLoader()
    {
    }

    public static Map<String, OneBlockExchangeService.ExchangeDefinition> loadExchangeMap(Class<?> owner,
                                                                                           String benchResourcePath,
                                                                                           String recipeFolderResourcePath)
    {
        JsonObject benchJson = loadJsonObject(owner, benchResourcePath);
        Map<String, OneBlockExchangeService.ExchangeDefinition> result = new HashMap<>();

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
                    continue;
                }

                JsonObject tags = recipeJson.getAsJsonObject("Tags");
                if (tags == null)
                {
                    continue;
                }

                String outputId = readStringFromTags(tags, "OneBlockExchangeOutputId");
                if (outputId == null || outputId.isEmpty())
                {
                    continue;
                }

                int quantity = readIntFromTags(tags, "OneBlockExchangeOutputQuantity", 1);
                if (quantity < 1)
                {
                    quantity = 1;
                }

                String expeditionId = readStringFromTags(tags, "OneBlockExchangeExpedition");
                if (expeditionId == null || expeditionId.isEmpty())
                {
                    expeditionId = (categoryExpeditionId == null || categoryExpeditionId.isEmpty())
                            ? OneBlockExpeditionResolver.DEFAULT_EXPEDITION
                            : categoryExpeditionId;
                }

                String unlockId = readStringFromTags(tags, "OneBlockExchangeUnlockId");

                result.put(recipeItemId, new OneBlockExchangeService.ExchangeDefinition(
                        expeditionId,
                        outputId,
                        quantity,
                        unlockId
                ));
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

        throw new RuntimeException("Missing exchange recipe: " + recipeItemId);
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
