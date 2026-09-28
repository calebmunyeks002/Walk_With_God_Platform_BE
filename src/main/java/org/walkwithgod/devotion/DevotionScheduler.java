package org.walkwithgod.devotion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
public class DevotionScheduler {

    private static final Logger log = LoggerFactory.getLogger(DevotionScheduler.class);
    private final DevotionRepository repo;
    private final Random rng = new Random();

    public DevotionScheduler(DevotionRepository r) {
        repo = r;
    }

    /** Every day at 00:05 server time. */
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void generateDaily() {
        LocalDate today = LocalDate.now();
        Instant start = today.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        boolean humanExists = repo.existsByPublishedAtBetweenAndSystemGeneratedFalse(start, end);
        if (humanExists) {
            log.info("Human devotion exists for {} — skipping system generation.", today);
            return;
        }

        Template t = pick();
        Devotion d = new Devotion();
        d.setTitle(t.title);
        d.setScripture(t.scripture);
        d.setBody(t.body);
        d.setPublished(true);
        d.setFeatured(false);
        d.setSystemGenerated(true);
        d.setPublishedAt(Instant.now());
        d.setAuthor(resolveSystemAuthor());
        repo.save(d);

        log.info("Generated system devotion for {}", today);
    }

    /** If your Devotion requires an author, plug a system user in here. */
    private org.walkwithgod.user.AppUser resolveSystemAuthor() {
        // Option A: return null if your entity allows it.
        // Option B: seed a system user in V14 and look it up.
        return null;
    }

    private Template pick() {
        return POOL.get(rng.nextInt(POOL.size()));
    }

    private record Template(String title, String scripture, String body) {
    }

    private static final List<Template> POOL = List.of(
            new Template("Trusting in the Unseen",
                    "Hebrews 11:1",
                    "Faith is not the absence of questions; it is the presence of trust. Today, " +
                            "surrender what you cannot see to the One who sees all. Let your heart rest in " +
                            "the character of God rather than the clarity of your circumstances."),
            new Template("A Quiet Strength",
                    "Isaiah 30:15",
                    "In repentance and rest is your salvation; in quietness and trust is your strength. " +
                            "The world shouts for your attention, but God whispers for your heart. Be still today. " +
                            "Let Him be your strength."),
            new Template("New Every Morning",
                    "Lamentations 3:22-23",
                    "His mercies are new every morning. Whatever yesterday held—failure, fear, or grief—" +
                            "today is a fresh page. Step into it with hope, because His faithfulness is great."),
            new Template("The Peace That Guards",
                    "Philippians 4:6-7",
                    "Do not be anxious about anything. Bring every concern to God in prayer, with " +
                            "thanksgiving. His peace—a peace that surpasses understanding—will guard your heart " +
                            "and mind in Christ Jesus."),
            new Template("Love in Action",
                    "1 John 3:18",
                    "Love is not a feeling we wait for; it is a choice we make. Today, let your love be " +
                            "visible—in a kind word, a listening ear, a generous hand. Faith without works is " +
                            "empty, and love without action is unseen."),
            new Template("The Good Shepherd",
                    "Psalm 23:1-3",
                    "The Lord is your shepherd. He leads you beside still waters and restores your soul. " +
                            "You lack nothing when you walk with Him. Today, follow His voice and trust His lead."),
            new Template("Rooted and Built Up",
                    "Colossians 2:6-7",
                    "Just as you received Christ, continue to walk in Him—rooted, built up, strengthened " +
                            "in the faith, overflowing with thanksgiving. Growth is not a sprint; it is a daily " +
                            "walk with the One who never leaves you."));
}