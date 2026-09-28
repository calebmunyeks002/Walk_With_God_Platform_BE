package org.walkwithgod.prayer;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/prayers")
public class PrayerController {

    private final PrayerRepository repo;
    private final PrayerScheduler scheduler;

    public PrayerController(PrayerRepository r, PrayerScheduler s) {
        repo = r;
        scheduler = s;
    }

    public record View(
            String id, String slot, String title, String body,
            String scripture, String date, boolean systemGenerated) {
    }

    @GetMapping("/today")
    public Map<String, View> today() {
        LocalDate today = LocalDate.now();

        // Self-heal: if the scheduler missed (server was down), seed on demand.
        scheduler.seedIfMissing("MORNING", today);
        scheduler.seedIfMissing("EVENING", today);

        Map<String, View> out = new HashMap<>();
        repo.findBySlotAndPrayerDate("MORNING", today)
                .ifPresent(p -> out.put("morning", toView(p)));
        repo.findBySlotAndPrayerDate("EVENING", today)
                .ifPresent(p -> out.put("evening", toView(p)));
        return out;
    }

    private View toView(Prayer p) {
        return new View(
                p.getId().toString(),
                p.getSlot(),
                p.getTitle(),
                p.getBody(),
                p.getScripture(),
                p.getPrayerDate().toString(),
                p.isSystemGenerated());
    }
}