package org.walkwithgod.media;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
public class LocalDiskStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalDiskStorageService.class);

    private final MediaProperties props;
    private Path root;

    public LocalDiskStorageService(MediaProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() throws IOException {
        this.root = Paths.get(props.getStorageRoot()).toAbsolutePath().normalize();
        Files.createDirectories(root);
        log.info("Media storage root: {}", root);
    }

    @Override
    public String store(String relativeKey, InputStream data, long contentLength) {
        try {
            Path target = resolve(relativeKey);
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
            return relativeKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store media: " + relativeKey, e);
        }
    }

    @Override
    public InputStream retrieve(String relativeKey) {
        try {
            return Files.newInputStream(resolve(relativeKey));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read media: " + relativeKey, e);
        }
    }

    @Override
    public Path physicalPath(String relativeKey) {
        return resolve(relativeKey);
    }

    @Override
    public void delete(String relativeKey) {
        try {
            Files.deleteIfExists(resolve(relativeKey));
        } catch (IOException e) {
            log.warn("Failed to delete {}: {}", relativeKey, e.getMessage());
        }
    }

    @Override
    public boolean exists(String relativeKey) {
        return Files.exists(resolve(relativeKey));
    }

    /** Prevent path traversal attacks (../). */
    private Path resolve(String relativeKey) {
        Path target = root.resolve(relativeKey).normalize();
        if (!target.startsWith(root)) {
            throw new SecurityException("Path traversal detected: " + relativeKey);
        }
        return target;
    }
}