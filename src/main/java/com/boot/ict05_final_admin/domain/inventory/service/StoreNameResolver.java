package com.boot.ict05_final_admin.domain.inventory.service;

import com.boot.ict05_final_admin.domain.store.entity.Store;
import com.boot.ict05_final_admin.domain.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 가맹점명 조회 공통 리졸버.
 *
 * <p>StoreRepositoryImpl 메서드 명세에 의존하지 않는다.
 * 단일은 findById, 복수는 findAllById(표준 JPA)로 처리한다.</p>
 */
@Component
@RequiredArgsConstructor
public class StoreNameResolver {

    private final StoreRepository storeRepository;

    /** 단일 ID → 이름. 없으면 null. */
    public String resolveOrNull(Long storeId) {
        if (storeId == null) return null;
        return storeRepository.findById(storeId).map(Store::getName).orElse(null);
    }

    /** 단일 ID → 이름. 없으면 id 문자열로 대체. */
    public String resolveOrFallback(Long storeId) {
        if (storeId == null) return null;
        return storeRepository.findById(storeId)
                .map(Store::getName)
                .orElse(String.valueOf(storeId));
    }

    /** 복수 ID → 이름 맵. 존재하지 않는 키는 포함하지 않는다. */
    public Map<Long, String> resolveAll(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return Collections.emptyMap();
        return storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, Store::getName));
    }

    /** 복수 ID → 이름 맵. 누락 키는 id 문자열로 채운다. */
    public Map<Long, String> resolveAllWithFallback(Collection<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return Collections.emptyMap();
        Map<Long, String> map = resolveAll(storeIds);
        for (Long id : storeIds) map.putIfAbsent(id, String.valueOf(id));
        return map;
    }

    /** DTO 채움에 쓸 Long→String 함수. */
    public Function<Long, String> asFunctionWithFallback() {
        return this::resolveOrFallback;
    }
}
