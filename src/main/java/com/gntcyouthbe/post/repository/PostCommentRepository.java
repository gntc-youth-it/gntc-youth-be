package com.gntcyouthbe.post.repository;

import com.gntcyouthbe.post.domain.PostComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    @Query("""
            SELECT pc.post.id, COUNT(pc) FROM PostComment pc
            WHERE pc.post.id IN :postIds
            GROUP BY pc.post.id
            """)
    List<Object[]> countByPostIds(@Param("postIds") List<Long> postIds);

    @Modifying
    @Query("DELETE FROM PostComment pc WHERE pc.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
