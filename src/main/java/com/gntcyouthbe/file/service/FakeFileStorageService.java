package com.gntcyouthbe.file.service;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"test", "local"})
public class FakeFileStorageService implements FileStorageService {

    @Override
    public String generatePresignedUrl(String storedFilename, String contentType) {
        return "https://fake-s3.example.com/uploads/" + storedFilename;
    }

    @Override
    public void deleteFiles(List<String> filePaths) {
        // no-op
    }
}
