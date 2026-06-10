package com.wip.workipedia.storage.dto;

// 서버가 R2에 직접 업로드한 결과. objectKey는 삭제용, publicUrl은 접근용.
public record StoredObject(
	String objectKey,
	String publicUrl
) {
}
