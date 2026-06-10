package com.wip.workipedia.department.ai;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FallbackRoutingPromptEditor implements DepartmentRoutingPromptEditor {

	@Override
	public List<RoutingPromptEditResult> edit(List<RoutingPromptEditTarget> targets, String instruction) {
		String trimmedInstruction = instruction.trim();
		List<MatchedTarget> matchedTargets = findMatchedTargets(targets, trimmedInstruction);

		return matchedTargets.stream()
			.map(matchedTarget -> toEditResult(matchedTarget, matchedTargets, trimmedInstruction))
			.toList();
	}

	private List<MatchedTarget> findMatchedTargets(List<RoutingPromptEditTarget> targets, String instruction) {
		List<MatchedTarget> candidates = targets.stream()
			.map(target -> new MatchedTarget(
				target,
				instruction.indexOf(target.departmentName()),
				instruction.indexOf(target.departmentName()) + target.departmentName().length()
			))
			.filter(matchedTarget -> matchedTarget.startIndex() >= 0)
			.sorted(Comparator
				.comparingInt(MatchedTarget::startIndex)
				.thenComparing((left, right) -> Integer.compare(right.length(), left.length()))
			)
			.toList();

		return candidates.stream()
			.filter(candidate -> candidates.stream()
				.filter(other -> other.startIndex() == candidate.startIndex())
				.findFirst()
				.orElse(candidate) == candidate
			)
			.filter(candidate -> candidates.stream()
				.filter(other -> other.startIndex() < candidate.startIndex())
				.noneMatch(other -> other.endIndex() > candidate.startIndex()))
			.sorted(Comparator.comparingInt(MatchedTarget::startIndex))
			.toList();
	}

	private RoutingPromptEditResult toEditResult(
		MatchedTarget matchedTarget,
		List<MatchedTarget> matchedTargets,
		String instruction
	) {
		String segment = extractSegment(matchedTarget, matchedTargets, instruction);
		String editedPrompt = mergePrompt(matchedTarget.target().currentPrompt(), segment);

		return new RoutingPromptEditResult(matchedTarget.target().departmentId(), editedPrompt);
	}

	private String extractSegment(MatchedTarget matchedTarget, List<MatchedTarget> matchedTargets, String instruction) {
		int currentIndex = matchedTargets.indexOf(matchedTarget);
		int start = matchedTarget.startIndex();
		int end = currentIndex + 1 < matchedTargets.size()
			? matchedTargets.get(currentIndex + 1).startIndex()
			: instruction.length();

		return instruction.substring(start, end).trim();
	}

	private String mergePrompt(String currentPrompt, String segment) {
		if (!StringUtils.hasText(currentPrompt)) {
			return segment;
		}

		String trimmedCurrentPrompt = currentPrompt.trim();
		String trimmedSegment = segment.trim();

		if (hasSameLine(trimmedCurrentPrompt, trimmedSegment)) {
			return trimmedCurrentPrompt;
		}

		return trimmedCurrentPrompt
			+ System.lineSeparator()
			+ trimmedSegment;
	}

	private boolean hasSameLine(String currentPrompt, String segment) {
		String normalizedSegment = normalize(segment);

		return currentPrompt.lines()
			.map(this::normalize)
			.anyMatch(normalizedSegment::equals);
	}

	private String normalize(String value) {
		return value.trim().replaceAll("\\s+", " ");
	}

	private record MatchedTarget(
		RoutingPromptEditTarget target,
		int startIndex,
		int endIndex
	) {
		private int length() {
			return endIndex - startIndex;
		}
	}
}
