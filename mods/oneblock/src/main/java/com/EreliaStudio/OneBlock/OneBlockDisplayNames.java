package com.EreliaStudio.OneBlock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/** Player-facing names generated from optional expedition DisplayName values. */
public final class OneBlockDisplayNames
{
    private static final Map<String, String> DISPLAY_NAMES;

    static
    {
        Map<String, String> names = new HashMap<>();
        names.put("Default", "Meadow");
        DISPLAY_NAMES = Collections.unmodifiableMap(names);
    }

    private OneBlockDisplayNames() {}

    public static String get(String expeditionId)
    {
        if (expeditionId == null || expeditionId.isBlank())
        {
            return "Unknown";
        }

        String displayName = DISPLAY_NAMES.get(expeditionId);
        if (displayName != null)
        {
            return displayName;
        }

        return expeditionId
                .replace('_', ' ')
                .replaceAll("(?<=[a-z0-9])(?=[A-Z])", " ")
                .trim();
    }
}
