package com.gntcyouthbe.video.repository;

import com.gntcyouthbe.post.domain.PostSubCategory;
import com.gntcyouthbe.video.domain.Video;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findBySubCategoryOrderByIdDesc(PostSubCategory subCategory);

    List<Video> findAllByOrderByIdDesc();
}
