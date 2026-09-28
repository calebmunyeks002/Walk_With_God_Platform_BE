package org.walkwithgod.devotion;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/devotions")
public class DevotionController {

    private final DevotionRepository repo;

    public DevotionController(DevotionRepository r) {
        repo = r;
    }

    public record View(
                    String id, String title, String scripture, String body,
                    String author, boolean featured, boolean systemGenerated,
                    String publishedAt) {
    }

    @GetMapping
    public List<View> list() {
        return repo.findByPublishedTrueAndHiddenFalseOrderByPublishedAtDesc()
                .stream()
                .map(d -> new View(
                        d.getId().toString(),
                        d.getTitle(),
                        d.getScripture(),
                        d.getBody(),
                        d.getAuthor().getName(),
                        d.isFeatured(),
                        d.isSystemGenerated(),
                        d.getPublishedAt().toString()))
                .toList();
    }

    @GetMapping("/{id}")
    public View get(@PathVariable UUID id) {
        Devotion d = repo.findByIdAndHiddenFalse(id).orElseThrow();
        return new View(
                d.getId().toString(),
                d.getTitle(),
                d.getScripture(),
                d.getBody(),
                d.getAuthor().getName(),
                d.isFeatured(),
                d.isSystemGenerated(),
                d.getPublishedAt().toString());
    }

    @GetMapping("/featured")
    public View featured() {
        return repo.findFirstByFeaturedTrueAndHiddenFalseAndPublishedTrue()
                .map(d -> new View(
                        d.getId().toString(),
                        d.getTitle(),
                        d.getScripture(),
                        d.getBody(),
                        d.getAuthor().getName(),
                        true,
                        d.isSystemGenerated(),
                        d.getPublishedAt().toString()))
                .orElse(null);
    }
}