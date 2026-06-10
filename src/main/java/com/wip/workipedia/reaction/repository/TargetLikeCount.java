package com.wip.workipedia.reaction.repository;

// 대상(target_id)별 반응 개수 배치 집계 결과. 목록에서 좋아요 수를 N+1 없이 한 번에 가져오기 위해 사용한다.
public interface TargetLikeCount {
    Long getTargetId();

    long getLikeCount();
}
