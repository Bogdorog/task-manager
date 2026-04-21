package com.sergeev.taskmanager.media.internal.controller;

import com.sergeev.taskmanager.media.api.MediaApi;
import com.sergeev.taskmanager.media.api.dto.MediaAssetDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaApi mediaApi;

    @GetMapping("/{id}/meta")
    public MediaAssetDto getMeta(@PathVariable UUID id) {
        return mediaApi.getMeta(id);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable UUID id) {
        MediaAssetDto meta = mediaApi.getMeta(id);

        // Определяем Content-Type по имени файла из метаданных
        String filename  = meta.fileName() != null ? meta.fileName() : "file";
        MediaType contentType = resolveMediaType(filename);
        Long len = 0L;
        try {
            len = mediaApi.getSize(meta);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        StreamingResponseBody body = out -> {
            try (InputStream in = mediaApi.download(id)) {
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
}
