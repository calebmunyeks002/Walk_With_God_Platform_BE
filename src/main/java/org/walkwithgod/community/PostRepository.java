package org.walkwithgod.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface PostRepository
        extends JpaRepository<Post, UUID>,
        JpaSpecificationExecutor<Post> {

    Page<Post> findByHiddenFalseOrderByCreatedAtDesc(Pageable p);

    @Query("""
                SELECT p FROM Post p
                WHERE p.hidden = false
                  AND p.author.id IN (
                      SELECT f.followedId FROM UserFollow f WHERE f.followerId = :uid
                  )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> followingFeed(@Param("uid") UUID uid, Pageable p);

    @Query("""
                SELECT p FROM Post p
                WHERE p.hidden = false
                  AND p.author.role = 'MENTOR'
                ORDER BY p.createdAt DESC
            """)
    Page<Post> mentorFeed(Pageable p);

    Page<Post> findByTypeAndHiddenFalseOrderByCreatedAtDesc(PostType type, Pageable p);

    Page<Post> findByCommunityIdAndHiddenFalseOrderByCreatedAtDesc(UUID communityId, Pageable p);

    @Query("""
                SELECT p FROM Post p
                WHERE p.hidden = false
                  AND p.communityId IS NULL
                ORDER BY p.createdAt DESC
            """)
    Page<Post> globalFeed(Pageable p);

    @Query("""
                SELECT p FROM Post p
                WHERE p.hidden = false
                  AND p.communityId IS NULL
                  AND p.author.id IN (
                      SELECT f.followedId FROM UserFollow f WHERE f.followerId = :uid
                  )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> followingFeedGlobal(@Param("uid") UUID uid, Pageable p);

    @Query("""
                SELECT p FROM Post p
                WHERE p.hidden = false
                  AND p.communityId IN (
                      SELECT m.communityId FROM CommunityMember m WHERE m.userId = :uid
                  )
                ORDER BY p.createdAt DESC
            """)
    Page<Post> myCommunitiesFeed(@Param("uid") UUID uid, Pageable p);
}