package com.wip.workipedia.chatbot.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SourceItem(
	String candidateId,
	String sourceType,
	String sourceId,
	Integer chunkIndex,
	String fileName,
	Integer pageStart,
	Integer pageEnd,
	String title,
	double score,
	String link
) {}
