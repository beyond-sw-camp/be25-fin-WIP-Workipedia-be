package com.wip.workipedia.manual.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class UpdateReasonLabelTest {

    @Test
    void knownCodes_mapToSentences() {
        assertThat(UpdateReasonLabel.toLabel("INITIAL_PDF_UPLOAD")).isEqualTo("PDF 매뉴얼이 처음 등록되었습니다.");
        assertThat(UpdateReasonLabel.toLabel("FILE_ADDED")).isEqualTo("첨부 파일이 추가되었습니다.");
        assertThat(UpdateReasonLabel.toLabel("PDF_UPLOAD")).isEqualTo("PDF 파일이 새 버전으로 업로드되었습니다.");
    }

    @Test
    void unknownCode_returnsOriginal() {
        assertThat(UpdateReasonLabel.toLabel("SOMETHING_ELSE")).isEqualTo("SOMETHING_ELSE");
    }

    @Test
    void nullOrBlank_returnsEmpty() {
        assertThat(UpdateReasonLabel.toLabel(null)).isEqualTo("");
        assertThat(UpdateReasonLabel.toLabel("   ")).isEqualTo("");
    }
}
