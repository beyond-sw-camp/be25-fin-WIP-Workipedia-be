package com.wip.workipedia.department.ai;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FallbackDepartmentRoutingPromptEditor implements DepartmentRoutingPromptEditor {

	@Override
	public List<RoutingPromptEditResult> edit(List<RoutingPromptEditTarget> targets, String instruction) {
		String trimmedInstruction = instruction.trim();
		List<MatchedTarget> matchedTargets = targets.stream()
			.map(target -> new MatchedTarget(target, trimmedInstruction.indexOf(target.departmentName())))
			.filter(matchedTarget -> matchedTarget.startIndex() >= 0)
			.sorted(Comparator.comparingInt(MatchedTarget::startIndex))
			.toList();

		return matchedTargets.stream()
			.map(matchedTarget -> toEditResult(matchedTarget, matchedTargets, trimmedInstruction))
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

		return currentPrompt.trim()
			+ System.lineSeparator()
			+ segment;
	}

	private record MatchedTarget(
		RoutingPromptEditTarget target,
		int startIndex
	) {
	}
}
