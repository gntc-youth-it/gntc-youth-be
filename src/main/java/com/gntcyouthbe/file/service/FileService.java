package com.gntcyouthbe.file.service;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.model.request.PresignedUrlRequest;
import com.gntcyouthbe.file.model.response.PresignedUrlResponse;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final UploadedFileRepository uploadedFileRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
        String storedFilename = UUID.randomUUID() + extractExtension(request.getFilename());
        String filePath = "uploads/" + storedFilename;

        UploadedFile file = new UploadedFile(
                request.getFilename(),
                storedFilename,
                filePath,
                request.getContentType(),
                0L
        );
        uploadedFileRepository.save(file);

        String presignedUrl = fileStorageService.generatePresignedUrl(storedFilename, request.getContentType());
        return new PresignedUrlResponse(file.getId(), presignedUrl);
    }

    private String extractExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return filename.substring(dotIndex);
    }
}
