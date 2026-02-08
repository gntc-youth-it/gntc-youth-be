package com.gntcyouthbe.file.repository;

import com.gntcyouthbe.file.domain.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
}
