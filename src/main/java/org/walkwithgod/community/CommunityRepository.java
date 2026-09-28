package org.walkwithgod.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CommunityRepository
        extends JpaRepository<Community, UUID>,
        JpaSpecificationExecutor<Community> {

    Optional<Community> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Community> findByHiddenFalseOrderByMemberCountDesc(Pageable p);

    @Query("""
                SELECT c FROM Community c
                WHERE c.hidden = false
                  AND c.id IN (
                      SELECT m.communityId FROM CommunityMember m WHERE m.userId = :uid
                  )
                ORDER BY c.name ASC
            """)
    Page<Community> myCommunities(@Param("uid") UUID uid, Pageable p);
}