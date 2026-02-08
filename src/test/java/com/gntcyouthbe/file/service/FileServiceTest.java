package com.gntcyouthbe.file.service;

import com.gntcyouthbe.file.domain.UploadedFile;
import com.gntcyouthbe.file.model.request.PresignedUrlRequest;
import com.gntcyouthbe.file.model.response.PresignedUrlResponse;
import com.gntcyouthbe.file.repository.UploadedFileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private UploadedFileRepository uploadedFileRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private FileService fileService;

    @Test
    @DisplayName("Presigned URL 발급 성공")
    void generatePresignedUrl_success() {
        // given
        PresignedUrlRequest request = new PresignedUrlRequest("photo.jpg", "image/jpeg", 1024L);
        given(uploadedFileRepository.save(any(UploadedFile.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(fileStorageService.generatePresignedUrl(anyString(), anyString()))
                .willReturn("https://s3.example.com/presigned-url");

        // when
        PresignedUrlResponse response = fileService.generatePresignedUrl(request);

        // then
        assertThat(response.getPresignedUrl()).isEqualTo("https://s3.example.com/presigned-url");
        verify(uploadedFileRepository).save(any(UploadedFile.class));
    }

    @Test
    @DisplayName("확장자 없는 파일명도 처리된다")
    void generatePresignedUrl_noExtension() {
        // given
        PresignedUrlRequest request = new PresignedUrlRequest("README", "text/plain", 512L);
        given(uploadedFileRepository.save(any(UploadedFile.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(fileStorageService.generatePresignedUrl(anyString(), anyString()))
                .willReturn("https://s3.example.com/presigned-url");

        // when
        PresignedUrlResponse response = fileService.generatePresignedUrl(request);

        // then
        assertThat(response.getPresignedUrl()).isNotBlank();
        verify(uploadedFileRepository).save(any(UploadedFile.class));
    }
}
