package com.wip.workipedia.manual.domain;

import java.util.Map;

// manual_versions.update_reason 코드를 사용자에게 보여줄 한국어 문장으로 변환한다.
// 기존 FE(ManualListView.vue formatUpdateReason)의 라벨 책임을 BE로 이전한 것.
public final class UpdateReasonLabel {

    private static final Map<String, String> LABELS = Map.ofEntries(
        Map.entry("INITIAL_PDF_UPLOAD", "PDF 매뉴얼이 처음 등록되었습니다."),
        Map.entry("PDF_UPLOAD", "PDF 파일이 새 버전으로 업로드되었습니다."),
        Map.entry("FILE_ADDED", "첨부 파일이 추가되었습니다."),
        Map.entry("FILE_REMOVED", "첨부 파일이 삭제되었습니다."),
        Map.entry("FILE_REPLACED", "첨부 파일이 교체되었습니다."),
        Map.entry("ADMIN_UPDATE", "관리자가 매뉴얼 정보를 수정했습니다."),
        Map.entry("CONTENT_UPDATE", "매뉴얼 본문이 수정되었습니다."),
        Map.entry("METADATA_UPDATE", "매뉴얼 기본 정보가 수정되었습니다."),
        Map.entry("INITIAL_CREATE", "매뉴얼이 생성되었습니다.")
    );

    private UpdateReasonLabel() {
    }

    public static String toLabel(String updateReason) {
        if (updateReason == null || updateReason.isBlank()) {
            return "";
        }
        return LABELS.getOrDefault(updateReason.trim().toUpperCase(), updateReason);
    }
}
