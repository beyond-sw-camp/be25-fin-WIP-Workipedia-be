package com.wip.workipedia.directdata.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.directdata.domain.DirectData;
import com.wip.workipedia.directdata.dto.DirectDataResponse;
import com.wip.workipedia.directdata.repository.DirectDataRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectDataService {

    private final DirectDataRepository directDataRepository;

    public PageResponse<DirectDataResponse> findAll(String category, String keyword, Pageable pageable) {
        return PageResponse.from(directDataRepository
                .findAll(searchSpec(category, keyword), pageable)
                .map(DirectDataResponse::from));
    }

    public DirectDataResponse findById(Long directDataId) {
        return directDataRepository.findByDirectDataIdAndDeletedAtIsNull(directDataId)
                .filter(DirectData::isActive)
                .map(DirectDataResponse::from)
                .orElseThrow(() -> new CustomException(ErrorType.DIRECT_DATA_NOT_FOUND));
    }

    private Specification<DirectData> searchSpec(String category, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            predicates.add(criteriaBuilder.equal(root.get("isActive"), "Y"));
            if (category != null && !category.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            if (keyword != null && !keyword.isBlank()) {
                String likeKeyword = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), likeKeyword)
                ));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
