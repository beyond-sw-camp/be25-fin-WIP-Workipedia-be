package com.wip.workipedia.admin.manual.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class ManualPdfValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("application/pdf", "application/x-pdf");
    private static final String PDF_EXTENSION = ".pdf";

    private final long maxFileSizeBytes;

    public ManualPdfValidator(
            @Value("${spring.servlet.multipart.max-file-size:20MB}") String maxFileSize
    ) {
        this.maxFileSizeBytes = DataSize.parse(maxFileSize).toBytes();
    }

    public byte[] validateAndRead(MultipartFile file) {
        validateRequired(file);
        validateExtension(file);
        validateContentType(file);
        validateSize(file.getSize());

        byte[] bytes = readBytes(file);
        validateNotEmpty(bytes);
        validateSize(bytes.length);
        validateReadablePdf(file, bytes);
        return bytes;
    }

    private void validateRequired(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일은 필수입니다.");
        }
    }

    private void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(PDF_EXTENSION)) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일만 업로드할 수 있습니다.");
        }
    }

    private void validateContentType(MultipartFile file) {
        String contentType = normalizeContentType(file.getContentType());
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 콘텐츠 타입만 업로드할 수 있습니다.");
        }
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return null;
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private void validateSize(long size) {
        if (size <= 0) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일은 필수입니다.");
        }
        if (size > maxFileSizeBytes) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일 크기가 제한을 초과했습니다.");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            log.warn("Failed to read PDF file filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일을 읽을 수 없습니다.");
        }
    }

    private void validateNotEmpty(byte[] bytes) {
        if (bytes.length == 0) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일은 필수입니다.");
        }
    }

    private void validateReadablePdf(MultipartFile file, byte[] bytes) {
        try (PDDocument document = Loader.loadPDF(bytes)) {
            if (document.isEncrypted()) {
                throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "암호화된 PDF 파일은 업로드할 수 없습니다.");
            }
        } catch (CustomException e) {
            throw e;
        } catch (InvalidPasswordException e) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "암호화된 PDF 파일은 업로드할 수 없습니다.");
        } catch (IOException e) {
            log.warn("Invalid PDF file filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "유효하지 않은 PDF 파일입니다.");
        }
    }
}
