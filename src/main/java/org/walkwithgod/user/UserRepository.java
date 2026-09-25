package org.walkwithgod.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends
        JpaRepository<AppUser, UUID>,
        JpaSpecificationExecutor<AppUser> {

    Optional<AppUser> findByEmailIgnoreCase(String email);

    long countByRole(Role role);

    long countBySuspendedTrue();

    long countByCreatedAtAfter(Instant threshold);
}