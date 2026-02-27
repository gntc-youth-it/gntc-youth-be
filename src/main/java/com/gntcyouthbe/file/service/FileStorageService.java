package com.gntcyouthbe.file.service;

import java.util.List;

public interface FileStorageService {

    String generatePresignedUrl(String storedFilename, String contentType);

    void deleteFiles(List<String> filePaths);
}
