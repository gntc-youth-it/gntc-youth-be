package com.gntcyouthbe.post.repository;

import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    @Query("""
            SELECT pi FROM PostImage pi
            JOIN FETCH pi.uploadedFile
            WHERE pi.post.status = 'APPROVED'
            AND pi.id < :cursor
            ORDER BY pi.id DESC
            LIMIT :size
            """)
    List<PostImage> findGalleryImages(@Param("cursor") Long cursor, @Param("size") int size);

    @Query("""
            SELECT pi FROM PostImage pi
            JOIN FETCH pi.uploadedFile
            WHERE pi.post.status = 'APPROVED'
            AND pi.post.subCategory = :subCategory
            AND pi.id < :cursor
            ORDER BY pi.id DESC
            LIMIT :size
            """)
    List<PostImage> findGalleryImagesBySubCategory(
            @Param("subCategory") PostSubCategory subCategory,
            @Param("cursor") Long cursor,
            @Param("size") int size);
}
