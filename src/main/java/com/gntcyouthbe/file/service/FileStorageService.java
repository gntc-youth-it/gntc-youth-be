package com.gntcyouthbe.file.service;

public interface FileStorageService {

    String generatePresignedUrl(String storedFilename, String contentType);
}
