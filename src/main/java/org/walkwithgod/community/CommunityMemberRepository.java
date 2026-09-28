package org.walkwithgod.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityMemberRepository extends JpaRepository<CommunityMember, UUID> {

    Optional<CommunityMember> findByCommunityIdAndUserId(UUID communityId, UUID userId);

    boolean existsByCommunityIdAndUserId(UUID communityId, UUID userId);

    List<CommunityMember> findByCommunityIdOrderByRoleAscJoinedAtAsc(UUID communityId);

    List<CommunityMember> findByUserId(UUID userId);

    long countByCommunityId(UUID communityId);

    void deleteByCommunityIdAndUserId(UUID communityId, UUID userId);
}