package com.converter.document_converter.controller;

import com.converter.document_converter.entity.User;
import com.converter.document_converter.service.PdfService;
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
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pdf")
@RequiredArgsConstructor
public class PdfController {

    private final PdfService pdfService;
    private final UserService userService;

    // PDF 합치기 페이지
    @GetMapping("/merge")
    public String mergePage() {
        return "pdf-merge";
    }

    // PDF 분할 페이지
    @GetMapping("/split")
    public String splitPage() {
        return "pdf-split";
    }

    // PDF 병합 API
    @PostMapping("/api/merge")
    @ResponseBody
    public ResponseEntity<?> mergePdf(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        try {
            // 로그인한 사용자면 사용량 체크
            if (userId != null) {
                // 파일 크기 체크
                long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
                userService.checkFileSize(userId, totalSize);

                // 기능별 사용량 증가
                userService.incrementUsage(userId, User.FeatureType.PDF_MERGE);
            }

            byte[] mergedPdf = pdfService.mergePdfs(files);
            ByteArrayResource resource = new ByteArrayResource(mergedPdf);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=merged.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(mergedPdf.length)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }

    // PDF 분할 API (전체 페이지)
    @PostMapping("/api/split")
    @ResponseBody
    public ResponseEntity<?> splitPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        try {
            // 사용자 체크
            if (userId != null) {
                userService.checkFileSize(userId, file.getSize());
                userService.incrementUsage(userId, User.FeatureType.PDF_SPLIT);
            }

            byte[] zipFile = pdfService.splitPdfByPage(file);
            ByteArrayResource resource = new ByteArrayResource(zipFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=split_pages.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(zipFile.length)
                    .body(resource);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }

    // PDF 분할 API (범위 지정)
    @PostMapping("/api/split-range")
    @ResponseBody
    public ResponseEntity<?> splitPdfRange(
            @RequestParam("file") MultipartFile file,
            @RequestParam("startPage") int startPage,
            @RequestParam("endPage") int endPage,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        try {
            if (userId != null) {
                userService.checkFileSize(userId, file.getSize());
                userService.incrementUsage(userId, User.FeatureType.PDF_SPLIT);
            }

            byte[] splitPdf = pdfService.splitPdfByRange(file, startPage, endPage);
            ByteArrayResource resource = new ByteArrayResource(splitPdf);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=pages_" + startPage + "_to_" + endPage + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(splitPdf.length)
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "파일 처리 중 오류가 발생했습니다."));
        }
    }

    // PDF 정보 조회 API
    @PostMapping("/api/info")
    @ResponseBody
    public ResponseEntity<PdfService.PdfInfo> getPdfInfo(@RequestParam("file") MultipartFile file)
            throws IOException {
        PdfService.PdfInfo info = pdfService.getPdfInfo(file);
        return ResponseEntity.ok(info);
    }

    // 사용량 조회 API
    @GetMapping("/api/usage/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserUsage(@PathVariable Long userId) {
        try {
            UserService.UsageInfo usage = userService.getUserUsage(userId);
            return ResponseEntity.ok(usage);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}