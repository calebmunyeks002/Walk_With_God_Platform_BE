package org.walkwithgod.media;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.media")
public class MediaProperties {

    /** Root folder where uploads are stored, e.g. ./uploads */
    private String storageRoot = "./uploads";

    /** Max image size in bytes (default 10 MB). */
    private long maxImageBytes = 10L * 1024 * 1024;

    /** Max video size in bytes (default 25 MB). */
    private long maxVideoBytes = 25L * 1024 * 1024;

    /** Max video duration in seconds (default 60). */
    private double maxVideoSeconds = 60.0;

    /** Min image dimension in pixels (both axes). */
    private int minImageDimension = 100;

    /** Max image dimension in pixels (both axes). */
    private int maxImageDimension = 8000;

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String v) {
        storageRoot = v;
    }

    public long getMaxImageBytes() {
        return maxImageBytes;
    }

    public void setMaxImageBytes(long v) {
        maxImageBytes = v;
    }

    public long getMaxVideoBytes() {
        return maxVideoBytes;
    }

    public void setMaxVideoBytes(long v) {
        maxVideoBytes = v;
    }

    public double getMaxVideoSeconds() {
        return maxVideoSeconds;
    }

    public void setMaxVideoSeconds(double v) {
        maxVideoSeconds = v;
    }

    public int getMinImageDimension() {
        return minImageDimension;
    }

    public void setMinImageDimension(int v) {
        minImageDimension = v;
    }

    public int getMaxImageDimension() {
        return maxImageDimension;
    }

    public void setMaxImageDimension(int v) {
        maxImageDimension = v;
    }
}