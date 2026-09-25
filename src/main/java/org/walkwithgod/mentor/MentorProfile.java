package org.walkwithgod.mentor;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

@Entity
@Table(name = "mentor_profiles", indexes = @Index(name = "idx_mentor_verified", columnList = "verified"))
public class MentorProfile extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    private AppUser user;

    @Column(length = 160)
    private String denomination;

    @Column(length = 200)
    private String church;

    @Column(length = 1000)
    private String bio;

    private int yearsExperience;

    @Column(nullable = false)
    private boolean verified = false;

    @Column(length = 1000)
    private String specialties;

    private Double rating;

    // --- getters / setters ---

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser v) {
        user = v;
    }

    public String getDenomination() {
        return denomination;
    }

    public void setDenomination(String v) {
        denomination = v;
    }

    public String getChurch() {
        return church;
    }

    public void setChurch(String v) {
        church = v;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String v) {
        bio = v;
    }

    public int getYearsExperience() {
        return yearsExperience;
    }

    public void setYearsExperience(int v) {
        yearsExperience = v;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean v) {
        verified = v;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String v) {
        specialties = v;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double v) {
        rating = v;
    }
}