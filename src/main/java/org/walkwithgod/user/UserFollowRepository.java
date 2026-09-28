package org.walkwithgod.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {

    Optional<UserFollow> findByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    boolean existsByFollowerIdAndFollowedId(UUID followerId, UUID followedId);

    List<UserFollow> findByFollowerId(UUID followerId);

    List<UserFollow> findByFollowedId(UUID followedId);
}