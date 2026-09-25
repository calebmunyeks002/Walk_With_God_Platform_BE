package org.walkwithgod.bible;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bible_read_chapters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_read_chapter_user", columnNames = { "user_id", "book", "chapter" })
})
public class BibleReadChapter extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 40)
    private String book;

    @Column(nullable = false)
    private int chapter;

    @Column(name = "read_at", nullable = false)
    private Instant readAt = Instant.now();

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID v) {
        userId = v;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String v) {
        version = v;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String v) {
        book = v;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int v) {
        chapter = v;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant v) {
        readAt = v;
    }
}