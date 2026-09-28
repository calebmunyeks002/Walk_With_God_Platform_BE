package org.walkwithgod.community;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface ReactionRepository extends JpaRepository<Reaction, UUID> {

    Optional<Reaction> findByUserIdAndPostId(UUID userId, UUID postId);

    long countByPostId(UUID postId);

    long countByPostIdAndType(UUID postId, String type);

    /** Returns a list of [type, count] tuples for one post. */
    @Query("SELECT r.type, COUNT(r) FROM Reaction r WHERE r.post.id = :postId GROUP BY r.type")
    List<Object[]> countsByTypeForPost(@Param("postId") UUID postId);
}