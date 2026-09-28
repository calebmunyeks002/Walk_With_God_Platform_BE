package org.walkwithgod.community;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostIdAndDeletedFalseOrderByCreatedAtAsc(UUID postId);

    long countByPostIdAndDeletedFalse(UUID postId);

    Optional<Comment> findByIdAndDeletedFalse(UUID id);
}