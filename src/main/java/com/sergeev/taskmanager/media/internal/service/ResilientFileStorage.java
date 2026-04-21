package com.sergeev.taskmanager.media.internal.service;

import com.sergeev.taskmanager.media.api.FileStorage;
import com.sergeev.taskmanager.media.api.dto.StorageSaveResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

@Component("resilientFileStorage")
public class ResilientFileStorage implements FileStorage {

    private final FileSystemStorage delegate;

    public ResilientFileStorage(FileSystemStorage delegate) {
        this.delegate = delegate;
    }

    @Override
    @TimeLimiter(name = "fileStorageTimeLimiter")
    @Retry(name = "fileStorageRetry")
    @CircuitBreaker(name = "fileStorageCircuitBreaker")
    public CompletableFuture<StorageSaveResult> save(MultipartFile file) {
        return delegate.save(file);
    }

    @Override
    @TimeLimiter(name = "fileStorageTimeLimiter")
    @Retry(name = "fileStorageRetry")
    @CircuitBreaker(name = "fileStorageCircuitBreaker")
    public CompletableFuture<InputStream> load(String filePath) {
        return delegate.load(filePath);
    }

    @Override
    @TimeLimiter(name = "fileStorageTimeLimiter")
    @Retry(name = "fileStorageRetry")
    @CircuitBreaker(name = "fileStorageCircuitBreaker")
    public CompletableFuture<Boolean> delete(String filePath) {
        return delegate.delete(filePath);
    }
}
