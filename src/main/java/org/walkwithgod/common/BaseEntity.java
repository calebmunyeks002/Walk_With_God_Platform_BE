package org.walkwithgod.common;
import jakarta.persistence.*;import java.time.Instant;import java.util.UUID;
@MappedSuperclass public abstract class BaseEntity { @Id @GeneratedValue(strategy=GenerationType.UUID) protected UUID id; @Column(nullable=false,updatable=false) protected Instant createdAt=Instant.now(); public UUID getId(){return id;} public Instant getCreatedAt(){return createdAt;} }
