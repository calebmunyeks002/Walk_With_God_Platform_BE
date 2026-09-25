package org.walkwithgod.bible;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/api/bible")
public class BibleController {

    private final BibleReadChapterRepository reads;
    private final BibleHighlightRepository highlights;

    public BibleController(
            BibleReadChapterRepository reads,
            BibleHighlightRepository highlights) {
        this.reads = reads;
        this.highlights = highlights;
    }

    /*
     * =========================================================
     * Read chapters
     * =========================================================
     */

    public record MarkReadRequest(
            @NotBlank String version,
            @NotBlank String book,
            @NotNull Integer chapter) {
    }

    public record ReadChapterView(String book, int chapter, String readAt) {
    }

    @PostMapping("/read")
    @Transactional
    public ReadChapterView markRead(
            @Valid @RequestBody MarkReadRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());

        var existing = reads.findByUserIdAndBookAndChapter(uid, r.book(), r.chapter());
        if (existing.isPresent()) {
            var rc = existing.get();
            return new ReadChapterView(rc.getBook(), rc.getChapter(), rc.getReadAt().toString());
        }

        BibleReadChapter rc = new BibleReadChapter();
        rc.setUserId(uid);
        rc.setVersion(r.version());
        rc.setBook(r.book());
        rc.setChapter(r.chapter());
        reads.save(rc);

        return new ReadChapterView(rc.getBook(), rc.getChapter(), rc.getReadAt().toString());
    }

    @DeleteMapping("/read")
    @Transactional
    public void unmarkRead(
            @RequestParam String book,
            @RequestParam int chapter,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        reads.findByUserIdAndBookAndChapter(uid, book, chapter)
                .ifPresent(reads::delete);
    }

    @GetMapping("/read")
    public List<ReadChapterView> listRead(@AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return reads.findByUserIdOrderByReadAtDesc(uid).stream()
                .map(rc -> new ReadChapterView(
                        rc.getBook(), rc.getChapter(), rc.getReadAt().toString()))
                .toList();
    }

    @GetMapping("/read/status")
    public Map<String, Boolean> readStatus(
            @RequestParam String book,
            @RequestParam int chapter,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return Map.of("read", reads.existsByUserIdAndBookAndChapter(uid, book, chapter));
    }

    /*
     * =========================================================
     * Highlights
     * =========================================================
     */

    public record HighlightRequest(
            @NotBlank String version,
            @NotBlank String book,
            @NotNull Integer chapter,
            @NotNull Integer verse,
            String color,
            String note) {
    }

    public record HighlightView(
            String id, String version, String book, int chapter, int verse,
            String color, String note, String createdAt) {
    }

    @PostMapping("/highlights")
    @Transactional
    public HighlightView upsertHighlight(
            @Valid @RequestBody HighlightRequest r,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());

        var existing = highlights.findByUserIdAndVersionAndBookAndChapterAndVerse(
                uid, r.version(), r.book(), r.chapter(), r.verse());

        BibleHighlight h = existing.orElseGet(BibleHighlight::new);
        h.setUserId(uid);
        h.setVersion(r.version());
        h.setBook(r.book());
        h.setChapter(r.chapter());
        h.setVerse(r.verse());
        h.setColor(r.color() == null || r.color().isBlank() ? "yellow" : r.color());
        h.setNote(r.note());
        highlights.save(h);

        return toView(h);
    }

    @DeleteMapping("/highlights/{id}")
    @Transactional
    public void deleteHighlight(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        BibleHighlight h = highlights.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!h.getUserId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        highlights.delete(h);
    }

    @GetMapping("/highlights")
    public List<HighlightView> chapterHighlights(
            @RequestParam String version,
            @RequestParam String book,
            @RequestParam int chapter,
            @AuthenticationPrincipal Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getSubject());
        return highlights
                .findByUserIdAndVersionAndBookAndChapterOrderByVerseAsc(
                        uid, version, book, chapter)
                .stream()
                .map(this::toView)
                .toList();
    }

    private HighlightView toView(BibleHighlight h) {
        return new HighlightView(
                h.getId().toString(),
                h.getVersion(),
                h.getBook(),
                h.getChapter(),
                h.getVerse(),
                h.getColor(),
                h.getNote(),
                h.getCreatedAt().toString());
    }
}