package org.walkwithgod.trivia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.walkwithgod.notification.NotificationService;
import org.walkwithgod.notification.NotificationType;
import org.walkwithgod.user.AppUser;
import org.walkwithgod.user.UserRepository;

import java.time.LocalDate;

@Component
public class TriviaDailyNotifier {

    private static final Logger log = LoggerFactory.getLogger(TriviaDailyNotifier.class);

    private final UserRepository users;
    private final NotificationService notifications;

    public TriviaDailyNotifier(UserRepository users, NotificationService notifications) {
        this.users = users;
        this.notifications = notifications;
    }

    /** Manual trigger for testing — call from an admin endpoint. */
    public long runNow() {
        LocalDate today = LocalDate.now();
        long sent = 0;
        for (AppUser u : users.findAll()) {
            if (!u.isEnabled() || u.isSuspended())
                continue;
            notifications.create(
                    u.getId(), null, NotificationType.ADMIN_BROADCAST,
                    "🎯 Today's Bible Trivia is live!",
                    "5 fresh questions are ready. Pick your difficulty — easy, medium or hard.",
                    "/trivia", false);
            sent++;
        }
        return sent;
    }

    /**
     * Fires every day at 00:00 server time.
     * Notifies every active user that a new daily quiz is available.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Africa/Nairobi")
    public void notifyNewDay() {
        LocalDate today = LocalDate.now();
        log.info("Trivia daily notifier running for {}", today);

        long sent = 0;
        for (AppUser u : users.findAll()) {
            if (!u.isEnabled() || u.isSuspended())
                continue;

            notifications.create(
                    u.getId(),
                    null,
                    NotificationType.ADMIN_BROADCAST,
                    "🎯 Today's Bible Trivia is live!",
                    "5 fresh questions are ready. Pick your difficulty — easy, medium or hard.",
                    "/trivia",
                    false // in-app only; don't spam email every midnight
            );
            sent++;
        }

        log.info("Trivia daily notifier sent {} notifications", sent);
    }
}