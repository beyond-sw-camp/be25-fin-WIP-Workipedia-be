package com.wip.workipedia.departmentsync.dto;

public record SyncApplyResponse(int created, int updated, int deleted, int merged, int linked, long membersReassigned) {}
