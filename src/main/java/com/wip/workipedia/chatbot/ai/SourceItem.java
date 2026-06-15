package com.wip.workipedia.chatbot.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SourceItem(
	String candidateId,
	String sourceType,
	String sourceId,
	Integer chunkIndex,
	String title,
	double score,
	String link
) {}
