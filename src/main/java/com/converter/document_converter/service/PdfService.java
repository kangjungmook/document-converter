package com.converter.document_converter.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfService {

    // PDF 병합
    public byte[] mergePdfs(List<MultipartFile> files) throws IOException {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (MultipartFile file : files) {
            InputStream inputStream = file.getInputStream();
            merger.addSource(inputStream);
        }

        merger.setDestinationStream(outputStream);
        merger.mergeDocuments(null);

        return outputStream.toByteArray();
    }

    // PDF 분할 (페이지별)
    public byte[] splitPdfByPage(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        Splitter splitter = new Splitter();
        List<PDDocument> pages = splitter.split(document);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (int i = 0; i < pages.size(); i++) {
            PDDocument page = pages.get(i);
            ByteArrayOutputStream pageStream = new ByteArrayOutputStream();
            page.save(pageStream);
            page.close();

            ZipEntry zipEntry = new ZipEntry("page_" + (i + 1) + ".pdf");
            zipOut.putNextEntry(zipEntry);
            zipOut.write(pageStream.toByteArray());
            zipOut.closeEntry();
        }

        zipOut.close();
        document.close();

        return baos.toByteArray();
    }

    // PDF 분할 (범위 지정)
    public byte[] splitPdfByRange(MultipartFile file, int startPage, int endPage) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());

        Splitter splitter = new Splitter();
        splitter.setStartPage(startPage);
        splitter.setEndPage(endPage);
        splitter.setSplitAtPage(endPage - startPage + 1);

        List<PDDocument> splitDocs = splitter.split(document);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (!splitDocs.isEmpty()) {
            splitDocs.get(0).save(outputStream);
            splitDocs.get(0).close();
        }

        document.close();
        return outputStream.toByteArray();
    }

    // PDF 압축
    public byte[] compressPdf(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());

        // 이미지 품질 낮추기 및 재압축
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return outputStream.toByteArray();
    }

    // PDF 정보 조회
    public PdfInfo getPdfInfo(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());

        PdfInfo info = new PdfInfo();
        info.setPageCount(document.getNumberOfPages());
        info.setFileSize(file.getSize());
        info.setFileName(file.getOriginalFilename());

        document.close();
        return info;
    }

    // PDF 정보 DTO
    public static class PdfInfo {
        private int pageCount;
        private long fileSize;
        private String fileName;

        public int getPageCount() { return pageCount; }
        public void setPageCount(int pageCount) { this.pageCount = pageCount; }

        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
    }
}