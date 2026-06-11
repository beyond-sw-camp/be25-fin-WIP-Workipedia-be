package com.wip.workipedia.manual.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.manual.domain.ManualStatus;
import com.wip.workipedia.manual.dto.ManualDetailResponse;
import com.wip.workipedia.manual.dto.ManualSummaryResponse;
import com.wip.workipedia.manual.repository.ManualFileRepository;
import com.wip.workipedia.manual.repository.ManualRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManualService {

    private final ManualRepository manualRepository;
    private final ManualFileRepository manualFileRepository;

    public PageResponse<ManualSummaryResponse> findPublished(Pageable pageable) {
        return PageResponse.from(
                manualRepository.findByDeletedAtIsNullAndStatus(ManualStatus.PUBLISHED, pageable)
                        .map(ManualSummaryResponse::from)
        );
    }

    public ManualDetailResponse findPublishedById(Long manualId) {
        return manualRepository.findByManualIdAndDeletedAtIsNullAndStatus(manualId, ManualStatus.PUBLISHED)
                .map(manual -> ManualDetailResponse.from(manual, findFileUrls(manual.getManualId())))
                .orElseThrow(() -> new CustomException(ErrorType.MANUAL_NOT_FOUND));
    }

    private List<String> findFileUrls(Long manualId) {
        return manualFileRepository.findByManualManualIdAndDeletedAtIsNullOrderBySortOrderAsc(manualId)
                .stream()
                .map(manualFile -> manualFile.getFileUrl())
                .toList();
    }
}
