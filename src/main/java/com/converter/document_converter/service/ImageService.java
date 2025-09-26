package com.converter.document_converter.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {

    public byte[] resizeImage(MultipartFile file, int width, int height) throws IOException {
        // 원본 이미지 읽기
        BufferedImage originalImage = ImageIO.read(file.getInputStream());

        // 리사이즈
        BufferedImage resizedImage = Thumbnails.of(originalImage)
                .size(width, height)
                .asBufferedImage();

        // byte[]로 변환
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String format = getImageFormat(file.getOriginalFilename());
        ImageIO.write(resizedImage, format, outputStream);

        return outputStream.toByteArray();
    }

    public byte[] compressImage(MultipartFile file, double quality) throws IOException {
        // 압축 (quality: 0.0 ~ 1.0)
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(file.getInputStream())
                .scale(1.0)  // 크기 유지
                .outputQuality(quality)  // 품질 조절
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }

    private String getImageFormat(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "jpg";
            case "png" -> "png";
            case "gif" -> "gif";
            case "bmp" -> "bmp";
            default -> "jpg";
        };
    }
}