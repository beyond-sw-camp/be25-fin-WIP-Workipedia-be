package com.wip.workipedia.manual.service;

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
        validate(file);

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            String text = new PDFTextStripper().getText(document).trim();
            if (text.isBlank()) {
                throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF에서 추출된 텍스트가 없습니다.");
            }
            return text;
        } catch (CustomException e) {
            throw e;
        } catch (IOException e) {
            log.warn("PDF 텍스트 추출 실패 filename={}", file.getOriginalFilename(), e);
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 텍스트 추출에 실패했습니다.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일이 필요합니다.");
        }

        String filename = file.getOriginalFilename();
        boolean hasPdfExtension = filename != null && filename.toLowerCase().endsWith(".pdf");
        boolean hasPdfContentType = PDF_CONTENT_TYPE.equalsIgnoreCase(file.getContentType());
        if (!hasPdfExtension && !hasPdfContentType) {
            throw new CustomException(ErrorType.MANUAL_INVALID_FILE, "PDF 파일만 업로드할 수 있습니다.");
        }
    }
}
