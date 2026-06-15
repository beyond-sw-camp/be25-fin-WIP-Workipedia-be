package com.wip.workipedia.directdata.controller;

import com.wip.workipedia.common.request.BasePageRequest;
import com.wip.workipedia.common.response.PageResponse;
import com.wip.workipedia.directdata.dto.DirectDataResponse;
import com.wip.workipedia.directdata.service.DirectDataService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/direct-data")
@RequiredArgsConstructor
public class DirectDataController {

    private final DirectDataService directDataService;

    @GetMapping
    public ResponseEntity<PageResponse<DirectDataResponse>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @Valid BasePageRequest pageRequest) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        return ResponseEntity.ok(directDataService.findAll(
                category,
                keyword,
                pageRequest.toPageable(sort)
        ));
    }

    @GetMapping("/{directDataId}")
    public ResponseEntity<DirectDataResponse> findById(@PathVariable Long directDataId) {
        return ResponseEntity.ok(directDataService.findById(directDataId));
    }
}
