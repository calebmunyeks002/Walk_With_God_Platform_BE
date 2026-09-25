package org.walkwithgod.mentor;

import org.walkwithgod.auth.AuthDtos;

import java.util.List;

public final class MentorDtos {

    private MentorDtos() {
    }

    /* ---------- Dashboard views ---------- */

    public record MentorRequestView(
            String id,
            AuthDtos.UserView member,
            String message,
            String status,
            String respondedAt,
            String responseNote,
            String createdAt) {
    }

    public record MenteeView(
            String requestId,
            AuthDtos.UserView member,
            String since,
            String lastMessage,
            String status) {
    }

    public record MentorStats(
            long activeMentees,
            long pendingRequests,
            long totalAccepted,
            long totalCompleted,
            double averageRating,
            int yearsExperience,
            boolean verified) {
    }

    public record MentorProfileView(
            String id,
            AuthDtos.UserView user,
            String denomination,
            String church,
            List<String> specialties,
            String bio,
            int yearsExperience,
            boolean verified,
            Double rating) {
    }

    /* ---------- Request actions ---------- */

    public record DeclineRequest(String reason) {
    }

    public record AcceptRequest(String note) {
    }
}