| 프로그램 목록 | 기능/역할 | 메서드명 | 설명 |
|---|---|---|---|
| StoreService | 가맹점 관리 | selectAllOfficeStore(StoreSearchDTO storeSearchDTO, Pageable pageable) |  |
| StoreService | 가맹점 관리 | insertOfficeStore(StoreWriteFormDTO dto) |  |
| StoreService | 가맹점 관리 | findStoreName() |  |
| StoreService | 가맹점 관리 | detailOfficeStore(Long id) |  |
| StoreService | 가맹점 관리 | storeModify(StoreModifyFormDTO dto) |  |
| StoreService | 가맹점 관리 | listHeaderStats() |  |
| StaffService | 직원 관리 | selectAllStaff(StaffSearchDTO staffSearchDTO, Pageable pageable) |  |
| StaffService | 직원 관리 | insertOfficeStaff(StaffWriteFormDTO dto) |  |
| StaffService | 직원 관리 | staffModify(StaffModifyFormDTO dto) |  |
| StaffService | 직원 관리 | deleteStaff(Long id) |  |
| StaffService | 직원 관리 | listHeaderStats() |  |
| ReceiveOrderService | 발주 관리 | selectAllOfficeReceive(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) |  |
| ReceiveOrderService | 발주 관리 | getReceiveOrderDetail(Long id) |  |
| ReceiveOrderService | 발주 관리 | updateStatus(Long id, String action) |  |
| ReceiveOrderService | 발주 관리 | applyStatusFromStore(String orderCode, String status) |  |
| ReceiveOrderService | 발주 관리 | getSummary() |  |
| ReceiveOrderService | 발주 관리 | downloadExcel(ReceiveOrderSearchDTO receiveOrderSearchDTO, Pageable pageable) |  |
| ReceiveOrderService | 발주 관리 | downloadDetailExcel(Long orderId) |  |
| PositionService | 포지션 관리 | memberLinkStaff(Long memberId) |  |
| NoticeService | 공지사항 관리 | insertOfficeNotice(NoticeWriteFormDTO dto, List<MultipartFile> files) |  |
| NoticeService | 공지사항 관리 | selectAllOfficeNotice(NoticeSearchDTO noticeSearchDTO, Pageable pageable) |  |
| NoticeService | 공지사항 관리 | findById(Long id) |  |
| NoticeService | 공지사항 관리 | noticeModify(NoticeModifyFormDTO dto, List<MultipartFile> files) |  |
| NoticeService | 공지사항 관리 | detailNotice(Long id) |  |
| NoticeService | 공지사항 관리 | deleteNotice(Long id) |  |
| NoticeService | 공지사항 관리 | downloadExcel(NoticeSearchDTO noticeSearchDTO, Pageable pageable) |  |
| NoticeAttachmentService | 공지사항 첨부파일 관리 | uploadFile(MultipartFile file) |  |
| NoticeAttachmentService | 공지사항 첨부파일 관리 | findByNoticeId(Long noticeId) |  |
| NoticeAttachmentService | 공지사항 첨부파일 관리 | deleteAttachment(Long id) |  |
| NavGateService | 네비게이션 관리 | isEnabledPath(String requestUri) |  |
| NavGateService | 네비게이션 관리 | selectAllNav(NavSearchDTO navSearchDTO, Pageable pageable) |  |
| NavGateService | 네비게이션 관리 | setEnabled(Long id, boolean enabled) |  |
| NavGateService | 네비게이션 관리 | toggle(Long id) |  |
| MyPageService | 마이페이지 | withdrawMember(Long memberId) |  |
| MenuService | 메뉴 관리 | selectAllStoreMenu(MenuSearchDTO menuSearchDTO, Pageable pageable) |  |
| MenuService | 메뉴 관리 | findMenuById(Long menuId) |  |
| MenuService | 메뉴 관리 | MenuDetail(Long menuId) |  |
| MemberService | 회원 관리 | selectAllMember(MemberSearchDTO memberSearchDTO, Pageable pageable) |  |
| MemberService | 회원 관리 | detailMember(Long id) |  |
| MemberService | 회원 관리 | memberModify(MemberModifyFormDTO dto) |  |
| InventoryBatchService | 재고 배치 관리 | getBatchesByMaterial(final Long materialId) |  |
| InventoryBatchService | 재고 배치 관리 | getLotDetail(final Long batchId) |  |
| InventoryAdjustmentService | 재고 조정 관리 | adjustInventory(final InventoryAdjustDTO dto) |  |
| InventoryAdjustmentService | 재고 조정 관리 | getAdjustDetail(final Long logId) |  |
| InventoryLogViewService | 재고 로그 조회 | getFilteredLogs(final Long materialId, final String type, final LocalDate startDate, final LocalDate endDate, final Pageable pageable) |  |
| InventoryLotService | 재고 로트 관리 | getBatchStatusForMaterial(final Long materialId) |  |
| InventoryLotService | 재고 로트 관리 | getOutLotHistory(final Long batchId, final Pageable pageable) |  |
| InventoryLotService | 재고 로트 관리 | deleteOutLot(final Long lotId) |  |
| InventoryLotService | 재고 로트 관리 | getOutDetailByOutId(final Long outLogId) |  |
| InventoryStockService | 재고 재고 관리 | addToInventory(Long materialId, BigDecimal delta) |  |
| InventoryService | 재고 관리 | listInventory(final InventorySearchDTO searchDTO, final Pageable pageable) |  |
| InventoryService | 재고 관리 | countInventory(final InventorySearchDTO searchDTO) |  |
| InventoryService | 재고 관리 | findByMaterialId(final Long materialId) |  |
| InventoryService | 재고 관리 | findAllForSelect() |  |
| InventoryService | 재고 관리 | hqRemainOfMaterial(final Long materialId) |  |
| InventoryService | 재고 관리 | downloadExcel(final InventorySearchDTO inventorySearchDTO, final Pageable pageable) |  |
| InventoryService | 재고 관리 | downloadLogExcel(final Long materialId, final String type, final LocalDate startDate, final LocalDate endDate, final Pageable pageable) |  |
| InventoryService | 재고 관리 | downloadBatchExcel(final Long materialId) |  |
| InventoryService | 재고 관리 | downloadLotOutHistoryExcel(final Long batchId) |  |
| InventoryService | 재고 관리 | syncInventoryQuantity(final Long materialId, final BigDecimal newQty) |  |
| StoreInventoryService | 매장 재고 관리 | listStoreInventory(final StoreInventorySearchDTO searchDTO, final Pageable pageable) |  |
| StoreInventoryService | 매장 재고 관리 | countStoreInventory(final StoreInventorySearchDTO searchDTO) |  |
| StoreInventoryService | 매장 재고 관리 | downloadExcel(final StoreInventorySearchDTO searchDTO, final Pageable pageable) |  |
| StoreMaterialService | 매장 재료 관리 | listStoreMaterials(final StoreMaterialSearchDTO searchDTO, final Pageable pageable) |  |
| StoreMaterialService | 매장 재료 관리 | countStoreMaterials(final StoreMaterialSearchDTO searchDTO) |  |
| StoreMaterialService | 매장 재료 관리 | downloadExcel(final StoreMaterialSearchDTO searchDTO, final Pageable pageable) |  |
| UnitPriceService | 단가 관리 | setPurchasePrice(Long materialId, BigDecimal price, LocalDateTime validFrom, LocalDateTime validTo) |  |
| UnitPriceService | 단가 관리 | updatePurchasePrice(Long unitPriceId, BigDecimal price) |  |
| UnitPriceService | 단가 관리 | latestPurchasePrice(Long materialId, LocalDateTime at) |  |
| UnitPriceService | 단가 관리 | addPricesForMaterial(Long materialId, BigDecimal unitPrice, BigDecimal sellingPrice) |  |
| MaterialService | 재료 관리 | insertOfficeMaterial(final @Valid MaterialWriteFormDTO dto) |  |
| MaterialService | 재료 관리 | selectAllMaterial(final MaterialSearchDTO materialSearchDTO, final Pageable pageable) |  |
| MaterialService | 재료 관리 | findById(final Long id) |  |
| MaterialService | 재료 관리 | materialModify(final MaterialModifyFormDTO dto) |  |
| MaterialService | 재료 관리 | detailMaterial(final Long id) |  |
| MaterialService | 재료 관리 | deleteMaterial(final Long id) |  |
| MaterialService | 재료 관리 | downloadExcel(final MaterialSearchDTO materialSearchDTO, final Pageable pageable) |  |
| MaterialService | 재료 관리 | findByCategory(final MaterialCategory category) |  |
| InventoryOutService | 재고 출고 관리 | previewFifo(final Long materialId, final BigDecimal qty) |  |
| InventoryOutService | 재고 출고 관리 | hqRemainOfMaterial(final Long materialId) |  |
| InventoryOutService | 재고 출고 관리 | confirmOut(final Long materialId, final Long storeId, final BigDecimal totalQty, final LocalDateTime outDate, final String memo) |  |
| InventoryOutService | 재고 출고 관리 | createOutByReceiveOrder(final ReceiveOrderDetailDTO orderDetail) |  |
| InventoryOutService | 재고 출고 관리 | deleteOut(final Long outId) |  |
| InventoryInService | 재고 입고 관리 | insertInventoryIn(final InventoryInWriteDTO dto) |  |
| InventoryInService | 재고 입고 관리 | deleteIn(final Long inId) |  |
| HomeService | 홈 서비스 | buildDashboard() |  |
| HqInventoryScanService | 본사 재고 스캔 | scanAndNotifyStockLow() |  |
| HqInventoryScanService | 본사 재고 스캔 | scanAndNotifyExpireSoon() |  |
| HqInventoryScanService | 본사 재고 스캔 | scanAll() |  |
| HqAlertStubService | 본사 알림 스텁 | sendHqStockLow(String materialName, long qty, long threshold) |  |
| HqAlertStubService | 본사 알림 스텁 | sendHqExpireSoon(String materialName, int days, String lot) |  |
| HqNoticeFcmBridgeService | 본사 공지사항 FCM 브릿지 | afterNoticeCreated(Long noticeId) |  |
| HqNoticeFcmBridgeService | 본사 공지사항 FCM 브릿지 | afterNoticeUpdated(Long noticeId) |  |
| FcmService | FCM 서비스 | sendToToken(AppType appType, String token, String title, String body, Map<String, String> data) |  |
| FcmService | FCM 서비스 | sendToTopic(AppType appType, String topic, String title, String body, Map<String, String> data) |  |
| FcmService | FCM 서비스 | subscribeToTopic(String topic, Long memberId) |  |
| FcmService | FCM 서비스 | unsubscribeFromTopic(String topic, Long memberId) |  |
| FcmService | FCM 서비스 | sendToTopic(AppType appType, HqTopic topic, String title, String body, Map<String, String> data) |  |
| FcmService | FCM 서비스 | subscribeToTopic(HqTopic topic, Long memberId) |  |
| FcmService | FCM 서비스 | unsubscribeFromTopic(HqTopic topic, Long memberId) |  |
| FcmPreferenceService | FCM 환경설정 | upsertForHqMember(Long memberId, Boolean catNotice, Boolean catStockLow, Boolean catExpireSoon, Integer thresholdDays) |  |
| FcmPreferenceService | FCM 환경설정 | getForHqMember(Long memberId) |  |
| JoinService | 회원 가입 | register(String email, String rawPassword, String name, String phone) |  |
| MemberUserDetailsService | 회원 사용자 상세 정보 | loadUserByUsername(String email) |  |
| AuthService | 인증 서비스 | join(JoinRequest req) |  |
| AuthService | 인증 서비스 | emailExists(String email) |  |
| AuthService | 인증 서비스 | loadUserByUsername(String email) |  |
| AnalyticsService | 분석 서비스 | selectKpiCards() |  |
| AnalyticsService | 분석 서비스 | selectKpis(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | selectOrdersCards() |  |
| AnalyticsService | 분석 서비스 | selectOrders(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | selectMaterialsCards() |  |
| AnalyticsService | 분석 서비스 | selectMaterials(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | selectTimeChartCards() |  |
| AnalyticsService | 분석 서비스 | selectTimeChart(AnalyticsSearchDto cond) |  |
| AnalyticsService | 분석 서비스 | selectTimeRows(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | downloadExcelKpi(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | downloadExcelOrders(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | downloadExcelTime(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | downloadExcelMaterials(AnalyticsSearchDto cond, Pageable pageable) |  |
| AnalyticsService | 분석 서비스 | downloadPdfMaterials(AnalyticsSearchDto cond) |  |
| AnalyticsService | 분석 서비스 | downloadPdfKpi(AnalyticsSearchDto cond) |  |
| AnalyticsService | 분석 서비스 | downloadPdfOrders(AnalyticsSearchDto cond) |  |
| AnalyticsService | 분석 서비스 | downloadPdfTime(AnalyticsSearchDto cond) |  |