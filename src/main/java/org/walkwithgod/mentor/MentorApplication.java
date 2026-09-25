package org.walkwithgod.mentor;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

@Entity
@Table(
    name = "mentor_applications",
    indexes = @Index(name = "idx_ma_status", columnList = "status")
)
public class MentorApplication extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private AppUser applicant;
    @Column(nullable = false, length = 2000)
    private String qualifications;

    @Column(length = 300)
    private String organization;

    private int yearsExperience;

    @Column(length = 1000)
    private String documentUrl;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    // --- getters / setters ---

    public AppUser getApplicant() { return applicant; }
    public void setApplicant(AppUser v) { applicant = v; }

    public String getQualifications() { return qualifications; }
    public void setQualifications(String v) { qualifications = v; }

    public String getOrganization() { return organization; }
    public void setOrganization(String v) { organization = v; }

    public int getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(int v) { yearsExperience = v; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String v) { documentUrl = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
}