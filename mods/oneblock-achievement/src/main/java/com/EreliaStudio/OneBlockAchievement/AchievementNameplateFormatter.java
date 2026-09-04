package com.EreliaStudio.OneBlockAchievement;

import java.util.function.Function;

final class AchievementNameplateFormatter {
    private static final String WITH_TITLE_KEY = "server.achievement.nameplate.withTitle";
    private static final String WITHOUT_TITLE_KEY = "server.achievement.nameplate.withoutTitle";

    private AchievementNameplateFormatter() {}

    static String format(AchievementDefinition achievement, int level, String username,
                         Function<String, String> translator) {
        String patternKey = achievement == null ? WITHOUT_TITLE_KEY : WITH_TITLE_KEY;
        String pattern = translated(translator, patternKey);
        if (pattern == null) {
            pattern = achievement == null
                    ? "[Lv {level}] {username}"
                    : "[{title} - Lv {level}] {username}";
        }

        String title = "";
        if (achievement != null) {
            title = translated(translator, achievement.titleTranslationKey());
            if (title == null) title = achievement.title;
        }

        return oneLine(pattern
                .replace("{title}", title)
                .replace("{level}", Integer.toString(level))
                .replace("{username}", username));
    }

    private static String translated(Function<String, String> translator, String key) {
        String value = translator == null ? null : translator.apply(key);
        return value == null || value.equals(key) ? null : value;
    }

    private static String oneLine(String value) {
        return value.replace('\r', ' ').replace('\n', ' ');
    }
}
