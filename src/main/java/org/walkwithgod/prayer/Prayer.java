package org.walkwithgod.prayer;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;

import java.time.LocalDate;

@Entity
@Table(name = "prayers", uniqueConstraints = @UniqueConstraint(name = "uk_prayer_slot_date", columnNames = { "slot",
        "prayer_date" }))
public class Prayer extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(length = 200)
    private String scripture;

    @Column(nullable = false, length = 20)
    private String slot; // MORNING | EVENING

    @Column(name = "prayer_date", nullable = false)
    private LocalDate prayerDate;

    @Column(name = "system_generated", nullable = false)
    private boolean systemGenerated = true;

    public String getTitle() {
        return title;
    }

    public void setTitle(String v) {
        title = v;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String v) {
        body = v;
    }

    public String getScripture() {
        return scripture;
    }

    public void setScripture(String v) {
        scripture = v;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String v) {
        slot = v;
    }

    public LocalDate getPrayerDate() {
        return prayerDate;
    }

    public void setPrayerDate(LocalDate v) {
        prayerDate = v;
    }

    public boolean isSystemGenerated() {
        return systemGenerated;
    }

    public void setSystemGenerated(boolean v) {
        systemGenerated = v;
    }
}