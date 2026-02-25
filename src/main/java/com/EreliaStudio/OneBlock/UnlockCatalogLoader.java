package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public final class UnlockCatalogLoader
{
    private static final Gson GSON = new Gson();

    private UnlockCatalogLoader()
    {
    }

    public static UnlockCatalog loadFromResources(Class<?> owner, String resourcePath)
    {
        try (var in = owner.getResourceAsStream(resourcePath))
        {
            if (in == null)
            {
                throw new IllegalStateException("Missing resource: " + resourcePath);
            }

            String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            UnlockCatalog catalog = GSON.fromJson(json, UnlockCatalog.class);

            if (catalog == null || catalog.categories == null)
            {
                throw new IllegalStateException("Invalid unlock catalog JSON: " + resourcePath);
            }

            return catalog;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to load unlock catalog: " + resourcePath, e);
        }
    }
}