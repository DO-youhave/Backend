package com.backend.doyouhave.repository.post;

import com.backend.doyouhave.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}