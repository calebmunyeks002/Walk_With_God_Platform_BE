package org.walkwithgod.settings;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_settings", indexes = {
        @Index(name = "idx_settings_category", columnList = "category"),
        @Index(name = "idx_settings_key", columnList = "key", unique = true)
})
public class PlatformSetting extends BaseEntity {

    @Column(name = "key", nullable = false, length = 80, unique = true)
    private String key;

    @Column(columnDefinition = "text")
    private String value;

    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType = "STRING"; // STRING | BOOLEAN | INT

    @Column(nullable = false, length = 40)
    private String category = "GENERAL";

    @Column(length = 500)
    private String description;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private UUID updatedBy;

    // --- getters / setters ---

    public String getKey() {
        return key;
    }

    public void setKey(String v) {
        key = v;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        value = v;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String v) {
        valueType = v;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String v) {
        category = v;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String v) {
        description = v;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant v) {
        updatedAt = v;
    }

    public UUID getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(UUID v) {
        updatedBy = v;
    }
}