package org.walkwithgod.community;

import jakarta.persistence.*;
import org.walkwithgod.common.BaseEntity;
import org.walkwithgod.user.AppUser;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comments_post", columnList = "post_id")
})
public class Comment extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "author_id")
    private AppUser author;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private boolean deleted = false;

    public Post getPost() {
        return post;
    }

    public void setPost(Post v) {
        post = v;
    }

    public AppUser getAuthor() {
        return author;
    }

    public void setAuthor(AppUser v) {
        author = v;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String v) {
        content = v;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean v) {
        deleted = v;
    }
}