package com.backend.doyouhave.domain.notification;

import com.backend.doyouhave.domain.comment.Comment;
import com.backend.doyouhave.domain.post.Post;
import com.backend.doyouhave.domain.user.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Notification{
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String postTitle;

    @Column(nullable = false)
    private String commentContent;

    @Column(nullable = false)
    private LocalDateTime notifiedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "comment_id")
    private Comment comment;

    public void create(String title, String commentContent) {
        this.postTitle = title;
        this.commentContent = commentContent;
        this.notifiedDate = LocalDateTime.now();
    }

    public void setComment(Comment comment) {
        this.comment = comment;
    }

    public void setUser(User user) {
        user.getNotifications().add(this);
        this.user = user;
    }

}
