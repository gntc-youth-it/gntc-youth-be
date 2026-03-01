package com.gntcyouthbe.post.repository;

import com.gntcyouthbe.church.domain.ChurchId;
import com.gntcyouthbe.post.domain.PostImage;
import com.gntcyouthbe.post.domain.PostStatus;
import com.gntcyouthbe.post.domain.PostSubCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    @Query("""
            SELECT pi FROM PostImage pi
            JOIN FETCH pi.uploadedFile
            WHERE pi.post.status = :status
            AND pi.id < :cursor
            ORDER BY pi.id DESC
            LIMIT :size
            """)
    List<PostImage> findGalleryImages(@Param("status") PostStatus status,
            @Param("cursor") Long cursor, @Param("size") int size);

    @Query("""
            SELECT pi FROM PostImage pi
            JOIN FETCH pi.uploadedFile
            WHERE pi.post.status = :status
            AND pi.post.subCategory = :subCategory
            AND pi.id < :cursor
            ORDER BY pi.id DESC
            LIMIT :size
            """)
    List<PostImage> findGalleryImagesBySubCategory(@Param("status") PostStatus status,
            @Param("subCategory") PostSubCategory subCategory,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    @Query("""
            SELECT pi FROM PostImage pi
            JOIN FETCH pi.uploadedFile
            WHERE pi.post.status = :status
            AND :churchId MEMBER OF pi.post.churches
            AND pi.id < :cursor
            ORDER BY pi.id DESC
            LIMIT :size
            """)
    List<PostImage> findGalleryImagesByChurch(@Param("status") PostStatus status,
            @Param("churchId") ChurchId churchId,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    @Query("""
            SELECT pi FROM PostImage pi
            JOIN FETCH pi.uploadedFile
            WHERE pi.post.status = :status
            AND pi.post.subCategory = :subCategory
            AND :churchId MEMBER OF pi.post.churches
            AND pi.id < :cursor
            ORDER BY pi.id DESC
            LIMIT :size
            """)
    List<PostImage> findGalleryImagesBySubCategoryAndChurch(@Param("status") PostStatus status,
            @Param("subCategory") PostSubCategory subCategory,
            @Param("churchId") ChurchId churchId,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    @Query(value = """
            SELECT uf.file_path FROM post_image pi
            JOIN post p ON pi.post_id = p.id
            JOIN post_church pc ON pc.post_id = p.id
            JOIN uploaded_file uf ON pi.uploaded_file_id = uf.id
            WHERE pc.church_id = :churchId
            AND p.status = 'APPROVED'
            ORDER BY RANDOM()
            LIMIT 7
            """, nativeQuery = true)
    List<String> findRandomImagePathsByChurch(@Param("churchId") String churchId);
}
