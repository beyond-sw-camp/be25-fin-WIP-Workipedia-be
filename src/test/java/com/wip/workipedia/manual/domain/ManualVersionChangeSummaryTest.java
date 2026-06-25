package com.wip.workipedia.manual.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ManualVersionChangeSummaryTest {

    @Test
    void applyChangeSummary_setsValue() {
        ManualVersion version = new ManualVersion();
        version.applyChangeSummary("소개서 문구가 수정되었습니다.");
        assertThat(version.getChangeSummary()).isEqualTo("소개서 문구가 수정되었습니다.");
    }

    @Test
    void applyChangeSummary_null_keepsNull() {
        ManualVersion version = new ManualVersion();
        version.applyChangeSummary(null);
        assertThat(version.getChangeSummary()).isNull();
    }
}
