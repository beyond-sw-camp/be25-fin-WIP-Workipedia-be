package com.wip.workipedia.esg.service;

import com.wip.workipedia.admin.dto.AdminEsgResponse;
import com.wip.workipedia.admin.dto.EsgGradeDistributionResponse;
import com.wip.workipedia.esg.domain.EsgGrade;
import com.wip.workipedia.esg.dto.EsgResponse;
import com.wip.workipedia.esg.repository.EsgGradeRepository;
import com.wip.workipedia.point.domain.UserPoint;
import com.wip.workipedia.point.repository.UserPointRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EsgService {

	private static final Long SKELETON_USER_ID = 1L;

	private final UserPointRepository userPointRepository;
	private final EsgGradeRepository esgGradeRepository;

	public EsgResponse getMyEsg() {
		UserPoint userPoint = userPointRepository.findByUserIdAndDeletedAtIsNull(SKELETON_USER_ID)
			.orElse(null);
		if (userPoint == null) {
			return new EsgResponse(SKELETON_USER_ID, 0, null, null, null, null);
		}

		EsgGrade currentGrade = findGrade(userPoint.getGradeId());
		EsgGrade nextGrade = findNextGrade(userPoint.getEsgScore());
		Long nextGradeMinScore = nextGrade == null ? null : nextGrade.getMinScore();
		Long remainingScoreForNextGrade = nextGrade == null ? null : nextGrade.getMinScore() - userPoint.getEsgScore();

		return new EsgResponse(
			userPoint.getUserId(),
			userPoint.getEsgScore(),
			currentGrade == null ? null : currentGrade.getGradeName(),
			nextGradeMinScore,
			remainingScoreForNextGrade,
			currentGrade == null ? null : currentGrade.getGradeImageUrl()
		);
	}

	public AdminEsgResponse getAdminEsg() {
		List<UserPoint> userPoints = userPointRepository.findByDeletedAtIsNull();
		Map<Integer, EsgGrade> gradeById = esgGradeRepository.findAll().stream()
			.filter(grade -> grade.getDeletedAt() == null)
			.collect(Collectors.toMap(EsgGrade::getGradeId, Function.identity()));

		long userCount = userPoints.size();
		long totalEsgScore = userPoints.stream()
			.mapToLong(UserPoint::getEsgScore)
			.sum();
		double averageEsgScore = userCount == 0 ? 0.0 : (double) totalEsgScore / userCount;
		long highestEsgScore = userPoints.stream()
			.mapToLong(UserPoint::getEsgScore)
			.max()
			.orElse(0);

		Map<Integer, Long> countByGradeId = userPoints.stream()
			.collect(Collectors.groupingBy(UserPoint::getGradeId, Collectors.counting()));
		List<EsgGradeDistributionResponse> gradeDistributions = countByGradeId.entrySet().stream()
			.map(entry -> {
				EsgGrade grade = gradeById.get(entry.getKey());
				return new EsgGradeDistributionResponse(
					entry.getKey(),
					grade == null ? null : grade.getGradeName(),
					entry.getValue()
				);
			})
			.sorted(Comparator.comparing(EsgGradeDistributionResponse::gradeId))
			.toList();

		return new AdminEsgResponse(
			userCount,
			totalEsgScore,
			averageEsgScore,
			highestEsgScore,
			gradeDistributions
		);
	}

	private EsgGrade findGrade(Integer gradeId) {
		return esgGradeRepository.findByGradeIdAndDeletedAtIsNull(gradeId)
			.orElse(null);
	}

	private EsgGrade findNextGrade(long esgScore) {
		return esgGradeRepository.findAll().stream()
			.filter(grade -> grade.getDeletedAt() == null)
			.filter(grade -> grade.getMinScore() > esgScore)
			.min(Comparator.comparingLong(EsgGrade::getMinScore))
			.orElse(null);
	}
}
