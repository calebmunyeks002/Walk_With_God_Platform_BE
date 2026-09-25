package org.walkwithgod.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.walkwithgod.bible.BibleHighlightRepository;
import org.walkwithgod.bible.BibleReadChapterRepository;
import org.walkwithgod.community.PostRepository;
import org.walkwithgod.devotion.DevotionRepository;
import org.walkwithgod.trivia.TriviaSessionRepository;
import org.walkwithgod.trivia.TriviaSession;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users/me")
public class UserStatsController {

    private final PostRepository posts;
    private final DevotionRepository devotions;
    private final BibleReadChapterRepository reads;
    private final BibleHighlightRepository highlights;
    private final TriviaSessionRepository triviaSessions;

    public UserStatsController(
            PostRepository posts,
            DevotionRepository devotions,
            BibleReadChapterRepository reads,
            BibleHighlightRepository highlights,
            TriviaSessionRepository triviaSessions) {
        this.posts = posts;
        this.devotions = devotions;
        this.reads = reads;
        this.highlights = highlights;
        this.triviaSessions = triviaSessions;
    }

    public record UserStats(
            long dayStreak,
            long versesRead,
            long devotionsRead,
            long postsCreated,
            long triviaAnswered,
            long chaptersRead,
            long highlights,
            long triviaPassedTotal,
            long triviaPassedToday,
            boolean triviaPassedTodayEasy,
            boolean triviaPassedTodayMedium,
            boolean triviaPassedTodayHard) {
    }

    @GetMapping("/stats")
    public UserStats stats(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());

        long chaptersRead = reads.countByUserId(uid);
        long highlightCount = highlights.findByUserIdOrderByCreatedAtDesc(uid).size();
        long versesRead = chaptersRead * 25;

        // Trivia aggregates
        List<TriviaSession> allSessions = triviaSessions.findByUserIdAndQuizDate(uid, LocalDate.now());
        List<TriviaSession> userHistory = triviaSessions.findAll().stream()
                .filter(s -> s.getUserId().equals(uid))
                .toList();

        long passedTotal = userHistory.stream().filter(TriviaSession::isPassed).count();
        long passedToday = allSessions.stream().filter(TriviaSession::isPassed).count();

        boolean easyPassed = allSessions.stream()
                .anyMatch(s -> "EASY".equalsIgnoreCase(s.getDifficulty()) && s.isPassed());
        boolean mediumPassed = allSessions.stream()
                .anyMatch(s -> "MEDIUM".equalsIgnoreCase(s.getDifficulty()) && s.isPassed());
        boolean hardPassed = allSessions.stream()
                .anyMatch(s -> "HARD".equalsIgnoreCase(s.getDifficulty()) && s.isPassed());

        return new UserStats(
                chaptersRead > 0 ? Math.min(chaptersRead, 30) : 0,
                versesRead,
                devotions.count(),
                posts.count(),
                passedTotal,
                chaptersRead,
                highlightCount,
                passedTotal,
                passedToday,
                easyPassed,
                mediumPassed,
                hardPassed);
    }
}