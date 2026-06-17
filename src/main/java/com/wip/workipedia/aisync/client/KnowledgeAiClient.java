package com.wip.workipedia.aisync.client;

import com.wip.workipedia.aisync.client.dto.KnowledgeSyncRequest;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.department.domain.Department;
import com.wip.workipedia.department.domain.DepartmentRoutingPrompt;
import com.wip.workipedia.department.repository.DepartmentRepository;
import com.wip.workipedia.department.repository.RoutingPromptRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class KnowledgeAiClient {

    private final RestClient restClient;
    private final DepartmentRepository departmentRepository;
    private final RoutingPromptRepository routingPromptRepository;

    public KnowledgeAiClient(
        @Qualifier("syncAiRestClient") RestClient restClient,
        DepartmentRepository departmentRepository,
        RoutingPromptRepository routingPromptRepository
    ) {
        this.restClient = restClient;
        this.departmentRepository = departmentRepository;
        this.routingPromptRepository = routingPromptRepository;
    }

    public void sync(Long departmentId) {
        Department dept = departmentRepository.findByDepartmentIdAndDeletedAtIsNull(departmentId)
            .orElseThrow(() -> new CustomException(ErrorType.DEPARTMENT_NOT_FOUND));
        DepartmentRoutingPrompt prompt = routingPromptRepository
            .findByDepartment_DepartmentIdAndDeletedAtIsNull(departmentId)
            .orElseThrow(() -> new CustomException(ErrorType.NOT_FOUND));

        try {
            restClient.post()
                .uri("/api/v1/knowledge/sync")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new KnowledgeSyncRequest(
                    departmentId, "DEPT_RR",
                    dept.getDepartmentName(), prompt.getPromptContent(),
                    departmentId, dept.getDepartmentName()
                ))
                .retrieve()
                .toBodilessEntity();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("DEPT_RR AI 동기화 실패: deptId={}, error={}", departmentId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }

    public void delete(Long departmentId) {
        try {
            restClient.delete()
                .uri("/api/v1/knowledge/{sourceId}?source_type=DEPT_RR", departmentId)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("DEPT_RR AI 삭제 실패: deptId={}, error={}", departmentId, e.getMessage());
            throw new CustomException(ErrorType.AI_SYNC_FAILED);
        }
    }
}
