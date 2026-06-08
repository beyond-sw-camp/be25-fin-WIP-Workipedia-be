package com.wip.workipedia.manual.service;

import com.wip.workipedia.manual.domain.Manual;
import com.wip.workipedia.manual.domain.ManualChunk;
import com.wip.workipedia.manual.repository.ManualChunkRepository;
import java.util.ArrayList;
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
public class ManualChunkService {

    private static final int MAX_CHUNK_LENGTH = 1_000;

    private final ManualChunkRepository manualChunkRepository;

    // 나눈 chunk 목록을 DB의 manual_chunks와 "동기화"함
    @Transactional
    public void rebuildChunks(Manual manual) {
        List<String> chunkContents = splitIntoChunks(manual.getContent());
        Map<Integer, ManualChunk> existingByIndex = manualChunkRepository.findByManualIdOrderByChunkIndexAsc(
                        manual.getManualId())
                .stream()
                .collect(Collectors.toMap(ManualChunk::getChunkIndex, Function.identity()));

        List<ManualChunk> chunksToSave = new ArrayList<>();
        for (int index = 0; index < chunkContents.size(); index++) {
            ManualChunk existing = existingByIndex.remove(index);
            if (existing == null) {
                chunksToSave.add(ManualChunk.create(manual.getManualId(), index, chunkContents.get(index)));
                continue;
            }
            existing.replaceContent(chunkContents.get(index));
        }

        existingByIndex.values().forEach(ManualChunk::markDeleted);
        manualChunkRepository.saveAll(chunksToSave);
    }

    // manual_chunks 테이블에서 메뉴얼 ID와 삭제되지 않은 조건으로 조회
    public List<ManualChunk> findActiveChunks(Long manualId) {
        return manualChunkRepository.findByManualIdAndDeletedAtIsNullOrderByChunkIndexAsc(manualId);
    }

    // manual_chunks 테이블에서 메뉴얼 ID와 삭제되지 않은 조건으로 삭제
    @Transactional
    public void deleteChunks(Long manualId) {
        manualChunkRepository.findByManualIdAndDeletedAtIsNullOrderByChunkIndexAsc(manualId)
                .forEach(ManualChunk::markDeleted);
    }

    // 문자열을 chunk 문자열 목록으로 "나누기만" 함
    private List<String> splitIntoChunks(String content) {
        if (content == null || content.isBlank()) {
            return List.of();
        }

        // 문자열 하나하나 받아서 청크로 나누기
        List<String> chunks = new ArrayList<>();
        String normalized = content.replace("\r\n", "\n").replace('\r', '\n');
        for (String paragraph : normalized.split("\\n\\s*\\n+")) {
            String trimmed = paragraph.replaceAll("[ \\t\\n]+", " ").trim();
            if (!trimmed.isBlank()) {
                splitLongParagraph(trimmed, chunks); // 1000 자 이상의 문장을 끊어서 여러 청크로 나누는 방식의 메서드
            }
        }
        return chunks;
    }

    // 1000 자 이상의 문장을 끊어서 여러 청크로 나누는 방식의 메서드
    private void splitLongParagraph(String paragraph, List<String> chunks) {
        String remaining = paragraph;
        while (remaining.length() > MAX_CHUNK_LENGTH) {
            int splitAt = findSplitPoint(remaining);
            chunks.add(remaining.substring(0, splitAt).trim());
            remaining = remaining.substring(splitAt).trim();
        }
        if (!remaining.isBlank()) {
            chunks.add(remaining);
        }
    }
    // 이번 chunk를 몇 번째 글자에서 자를까? 를 정함.
    private int findSplitPoint(String text) {
        int max = Math.min(MAX_CHUNK_LENGTH, text.length());
        int splitAt = text.lastIndexOf(' ', max);
        if (splitAt < MAX_CHUNK_LENGTH / 2) {
            splitAt = max;
        }
        return splitAt;
    }
}
