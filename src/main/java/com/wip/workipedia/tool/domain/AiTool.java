package com.wip.workipedia.tool.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ai_tools")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiTool {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ai_tool_id")
	private Long aiToolId;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, length = 1000)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ToolType toolType;

	@Column(length = 1000)
	private String endpointUrl;

	@Column(length = 10)
	private String httpMethod;

	@Column(length = 100)
	private String datasourceKey;

	@Column(columnDefinition = "LONGTEXT")
	private String queryTemplate;

	@Column(nullable = false, columnDefinition = "JSON")
	private String parametersSchema;

	@Column(columnDefinition = "JSON")
	private String responseSchema;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AuthType authType;

	@Column(length = 255)
	private String credentialRef;

	@Column(nullable = false)
	private int timeoutMs;

	@Column(nullable = false)
	private int maxResultCount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private ApprovalStatus approvalStatus;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String isActive;

	private Long createdBy;
	private Long updatedBy;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;

	@Column(nullable = false, columnDefinition = "CHAR(1)")
	private String isDeleted;

	@PrePersist
	void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	public static AiTool createHttpApiTool(
		String name, String description,
		String endpointUrl, String httpMethod, String parametersSchema, String responseSchema,
		AuthType authType, String credentialRef, int timeoutMs, int maxResultCount, Long createdBy
	) {
		AiTool tool = new AiTool();
		tool.name = name;
		tool.description = description;
		tool.toolType = ToolType.HTTP_API;
		tool.endpointUrl = endpointUrl;
		tool.httpMethod = httpMethod;
		tool.parametersSchema = parametersSchema;
		tool.responseSchema = responseSchema;
		tool.authType = authType;
		tool.credentialRef = credentialRef;
		tool.timeoutMs = timeoutMs;
		tool.maxResultCount = maxResultCount;
		tool.approvalStatus = ApprovalStatus.APPROVED;
		tool.isActive = "N";
		tool.isDeleted = "N";
		tool.createdBy = createdBy;
		tool.updatedBy = createdBy;
		return tool;
	}

	public static AiTool createDbQueryTool(
		String name, String description,
		String datasourceKey, String queryTemplate, String parametersSchema, String responseSchema,
		int timeoutMs, int maxResultCount, Long createdBy
	) {
		AiTool tool = new AiTool();
		tool.name = name;
		tool.description = description;
		tool.toolType = ToolType.DB_QUERY;
		tool.datasourceKey = datasourceKey;
		tool.queryTemplate = queryTemplate;
		tool.parametersSchema = parametersSchema;
		tool.responseSchema = responseSchema;
		tool.authType = AuthType.NONE;
		tool.timeoutMs = timeoutMs;
		tool.maxResultCount = maxResultCount;
		tool.approvalStatus = ApprovalStatus.APPROVED;
		tool.isActive = "N";
		tool.isDeleted = "N";
		tool.createdBy = createdBy;
		tool.updatedBy = createdBy;
		return tool;
	}

	public void updateConfig(
		String description, String endpointUrl, String httpMethod,
		String datasourceKey, String queryTemplate, String parametersSchema, String responseSchema,
		AuthType authType, String credentialRef, Integer timeoutMs, Integer maxResultCount, Long updatedBy
	) {
		if (description != null) this.description = description;
		if (endpointUrl != null) this.endpointUrl = endpointUrl;
		if (httpMethod != null) this.httpMethod = httpMethod;
		if (datasourceKey != null) this.datasourceKey = datasourceKey;
		if (queryTemplate != null) this.queryTemplate = queryTemplate;
		if (parametersSchema != null) this.parametersSchema = parametersSchema;
		if (responseSchema != null) this.responseSchema = responseSchema;
		if (authType != null) this.authType = authType;
		if (credentialRef != null) this.credentialRef = credentialRef;
		if (timeoutMs != null) this.timeoutMs = timeoutMs;
		if (maxResultCount != null) this.maxResultCount = maxResultCount;
		this.updatedBy = updatedBy;
	}

	public void changeApprovalStatus(ApprovalStatus approvalStatus, Long updatedBy) {
		this.approvalStatus = approvalStatus;
		this.updatedBy = updatedBy;
	}

	public void changeActive(boolean active, Long updatedBy) {
		this.isActive = active ? "Y" : "N";
		this.updatedBy = updatedBy;
	}

	public boolean isActive() {
		return "Y".equals(this.isActive);
	}

	public boolean isDeleted() {
		return "Y".equals(this.isDeleted);
	}

	public boolean isExecutable() {
		return isActive() && !isDeleted() && approvalStatus == ApprovalStatus.APPROVED;
	}
}
