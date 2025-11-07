package com.boot.ict05_final_admin.domain.inventory.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 출고 서비스 (InventoryOutService)
 */
@Deprecated
@Service
public class InventoryInOutService {
    // 기존 컨트롤러 호환용. 내부에서 새 서비스 위임만 수행.
    @Autowired
    private InventoryInService inService;
    @Autowired private InventoryOutService outService;
}
