package com.sergeev.taskmanager.media.internal.service;

import com.sergeev.taskmanager.media.api.FileStorage;
import com.sergeev.taskmanager.media.api.dto.StorageSaveResult;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class FileSystemStorage implements FileStorage {

    @Getter
    private final Path storageRoot;
    private final ExecutorService ioExecutor = Executors.newCachedThreadPool();

    private static final long MAX_SIZE = 5L * 1024 * 1024; // 5 MB

    public FileSystemStorage(@Value("${app.media.storage-root:media_storage}") String root) throws IOException {
        this.storageRoot = Paths.get(root).toAbsolutePath().normalize();
        Files.createDirectories(this.storageRoot);
    }

    @Override
    public CompletableFuture<StorageSaveResult> save(MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                validateFile(file);
                // Вычисление контрольной суммы (SHA-256)
                String checksum;
                try (InputStream is = file.getInputStream()) {
                    checksum = DigestUtils.sha256Hex(is);
                }
                String dir1 = checksum.substring(0,2);
                String dir2 = checksum.substring(2,4);
                String ext = FilenameUtils.getExtension(file.getOriginalFilename());
                String baseName = checksum;
                String fileName = baseName + (ext != null && !ext.isBlank() ? "." + ext : "");
                // Относительный путь для хранения в БД
                String relPath  = Paths.get(dir1, dir2, baseName, fileName).toString();
                Path targetDir = storageRoot.resolve(dir1).resolve(dir2).resolve(baseName);
                Files.createDirectories(targetDir);
                Path target = targetDir.resolve(fileName);
                // Если файл существует, запись не происходит
                if (!Files.exists(target)) {
                    try (InputStream is = file.getInputStream()) {
                        Files.copy(is, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                UUID id = UUID.nameUUIDFromBytes(checksum.getBytes());
                return new StorageSaveResult(id, checksum, relPath);

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, ioExecutor);
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Файл слишком большой. Максимальный размер 5 MB");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String ext = FilenameUtils.getExtension(original);
        // Проверка на разрешенный формат
        try {
            byte[] header = new byte[12];
            try (InputStream is = file.getInputStream()) {
                int read = is.read(header, 0, header.length);
            }
            if (!isValidImageByMagic(header, ext)) {
                throw new IllegalArgumentException("Данный тип изображения не поддерживается. Список поддерживаемых расширений: png, jpg, webp");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isValidImageByMagic(byte[] header, String ext) {
        if (header == null) return false;
        // PNG: 89 50 4E 47
        if (header.length >= 4 && header[0]==(byte)0x89 && header[1]==0x50 && header[2]==0x4E && header[3]==0x47) return true;
        // JPG: FF D8 FF
        if (header.length >= 3 && (header[0]==(byte)0xFF && header[1]==(byte)0xD8 && header[2]==(byte)0xFF)) return true;
        // WEBP: "RIFF" .... "WEBP"
        if (header.length >= 12 &&
                header[0]=='R' && header[1]=='I' && header[2]=='F' && header[3]=='F' &&
                header[8]=='W' && header[9]=='E' && header[10]=='B' && header[11]=='P') return true;
        return ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") || ext.equalsIgnoreCase("webp");
    }

    @Override
    public CompletableFuture<InputStream> load(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path absolutePath = storageRoot.resolve(filePath).normalize();
                if (!Files.exists(absolutePath)) {
                    throw new FileNotFoundException("Файл не найден: " + filePath);
                }
                return Files.newInputStream(absolutePath, StandardOpenOption.READ);
            } catch (IOException e) {
                throw new UncheckedIOException("Ошибка при чтении файла: " + filePath, e);
            }
        }, ioExecutor);
    }

    @Override
    public CompletableFuture<Boolean> delete(String filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path absolutePath = storageRoot.resolve(filePath).normalize();

                // Проверка что файл находится в хранилище
                if (!absolutePath.startsWith(storageRoot.normalize())) {
                    throw new SecurityException("Попытка удаления файла вне хранилища: " + filePath);
                }

                boolean deleted = Files.deleteIfExists(absolutePath);

                // Удаляем пустые родительские директории (dir1/dir2/baseName)
                if (deleted) {
                    Path dir = absolutePath.getParent();
                    while (dir != null && !dir.equals(storageRoot) && Files.isDirectory(dir)) {
                        try {
                            Files.delete(dir);
                            dir = dir.getParent();
                        } catch (DirectoryNotEmptyException e) {
                            break; // В директории ещё есть файлы — останавливаемся
                        }
                    }
                }
                return deleted;
            } catch (IOException e) {
                throw new UncheckedIOException("Ошибка при удалении файла: " + filePath, e);
            }
        }, ioExecutor);
    }
}
