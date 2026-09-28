package org.walkwithgod.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityJoinRequestRepository extends JpaRepository<CommunityJoinRequest, UUID> {

    Optional<CommunityJoinRequest> findByCommunityIdAndUserId(UUID communityId, UUID userId);

    List<CommunityJoinRequest> findByCommunityIdAndStatusOrderByCreatedAtAsc(
            UUID communityId, JoinRequestStatus status);

    long countByCommunityIdAndStatus(UUID communityId, JoinRequestStatus status);
}