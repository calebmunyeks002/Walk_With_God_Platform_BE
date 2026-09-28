package org.walkwithgod.media;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Set;

@Component
public class MediaValidator {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif");

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4", "video/webm");

    private final MediaProperties props;

    public MediaValidator(MediaProperties props) {
        this.props = props;
    }

    public MediaType classify(MultipartFile file) {
        String ct = file.getContentType();
        if (ct == null) {
            throw bad("Missing content type");
        }
        if (IMAGE_TYPES.contains(ct))
            return MediaType.IMAGE;
        if (VIDEO_TYPES.contains(ct))
            return MediaType.VIDEO;
        throw bad("Unsupported file type: " + ct);
    }

    public void validateImage(MultipartFile file) {
        if (file.getSize() > props.getMaxImageBytes()) {
            throw bad("Image exceeds " + (props.getMaxImageBytes() / 1024 / 1024) + " MB");
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(file.getBytes()));
            if (img == null)
                throw bad("Unreadable image");
            int w = img.getWidth(), h = img.getHeight();
            if (w < props.getMinImageDimension() || h < props.getMinImageDimension()) {
                throw bad("Image too small (min " + props.getMinImageDimension() + "px)");
            }
            if (w > props.getMaxImageDimension() || h > props.getMaxImageDimension()) {
                throw bad("Image too large (max " + props.getMaxImageDimension() + "px)");
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw bad("Invalid image file");
        }
    }

    public void validateVideo(MultipartFile file, Double durationSeconds) {
        if (file.getSize() > props.getMaxVideoBytes()) {
            throw bad("Video exceeds " + (props.getMaxVideoBytes() / 1024 / 1024) + " MB");
        }
        if (durationSeconds != null && durationSeconds > props.getMaxVideoSeconds()) {
            throw bad("Video exceeds " + (int) props.getMaxVideoSeconds() + " seconds");
        }
    }

    private ResponseStatusException bad(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}