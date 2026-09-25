package org.walkwithgod.settings;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(100)
public class PlatformSettingsSeeder implements CommandLineRunner {

    private final PlatformSettingRepository repo;

    public PlatformSettingsSeeder(PlatformSettingRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0)
            return; // already seeded

        List<PlatformSetting> defaults = List.of(
                // GENERAL
                seed("platformName", "WalkWithGod", "STRING", "GENERAL", "Site name shown in the header and emails."),
                seed("tagline", "Faith • Purpose • Growth", "STRING", "GENERAL", "Short tagline for the platform."),
                seed("supportEmail", "support@walkwithgod.local", "STRING", "GENERAL",
                        "Email shown on support and legal pages."),
                seed("timezone", "UTC", "STRING", "GENERAL", "Default timezone for dates and scheduling."),

                // COMMUNITY
                seed("allowPosts", "true", "BOOLEAN", "COMMUNITY", "Allow members to create new posts."),
                seed("maxPostLength", "5000", "INT", "COMMUNITY", "Maximum characters per post."),
                seed("enableComments", "true", "BOOLEAN", "COMMUNITY", "Allow comments on posts."),
                seed("requirePostApproval", "false", "BOOLEAN", "COMMUNITY", "Hold new posts for admin approval."),

                // MENTORSHIP
                seed("mentorApplicationsOpen", "true", "BOOLEAN", "MENTORSHIP", "Accept new mentor applications."),
                seed("maxActiveMentees", "10", "INT", "MENTORSHIP", "Maximum active mentees per mentor."),
                seed("requireVerifiedMentors", "true", "BOOLEAN", "MENTORSHIP",
                        "Only verified mentors appear in the directory."),

                // TRIVIA
                seed("triviaEnabled", "true", "BOOLEAN", "TRIVIA", "Enable the trivia module."),
                seed("questionsPerSession", "5", "INT", "TRIVIA", "Number of questions per trivia session."),
                seed("defaultDifficulty", "EASY", "STRING", "TRIVIA", "Default difficulty for new sessions."),

                // DEVOTIONS
                seed("autoFeatureDailyDevotion", "false", "BOOLEAN", "DEVOTIONS",
                        "Automatically feature the latest devotion."),
                seed("devotionsVisibleToGuests", "true", "BOOLEAN", "DEVOTIONS",
                        "Allow logged-out visitors to read devotions."),

                // MODERATION
                seed("autoHideReportedContent", "false", "BOOLEAN", "MODERATION",
                        "Hide posts after a report threshold is reached."),
                seed("reportThreshold", "3", "INT", "MODERATION", "Number of reports before content is auto-hidden."),

                // SAFETY
                seed("maintenanceMode", "false", "BOOLEAN", "SAFETY",
                        "Put the platform in read-only maintenance mode."),
                seed("maintenanceMessage", "We’re briefly offline for improvements. Please check back soon.", "STRING",
                        "SAFETY", "Message shown during maintenance."));

        repo.saveAll(defaults);
    }

    private static PlatformSetting seed(String key, String value, String type, String category, String desc) {
        PlatformSetting s = new PlatformSetting();
        s.setKey(key);
        s.setValue(value);
        s.setValueType(type);
        s.setCategory(category);
        s.setDescription(desc);
        return s;
    }
}