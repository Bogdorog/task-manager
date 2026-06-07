package com.sergeev.taskmanager.media.internal.controller;

import com.sergeev.taskmanager.exception.BusinessRuleException;
import com.sergeev.taskmanager.media.api.FileStorage;
import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import com.sergeev.taskmanager.media.internal.mapper.MediaMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaApi mediaService;
    private final MediaMapper mediaMapper;
    private final FileStorage fileStorage;

    @GetMapping("/{id}/meta")
    public MediaAssetDto getMeta(@PathVariable UUID id) {
        return mediaService.getMeta(id);
    }
/*
    @PostMapping("/api/media/upload")
    public ResponseEntity<MediaAssetDto> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetailsImpl user) throws BusinessRuleException {

        // 1. Валидация типа и размера
        validateFile(file); // бросает BusinessRuleException при нарушении

        // 2. Генерируем безопасное имя файла
        String ext = getExtension(file.getOriginalFilename());
        String storedName = UUID.randomUUID() + "." + ext;

        // 3. Сохраняем на диск через FileSystemStorage
        fileStorage.save(storedName, file.getInputStream());

        // 4. Сохраняем метаданные в БД
        MediaAsset asset = mediaService.create(
                file.getOriginalFilename(), storedName,
                file.getContentType(), file.getSize(), user.getId()
        );

        return ResponseEntity.ok(mediaMapper.toDto(asset));
    }*/

    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID id) {
        MediaAssetDto meta = mediaService.getMeta(id);

        // Определяем Content-Type по имени файла из метаданных
        String filename  = meta.fileName() != null ? meta.fileName() : "file";
        MediaType contentType = resolveMediaType(filename);
        Long len = 0L;
        try {
            len = mediaService.getSize(meta.id());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        StreamingResponseBody body = out -> {
            try (InputStream in = mediaService.download(id)) {
                in.transferTo(out);
            }
            catch (Exception e) {
                System.err.println(e.getMessage());
            }
        };

        return ResponseEntity.ok()
                .contentType(contentType)
                .contentLength(len)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(body);
    }

    private MediaType resolveMediaType(String filename) {
        String ext = FilenameUtils.getExtension(filename).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png"         -> MediaType.IMAGE_PNG;
            case "webp"        -> MediaType.parseMediaType("image/webp");
            default            -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }

    private void validateFile(MultipartFile file) throws BusinessRuleException {
        long MAX_SIZE = 10 * 1024 * 1024; // 10 МБ
        Set<String> ALLOWED = Set.of(
                "image/jpeg", "image/png", "image/webp",
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        );
        if (file.getSize() > MAX_SIZE)
            throw new BusinessRuleException("Размер файла не должен превышать 10 МБ");
        if (!ALLOWED.contains(file.getContentType()))
            throw new BusinessRuleException("Недопустимый тип файла: " + file.getContentType());
    }
}
