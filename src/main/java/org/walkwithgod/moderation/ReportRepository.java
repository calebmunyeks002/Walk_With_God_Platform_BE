package org.walkwithgod.moderation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ReportRepository
                extends JpaRepository<Report, UUID>,
                JpaSpecificationExecutor<Report> { // ← ADD THIS

        long countByTargetTypeAndTargetIdAndStatus(
                        ReportTargetType type, UUID targetId, ReportStatus status);
}