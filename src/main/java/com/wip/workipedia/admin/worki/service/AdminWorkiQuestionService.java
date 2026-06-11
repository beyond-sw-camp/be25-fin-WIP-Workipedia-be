package com.wip.workipedia.admin.worki.service;

import com.wip.workipedia.common.exception.CustomException;
import com.wip.workipedia.common.exception.ErrorType;
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
	private static final String INAPPROPRIATE_CONTENT_REASON_TYPE = "ADMIN_WORKI_DELETE";
	private static final String WORKI_QUESTION_RELATED_TYPE = "WORKI_QUESTION";

	private final WorkiQuestionRepository questionRepository;
	private final PointService pointService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void delete(Long questionId) {
		WorkiQuestion question = questionRepository.findByQuestionIdAndDeletedAtIsNull(questionId)
			.orElseThrow(() -> new CustomException(ErrorType.WORKI_NOT_FOUND, "질문을 찾을 수 없습니다. id=" + questionId));

		question.deleteByAdmin();
		pointService.spendPoint(
			question.getAuthorId(),
			INAPPROPRIATE_CONTENT_DEDUCT_POINT,
			INAPPROPRIATE_CONTENT_REASON_TYPE,
			WORKI_QUESTION_RELATED_TYPE,
			question.getQuestionId()
		);

		eventPublisher.publishEvent(new WorkiQuestionChangedEvent(question.getQuestionId()));
	}
}
