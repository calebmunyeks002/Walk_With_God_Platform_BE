package org.walkwithgod.prayer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Component
public class PrayerScheduler {

    private static final Logger log = LoggerFactory.getLogger(PrayerScheduler.class);
    private final PrayerRepository repo;
    private final Random rng = new Random();

    public PrayerScheduler(PrayerRepository r) {
        repo = r;
    }

    /** Every day at 06:00 server time. */
    @Scheduled(cron = "0 0 6 * * *")
    @Transactional
    public void seedMorning() {
        seed("MORNING", LocalDate.now());
    }

    /** Every day at 18:00 server time. */
    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void seedEvening() {
        seed("EVENING", LocalDate.now());
    }

    /** Also invoked on app startup (catch-up if server was down). */
    @Transactional
    public void seedIfMissing(String slot, LocalDate date) {
        if (!repo.existsBySlotAndPrayerDate(slot, date)) {
            seed(slot, date);
        }
    }

    private void seed(String slot, LocalDate date) {
        if (repo.existsBySlotAndPrayerDate(slot, date))
            return;

        Template t = pick(slot);
        Prayer p = new Prayer();
        p.setSlot(slot);
        p.setPrayerDate(date);
        p.setTitle(t.title);
        p.setBody(t.body);
        p.setScripture(t.scripture);
        p.setSystemGenerated(true);
        repo.save(p);

        log.info("Seeded {} prayer for {}", slot, date);
    }

    private Template pick(String slot) {
        List<Template> pool = "MORNING".equals(slot) ? MORNING : EVENING;
        return pool.get(rng.nextInt(pool.size()));
    }

    private record Template(String title, String body, String scripture) {
    }

    private static final List<Template> MORNING = List.of(
            new Template(
                    "A New Morning of Mercy",
                    "Father, thank You for the gift of a new day. Your mercies are new every morning. " +
                            "Guide my steps today, let my words bring life, and let my heart stay anchored in You. " +
                            "In Jesus' name, Amen.",
                    "Lamentations 3:22-23"),
            new Template(
                    "Strength for Today",
                    "Lord, I surrender this day to You. Give me strength for every task, wisdom for every " +
                            "decision, and peace in every moment. May Your presence go before me. Amen.",
                    "Isaiah 40:31"),
            new Template(
                    "A Grateful Heart",
                    "Heavenly Father, thank You for waking me up today. I choose gratitude over worry, faith " +
                            "over fear. Let my life reflect Your love to everyone I meet. Amen.",
                    "Psalm 118:24"),
            new Template(
                    "Directed Steps",
                    "Lord, I do not know what this day holds, but I know Who holds this day. Direct my paths, " +
                            "order my steps, and let Your will be done in my life. Amen.",
                    "Proverbs 16:9"),
            new Template(
                    "Armor Up",
                    "Father, clothe me today with the full armor of God. Guard my mind, purify my heart, and " +
                            "let me stand firm in faith. In Jesus' name, Amen.",
                    "Ephesians 6:10-18"));

    private static final List<Template> EVENING = List.of(
            new Template(
                    "Rest in His Presence",
                    "Lord, as this day closes, I rest in Your presence. Forgive me where I have fallen short, " +
                            "heal what is broken, and quiet my heart. I trust You with tomorrow. Amen.",
                    "Psalm 4:8"),
            new Template(
                    "Reflection and Release",
                    "Father, I release every burden of today into Your hands. Thank You for Your faithfulness. " +
                            "Let me sleep in peace and wake with hope. Amen.",
                    "1 Peter 5:7"),
            new Template(
                    "Gratitude at Day's End",
                    "God, thank You for every blessing, every lesson, and every grace today. Let my last " +
                            "thoughts be of You and my first thoughts tomorrow be praise. Amen.",
                    "Psalm 63:6-7"),
            new Template(
                    "Cover My Household",
                    "Lord, I lift up my home to You tonight. Cover every person under my roof with Your " +
                            "protection and peace. Let Your angels watch over us. Amen.",
                    "Psalm 91:11"),
            new Template(
                    "A Quiet Heart",
                    "Father, silence the noise inside me. Let Your peace rule my heart and Your truth anchor " +
                            "my soul. I lay this day at Your feet. Amen.",
                    "John 14:27"));
}