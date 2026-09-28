package org.walkwithgod.media;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class MediaService {

    private final MediaRepository repo;
    private final StorageService storage;
    private final MediaValidator validator;

    public MediaService(MediaRepository repo, StorageService storage, MediaValidator validator) {
        this.repo = repo;
        this.storage = storage;
        this.validator = validator;
    }

    @Transactional
    public Media upload(UUID uploaderId, MultipartFile file, Double videoDuration) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }

        MediaType type = validator.classify(file);

        if (type == MediaType.IMAGE) {
            validator.validateImage(file);
        } else {
            validator.validateVideo(file, videoDuration);
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read upload");
        }

        String checksum = sha256(bytes);
        boolean duplicate = repo.countByChecksumSha256(checksum) > 0;

        // Build a storage key: media/2026/09/<uuid>.<ext>
        String ext = extensionOf(file.getOriginalFilename());
        String uuid = UUID.randomUUID().toString();
        String key = "media/" + java.time.LocalDate.now().toString().replace("-", "/")
                + "/" + uuid + (ext.isEmpty() ? "" : "." + ext);

        try (InputStream in = new java.io.ByteArrayInputStream(bytes)) {
            storage.store(key, in, bytes.length);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Storage failed");
        }

        Media m = new Media();
        m.setUploaderId(uploaderId);
        m.setType(type);
        m.setStorageKey(key);
        m.setOriginalFilename(file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
        m.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        m.setSizeBytes(bytes.length);
        m.setChecksumSha256(checksum);
        m.setStatus(duplicate ? ModerationStatus.FLAGGED : ModerationStatus.APPROVED);
        if (duplicate)
            m.setFlaggedReason("Duplicate of an existing upload");
        if (type == MediaType.VIDEO && videoDuration != null) {
            m.setDurationSeconds(videoDuration);
        }

        return repo.save(m);
    }

    public Media get(UUID id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media not found"));
    }

    public InputStream stream(Media m) {
        if (m.getStatus() == ModerationStatus.DELETED || m.getStatus() == ModerationStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.GONE, "Media unavailable");
        }
        return storage.retrieve(m.getStorageKey());
    }

    @Transactional
    public void attachToPost(UUID mediaId, UUID postId) {
        Media m = get(mediaId);
        m.setPostId(postId);
        repo.save(m);
    }

    @Transactional
    public void markDeleted(Media m) {
        m.setStatus(ModerationStatus.DELETED);
        repo.save(m);
        storage.delete(m.getStorageKey());
    }

    private String sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extensionOf(String name) {
        if (name == null)
            return "";
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1)
            return "";
        return name.substring(dot + 1).toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}