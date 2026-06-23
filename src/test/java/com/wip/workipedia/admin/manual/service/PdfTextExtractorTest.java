package com.wip.workipedia.admin.manual.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

class PdfTextExtractorTest {

	private final PdfTextExtractor extractor = new PdfTextExtractor();

	@Test
	void extract_returnsTextFromTextPdf() throws IOException {
		MultipartFile file = pdfFile("manual.pdf", createTextPdf("manual content"));

		String text = extractor.extract(file, file.getBytes());

		assertThat(text).contains("manual content");
	}

	@Test
	void extract_rejectsPdfWithoutText() throws IOException {
		MultipartFile file = pdfFile("empty.pdf", createEmptyPdf());

		assertInvalidFile(file);
	}

	@Test
	void extract_rejectsWhitespaceOnlyTextPdf() throws IOException {
		MultipartFile file = pdfFile("blank.pdf", createTextPdf("   "));

		assertInvalidFile(file);
	}

	private void assertInvalidFile(MultipartFile file) {
		assertThatThrownBy(() -> extractor.extract(file, file.getBytes()))
			.isInstanceOf(CustomException.class)
			.extracting("errorType")
			.isEqualTo(ErrorType.MANUAL_INVALID_FILE);
	}

	private MockMultipartFile pdfFile(String filename, byte[] bytes) {
		return new MockMultipartFile("file", filename, "application/pdf", bytes);
	}

	private byte[] createEmptyPdf() throws IOException {
		try (PDDocument document = new PDDocument();
			 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			document.addPage(new PDPage());
			document.save(output);
			return output.toByteArray();
		}
	}

	private byte[] createTextPdf(String text) throws IOException {
		try (PDDocument document = new PDDocument();
			 ByteArrayOutputStream output = new ByteArrayOutputStream()) {
			PDPage page = new PDPage();
			document.addPage(page);
			try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
				contentStream.beginText();
				contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
				contentStream.newLineAtOffset(50, 700);
				contentStream.showText(text);
				contentStream.endText();
			}
			document.save(output);
			return output.toByteArray();
		}
	}
}
