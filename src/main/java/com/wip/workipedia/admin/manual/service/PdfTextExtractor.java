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

    public String extract(MultipartFile file) {
        validateReadable(file, null);

        try {
            return extract(file, file.getBytes());
        } catch (IOException e) {
            log.warn("Failed to read PDF file filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일을 읽을 수 없습니다.");
        }
    }

    public String extract(MultipartFile file, byte[] bytes) {
        validateReadable(file, bytes);

        try (PDDocument document = Loader.loadPDF(bytes)) {
            String text = new PDFTextStripper().getText(document).trim();
            if (text.isBlank()) {
                throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일에서 추출 가능한 텍스트가 없습니다.");
            }
            return text;
        } catch (CustomException e) {
            throw e;
        } catch (IOException e) {
            log.warn("Failed to extract PDF text filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 텍스트를 추출할 수 없습니다.");
        }
    }

    private void validateReadable(MultipartFile file, byte[] bytes) {
        if (file == null || file.isEmpty() || bytes != null && bytes.length == 0) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일은 필수입니다.");
        }
    }
}
