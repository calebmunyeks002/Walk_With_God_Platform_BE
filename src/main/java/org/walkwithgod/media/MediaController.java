package org.walkwithgod.media;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaService media;

    public MediaController(MediaService m) {
        media = m;
    }

    public record MediaView(
            String id, String type, String status,
            String contentType, long sizeBytes,
            Integer width, Integer height, Double durationSeconds,
            String flaggedReason, String createdAt) {
    }

    @PostMapping("/upload")
    public MediaView upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "duration", required = false) Double duration,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Media m = media.upload(uid, file, duration);
        return toView(m);
    }

    @GetMapping("/{id}/raw")
    public ResponseEntity<InputStreamResource> raw(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        Media m = media.get(id);
        InputStream in = media.stream(m);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, m.getContentType())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=300")
                .body(new InputStreamResource(in));
    }

    @GetMapping("/{id}")
    public MediaView meta(@PathVariable UUID id) {
        return toView(media.get(id));
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {

        UUID uid = UUID.fromString(jwt.getSubject());
        Media m = media.get(id);
        if (!m.getUploaderId().equals(uid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        media.markDeleted(m);
    }

    private MediaView toView(Media m) {
        return new MediaView(
                m.getId().toString(),
                m.getType().name(),
                m.getStatus().name(),
                m.getContentType(),
                m.getSizeBytes(),
                m.getWidth(),
                m.getHeight(),
                m.getDurationSeconds(),
                m.getFlaggedReason(),
                m.getCreatedAt().toString());
    }
}