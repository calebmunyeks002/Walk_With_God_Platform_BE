package org.walkwithgod.admin;

import java.util.List;
import org.walkwithgod.moderation.ReportStatus;
import org.walkwithgod.moderation.ReportTargetType;
import org.walkwithgod.user.Role;

import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    public record SystemStats(
            long totalUsers,
            long totalMembers,
            long totalMentors,
            long totalAdmins,
            long suspendedUsers,
            long pendingMentorApplications,
            long totalPosts,
            long totalComments,
            long totalDevotions,
            long totalConversations,
            long activeToday) {
    }

    public record AdminUserView(
            String id,
            String name,
            String email,
            Role role,
            boolean enabled,
            boolean suspended,
            String suspensionReason,
            String suspendedAt,
            String avatarUrl,
            String bio,
            String createdAt) {
    }

    public record PageResponse<T>(
            List<T> content,
            long totalElements,
            int totalPages,
            int number,
            int size,
            boolean first,
            boolean last) {
    }

    public record SuspendRequest(String reason) {
    }

    public record RoleChangeRequest(Role role, String reason) {
    }

    /* ---------- NEW: Moderation DTOs ---------- */

    public record AdminPostView(
            String id,
            String authorId,
            String authorName,
            String authorEmail,
            String content,
            String scriptureReference,
            String imageUrl,
            boolean hidden,
            String hiddenReason,
            String hiddenAt,
            long reportCount,
            String createdAt) {
    }

    public record DeletePostRequest(String reason) {
    }

    public record ReportView(
            String id,
            String reporterId,
            String reporterName,
            ReportTargetType targetType,
            String targetId,
            String reason,
            String details,
            ReportStatus status,
            String resolvedBy,
            String resolvedAt,
            String resolutionNote,
            String createdAt) {
    }

    public record ResolveReportRequest(
            ReportStatus status,
            String note) {
    }

    /* ---------- Devotion DTOs ---------- */

    public record AdminDevotionView(
            String id,
            String title,
            String scripture,
            String body,
            String authorId,
            String authorName,
            boolean published,
            boolean featured,
            boolean hidden,
            String hiddenReason,
            String publishedAt,
            String updatedAt,
            String createdAt) {
    }

    public record CreateDevotionRequest(
            String title,
            String scripture,
            String body,
            Boolean published,
            Boolean featured) {
    }

    public record UpdateDevotionRequest(
            String title,
            String scripture,
            String body,
            Boolean published,
            Boolean featured) {
    }

    public record DeleteDevotionRequest(String reason) {
    }

    /* ---------- Trivia DTOs ---------- */

    public record AdminTriviaView(
            String id,
            String question,
            List<String> options,
            int answerIndex,
            String explanation,
            String difficulty,
            boolean hidden,
            String hiddenReason,
            String hiddenAt,
            String updatedAt,
            String createdAt) {
    }

    public record CreateTriviaRequest(
            String question,
            List<String> options,
            Integer answerIndex,
            String explanation,
            String difficulty) {
    }

    public record UpdateTriviaRequest(
            String question,
            List<String> options,
            Integer answerIndex,
            String explanation,
            String difficulty) {
    }
}