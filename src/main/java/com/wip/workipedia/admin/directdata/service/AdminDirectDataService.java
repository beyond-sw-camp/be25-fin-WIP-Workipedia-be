package com.wip.workipedia.admin.directdata.service;

import com.wip.workipedia.admin.directdata.dto.AdminDirectDataRequest;
import com.wip.workipedia.admin.directdata.dto.AdminDirectDataResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.directdata.domain.DirectData;
import com.wip.workipedia.directdata.repository.DirectDataRepository;
import com.wip.workipedia.user.domain.User;
import com.wip.workipedia.user.domain.UserRole;
import com.wip.workipedia.user.repository.UserRepository;
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
public class AdminDirectDataService {

    private final DirectDataRepository directDataRepository;
    private final UserRepository userRepository;

    public PageResponse<AdminDirectDataResponse> findAll(Long actorUserId, Boolean isActive,
            String category, String keyword, Pageable pageable) {
        assertSystemAdmin(actorUserId);

        return PageResponse.from(directDataRepository
                .findAll(searchSpec(isActive, category, keyword), pageable)
                .map(this::toResponse));
    }

    public AdminDirectDataResponse findById(Long actorUserId, Long directDataId) {
        assertSystemAdmin(actorUserId);
        return toResponse(getActiveDirectData(directDataId));
    }

    @Transactional
    public AdminDirectDataResponse create(Long actorUserId, AdminDirectDataRequest request) {
        assertSystemAdmin(actorUserId);

        DirectData directData = DirectData.create(
                request.title(),
                request.content(),
                request.category(),
                activeOrDefault(request.isActive()),
                actorUserId
        );

        return toResponse(directDataRepository.save(directData));
    }

    @Transactional
    public AdminDirectDataResponse update(Long actorUserId, Long directDataId, AdminDirectDataRequest request) {
        assertSystemAdmin(actorUserId);

        DirectData directData = getActiveDirectData(directDataId);
        directData.update(
                request.title(),
                request.content(),
                request.category(),
                activeOrDefault(request.isActive()),
                actorUserId
        );

        return toResponse(directData);
    }

    @Transactional
    public void delete(Long actorUserId, Long directDataId) {
        assertSystemAdmin(actorUserId);

        DirectData directData = directDataRepository.findByDirectDataIdAndDeletedAtIsNull(directDataId)
                .orElseThrow(() -> deletedOrNotFound(directDataId));

        directData.delete(actorUserId);
    }

    private Specification<DirectData> searchSpec(Boolean isActive, String category, String keyword) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            if (isActive != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), isActive ? "Y" : "N"));
            }
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

    private DirectData getActiveDirectData(Long directDataId) {
        return directDataRepository.findByDirectDataIdAndDeletedAtIsNull(directDataId)
                .orElseThrow(() -> new CustomException(ErrorType.DIRECT_DATA_NOT_FOUND));
    }

    private RuntimeException deletedOrNotFound(Long directDataId) {
        if (directDataRepository.existsByDirectDataIdAndDeletedAtIsNotNull(directDataId)) {
            return new CustomException(ErrorType.DIRECT_DATA_ALREADY_DELETED);
        }
        return new CustomException(ErrorType.DIRECT_DATA_NOT_FOUND);
    }

    private void assertSystemAdmin(Long actorUserId) {
        User user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new CustomException(ErrorType.FORBIDDEN));
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            throw new CustomException(ErrorType.FORBIDDEN);
        }
    }

    private boolean activeOrDefault(Boolean active) {
        return active == null || active;
    }

    private AdminDirectDataResponse toResponse(DirectData directData) {
        return AdminDirectDataResponse.from(directData);
    }
}
