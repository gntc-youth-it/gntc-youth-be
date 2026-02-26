package com.gntcyouthbe.post.repository;

import com.gntcyouthbe.post.domain.Post;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.author
            WHERE p.status = :status
            AND p.id < :cursor
            ORDER BY p.id DESC
            LIMIT :size
            """)
    List<Post> findFeed(@Param("status") PostStatus status, @Param("cursor") Long cursor,
            @Param("size") int size);

    @Query("""
            SELECT p FROM Post p
            JOIN FETCH p.author
            WHERE p.status = :status
            AND p.subCategory = :subCategory
            AND p.id < :cursor
            ORDER BY p.id DESC
            LIMIT :size
            """)
    List<Post> findFeedBySubCategory(@Param("status") PostStatus status,
            @Param("subCategory") PostSubCategory subCategory,
            @Param("cursor") Long cursor, @Param("size") int size);
}
