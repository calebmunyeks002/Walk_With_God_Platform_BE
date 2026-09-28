package org.walkwithgod.media;

import java.io.InputStream;
import java.nio.file.Path;

/**
 * Storage abstraction. Today: local disk. Tomorrow: S3/R2.
 * Every consumer should go through this interface.
 */
public interface StorageService {

    /** Persist the stream at the given relative key. Returns the key. */
    String store(String relativeKey, InputStream data, long contentLength);

    /** Open a stream for reading. Caller must close it. */
    InputStream retrieve(String relativeKey);

    /** Return a filesystem path if this backend supports it, else null. */
    Path physicalPath(String relativeKey);

    /** Delete the object. Idempotent — silently ignores missing files. */
    void delete(String relativeKey);

    /** True if the object exists. */
    boolean exists(String relativeKey);
}