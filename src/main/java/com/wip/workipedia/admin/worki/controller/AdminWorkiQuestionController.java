package com.wip.workipedia.admin.worki.controller;

import com.wip.workipedia.admin.worki.service.AdminWorkiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/worki/questions")
@RequiredArgsConstructor
public class AdminWorkiQuestionController {

	private final AdminWorkiQuestionService adminWorkiQuestionService;

	// 부적절한 워키 게시글을 관리자 권한으로 삭제하고, 작성자 포인트를 차감한다.
	@DeleteMapping("/{questionId}")
	public ResponseEntity<Void> delete(@PathVariable Long questionId) {
		adminWorkiQuestionService.delete(questionId);
		return ResponseEntity.noContent().build();
	}
}
