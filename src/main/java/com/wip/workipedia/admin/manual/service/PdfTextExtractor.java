package com.wip.workipedia.admin.manual.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class PdfTextExtractor {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    public String extract(MultipartFile file) {
        validate(file, null);

        try {
            return extract(file, file.getBytes());
        } catch (IOException e) {
            log.warn("Failed to read PDF file filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "Failed to read PDF file.");
        }
    }

    public String extract(MultipartFile file, byte[] bytes) {
        validate(file, bytes);

        try (PDDocument document = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(document).trim();
            if (text.isBlank()) {
                throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF text is empty.");
            }
            return text;
        } catch (CustomException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Failed to extract PDF text filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "Failed to extract PDF text.");
        }
    }

    private void validate(MultipartFile file, byte[] bytes) {
        if (file == null || file.isEmpty() || bytes != null && bytes.length == 0) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF file is required.");
        }

        String filename = file.getOriginalFilename();
        boolean hasPdfExtension = filename != null && filename.toLowerCase().endsWith(".pdf");
        boolean hasPdfContentType = PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType());
        if (!hasPdfExtension && !hasPdfContentType) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "Only PDF files can be uploaded.");
        }
    }
}
