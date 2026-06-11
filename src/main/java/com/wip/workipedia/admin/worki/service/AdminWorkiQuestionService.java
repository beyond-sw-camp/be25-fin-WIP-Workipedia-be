package com.wip.workipedia.admin.worki.service;

import com.wip.workipedia.admin.worki.dto.AdminWorkiQuestionDeleteResponse;
import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
import com.wip.workipedia.point.dto.MyPointResponse;
import com.wip.workipedia.point.service.PointService;
import com.wip.workipedia.worki.domain.WorkiQuestion;
import com.wip.workipedia.worki.event.WorkiQuestionChangedEvent;
import com.wip.workipedia.worki.repository.WorkiQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminWorkiQuestionService {
	private static final int INAPPROPRIATE_CONTENT_DEDUCT_POINT = 100;
	private static final String INAPPROPRIATE_CONTENT_REASON_TYPE = "부적절한 워키게시글 작성으로 인한 차감";
	private static final String WORKI_QUESTION_RELATED_TYPE = "WORKI_QUESTION";

	private final WorkiQuestionRepository questionRepository;
	private final PointService pointService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public AdminWorkiQuestionDeleteResponse delete(Long questionId) {
		WorkiQuestion question = questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
			.orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));

		question.deleteByAdmin();
		int deductedPoint = deductAuthorPoint(question);

		eventPublisher.publishEvent(new WorkiQuestionChangedEvent(question.getQuestionId()));
		long remainingPoint = pointService.getMyPoint(question.getAuthorId()).currentPoint();

		return new AdminWorkiQuestionDeleteResponse(
			question.getQuestionId(),
			question.getAuthorId(),
			deductedPoint,
			remainingPoint
		);
	}

	private int deductAuthorPoint(WorkiQuestion question) {
		MyPointResponse point = pointService.getMyPoint(question.getAuthorId());
		int deductPoint = Math.toIntExact(Math.min(INAPPROPRIATE_CONTENT_DEDUCT_POINT, point.currentPoint()));
		if (deductPoint == 0) {
			return 0;
		}

		pointService.spendPoint(
			question.getAuthorId(),
			deductPoint,
			INAPPROPRIATE_CONTENT_REASON_TYPE,
			WORKI_QUESTION_RELATED_TYPE,
			question.getQuestionId()
		);
		return deductPoint;
	}
}
