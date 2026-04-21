package com.sergeev.taskmanager.media.api;

import com.sergeev.taskmanager.media.api.dto.StorageSaveResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface FileStorage {
    CompletableFuture<StorageSaveResult> save(MultipartFile file) throws Exception;
    CompletableFuture<InputStream> load(String filePath) throws Exception;
    CompletableFuture<Boolean> delete(String filePath) throws Exception;
}

