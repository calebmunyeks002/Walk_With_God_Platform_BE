package org.walkwithgod.community;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface PostRepository extends
        JpaRepository<Post, UUID>,
        JpaSpecificationExecutor<Post> {

    /** Public feed — all posts, newest first, with author pre-fetched. */
    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Public feed — only visible (non-hidden) posts, newest first. */
    @EntityGraph(attributePaths = "author")
    Page<Post> findByHiddenFalseOrderByCreatedAtDesc(Pageable pageable);
}