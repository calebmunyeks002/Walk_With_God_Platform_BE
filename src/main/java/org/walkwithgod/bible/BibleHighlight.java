package org.walkwithgod.bible;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.util.UUID;

@Entity
@Table(name = "bible_highlights", uniqueConstraints = {
        @UniqueConstraint(name = "uk_highlight_user_verse", columnNames = { "user_id", "version", "book", "chapter",
                "verse" })
})
public class BibleHighlight extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(nullable = false, length = 40)
    private String book;

    @Column(nullable = false)
    private int chapter;

    @Column(nullable = false)
    private int verse;

    @Column(nullable = false, length = 20)
    private String color = "yellow";

    @Column(length = 1000)
    private String note;

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

    public int getVerse() {
        return verse;
    }

    public void setVerse(int v) {
        verse = v;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String v) {
        color = v;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String v) {
        note = v;
    }
}