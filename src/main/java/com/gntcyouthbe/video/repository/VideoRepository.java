package com.gntcyouthbe.video.repository;

import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.video.domain.Video;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v WHERE v.subCategory = :subCategory ORDER BY v.id DESC")
    List<Video> findBySubCategory(PostSubCategory subCategory);
}
