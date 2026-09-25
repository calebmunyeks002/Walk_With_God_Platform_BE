package org.walkwithgod.moderation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ReportRepository extends
        JpaRepository<Report, UUID>,
        JpaSpecificationExecutor<Report> {

    long countByStatus(ReportStatus status);

    List<Report> findByTargetTypeAndTargetId(ReportTargetType type, UUID targetId);

    long countByTargetTypeAndTargetIdAndStatus(
            ReportTargetType type, UUID targetId, ReportStatus status);
}