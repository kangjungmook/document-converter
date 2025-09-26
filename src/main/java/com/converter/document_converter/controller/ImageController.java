package com.converter.document_converter.controller;

import com.converter.document_converter.service.ImageService;
import com.converter.document_converter.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Controller
@RequestMapping("/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final UserService userService;

    @GetMapping("/resize")
    public String resizePage() {
        return "image-resize";
    }

    @GetMapping("/compress")
    public String compressPage() {
        return "image-compress";
    }

    @PostMapping("/api/resize")
    @ResponseBody
    public ResponseEntity<?> resizeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("width") int width,
            @RequestParam("height") int height,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        try {
            // 사용자 체크
            if (userId != null) {
                userService.checkFileSize(userId, file.getSize());
                userService.incrementUsage(userId);
            }

            byte[] resizedImage = imageService.resizeImage(file, width, height);
            ByteArrayResource resource = new ByteArrayResource(resizedImage);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=resized_" + file.getOriginalFilename())
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(resizedImage.length)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/api/compress")
    @ResponseBody
    public ResponseEntity<?> compressImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("quality") double quality,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        try {
            // 사용자 체크
            if (userId != null) {
                userService.checkFileSize(userId, file.getSize());
                userService.incrementUsage(userId);
            }

            byte[] compressedImage = imageService.compressImage(file, quality);
            ByteArrayResource resource = new ByteArrayResource(compressedImage);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=compressed_" + file.getOriginalFilename())
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(compressedImage.length)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }
}