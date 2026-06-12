package com.wip.workipedia.admin.manual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class ManualPdfValidatorTest {

    private final ManualPdfValidator validator = new ManualPdfValidator("1MB");

    @Test
    void validateAndRead_acceptsValidPdf() throws IOException {
        byte[] pdf = createPdf();
        MultipartFile file = pdfFile("manual.pdf", "application/pdf", pdf);

        byte[] result = validator.validateAndRead(file);

        assertThat(result).isEqualTo(pdf);
    }

    @Test
    void validateAndRead_rejectsNonPdfExtension() throws IOException {
        MultipartFile file = pdfFile("manual.txt", "application/pdf", createPdf());

        assertInvalidFile(file);
    }

    @Test
    void validateAndRead_rejectsNonPdfContentType() throws IOException {
        MultipartFile file = pdfFile("manual.pdf", "text/plain", createPdf());

        assertInvalidFile(file);
    }

    @Test
    void validateAndRead_rejectsEmptyFile() {
        MultipartFile file = pdfFile("manual.pdf", "application/pdf", new byte[0]);

        assertInvalidFile(file);
    }

    @Test
    void validateAndRead_rejectsOversizedFileBeforeParsing() {
        ManualPdfValidator smallLimitValidator = new ManualPdfValidator("4B");
        MultipartFile file = pdfFile("manual.pdf", "application/pdf", "%PDF-".getBytes());

        assertThatThrownBy(() -> smallLimitValidator.validateAndRead(file))
                .isInstanceOf(CustomException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.MANUAL_INVALID_FILE);
    }

    @Test
    void validateAndRead_rejectsEncryptedPdf() throws IOException {
        MultipartFile file = pdfFile("manual.pdf", "application/pdf", createEncryptedPdf());

        assertInvalidFile(file);
    }

    @Test
    void validateAndRead_rejectsDamagedPdf() {
        MultipartFile file = pdfFile("manual.pdf", "application/pdf", "%PDF-damaged".getBytes());

        assertInvalidFile(file);
    }

    private void assertInvalidFile(MultipartFile file) {
        assertThatThrownBy(() -> validator.validateAndRead(file))
                .isInstanceOf(CustomException.class)
                .extracting("errorType")
                .isEqualTo(ErrorType.MANUAL_INVALID_FILE);
    }

    private MockMultipartFile pdfFile(String filename, String contentType, byte[] bytes) {
        return new MockMultipartFile("file", filename, contentType, bytes);
    }

    private byte[] createPdf() throws IOException {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }

    private byte[] createEncryptedPdf() throws IOException {
        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            AccessPermission accessPermission = new AccessPermission();
            StandardProtectionPolicy policy = new StandardProtectionPolicy("owner", "user", accessPermission);
            policy.setEncryptionKeyLength(128);
            document.protect(policy);
            document.save(output);
            return output.toByteArray();
        }
    }
}
