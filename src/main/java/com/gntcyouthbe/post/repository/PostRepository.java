package com.gntcyouthbe.post.repository;

import com.gntcyouthbe.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
