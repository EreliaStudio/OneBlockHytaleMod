package com.EreliaStudio.OneBlockAchievement;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

final class AchievementNameplateFormatterTest {
    @Test void translatesTheTitleAndKeepsTheNameplateOnOneLine() {
        AchievementDefinition achievement = new AchievementDefinition();
        achievement.id = "beginner_miner";
        achievement.title = "Beginner Miner";
        Map<String, String> translations = Map.of(
                "server.achievement.nameplate.withTitle", "[{title} - Niv. {level}] {username}",
                achievement.titleTranslationKey(), "Mineur débutant"
        );

        String text = AchievementNameplateFormatter.format(
                achievement, 2, "Alice", translations::get);

        assertEquals("[Mineur débutant - Niv. 2] Alice", text);
        assertFalse(text.contains("\n"));
    }

    @Test void fallsBackToConfiguredEnglishTextWhenTranslationsAreMissing() {
        AchievementDefinition achievement = new AchievementDefinition();
        achievement.id = "unknown";
        achievement.title = "Configured Title";

        assertEquals("[Configured Title - Lv 3] Bob",
                AchievementNameplateFormatter.format(achievement, 3, "Bob", key -> key));
        assertEquals("[Lv 0] Bob",
                AchievementNameplateFormatter.format(null, 0, "Bob", key -> null));
    }

    @Test void replacesUnsupportedLineBreaksFromTranslations() {
        AchievementDefinition achievement = new AchievementDefinition();
        achievement.id = "beginner_miner";
        achievement.title = "Beginner Miner";

        String text = AchievementNameplateFormatter.format(achievement, 1, "Alice", key ->
                key.endsWith("withTitle") ? "{title}\n{username}" : "Mineur\ndébutant");

        assertEquals("Mineur débutant Alice", text);
    }
}
