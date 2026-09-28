package org.walkwithgod.media;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MediaRepository
        extends JpaRepository<Media, UUID>,
        JpaSpecificationExecutor<Media> {

    List<Media> findByPostId(UUID postId);

    Optional<Media> findFirstByPostIdOrderByCreatedAtAsc(UUID postId);

    long countByUploaderIdAndChecksumSha256(UUID uploaderId, String checksum);

    long countByChecksumSha256(String checksum);

    Page<Media> findByStatusOrderByCreatedAtAsc(ModerationStatus status, Pageable p);

    long countByStatus(ModerationStatus status);
}