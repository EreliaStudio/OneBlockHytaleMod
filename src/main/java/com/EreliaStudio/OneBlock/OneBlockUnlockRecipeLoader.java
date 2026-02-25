package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class OneBlockUnlockRecipeLoader
{
    private static final Gson GSON = new Gson();

    private OneBlockUnlockRecipeLoader()
    {
    }

    public static Map<String, String> loadConsumableToDropMap(Class<?> owner,
                                                               String benchResourcePath,
                                                               String recipeFolderResourcePath)
    {
        JsonObject benchJson = loadJsonObject(owner, benchResourcePath);
        Map<String, String> result = new HashMap<>();

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

                String recipePath = recipeFolderResourcePath + "/" + recipeItemId + ".json";
                JsonObject recipeJson = loadJsonObject(owner, recipePath);

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

                JsonArray dropTags = tags.getAsJsonArray("OneBlockUnlockDropId");
                if (dropTags == null || dropTags.isEmpty())
                {
                    continue;
                }

                String dropItemId = dropTags.get(0).getAsString();
                if (dropItemId == null || dropItemId.isEmpty())
                {
                    continue;
                }

                result.put(recipeConsumableId, dropItemId);
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
}
