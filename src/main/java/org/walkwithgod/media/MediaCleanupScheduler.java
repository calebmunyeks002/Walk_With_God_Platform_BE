package org.walkwithgod.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Deletes media rows whose status is DELETED and that are older than 24h,
 * plus their physical files. Runs nightly.
 */
@Component
public class MediaCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(MediaCleanupScheduler.class);
    private final MediaRepository repo;
    private final StorageService storage;

    public MediaCleanupScheduler(MediaRepository r, StorageService s) {
        repo = r;
        storage = s;
    }

    @Scheduled(cron = "0 30 3 * * *") // 03:30 daily
    @Transactional
    public void purgeDeleted() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        List<Media> old = repo.findByStatusOrderByCreatedAtAsc(
                ModerationStatus.DELETED,
                org.springframework.data.domain.PageRequest.of(0, 500)).getContent();

        int n = 0;
        for (Media m : old) {
            if (m.getCreatedAt().isBefore(cutoff)) {
                storage.delete(m.getStorageKey());
                repo.delete(m);
                n++;
            }
        }
        if (n > 0)
            log.info("Purged {} deleted media records", n);
    }
}