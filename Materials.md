# 재료(Material) 관련 통계 분석

이 문서는 `AnalyticsRepositoryImpl.java`에 구현된 재료 관련 통계 쿼리의 기능과 로직을 분석하고, `materials.html` 프론트엔드 페이지와의 연동을 설명합니다.

## 주요 엔티티

재료 분석에서는 다음 엔티티들이 주로 사용됩니다.

- **`Material`**: 본사에서 관리하는 재료의 마스터 정보 (재료명, 카테고리, 단위 등)
- **`StoreMaterial`**: 가맹점별로 관리되는 재료 정보 (가맹점 재료명, 판매가, 현재고 등)
- **`StoreInventory`**: 가맹점의 실시간 재고 정보.
- **`Inventory`**: 본사 재고 정보.
- **`InventoryIn`**: 재료 입고 이력.
- **`InventoryOut`**: 재료 출고 이력.
- **`InventoryOutLot`**: 출고된 재료의 로트(LOT) 상세 정보.
- **`InventoryAdjustment`**: 재고 조정 이력.
- **`UnitPrice`**: 재료별 본사 매입 단가와 가맹점 공급 단가의 이력.
- **`ReceiveOrder`**: 가맹점의 발주에 대해 본사가 생성하는 수주 정보 (주문일, 납기일 등)
- **`ReceiveOrderDetail`**: 수주에 포함된 개별 재료의 상세 내역 (수량, 단가 등)

---

## 1. 재료 분석 요약 카드 (`findMaterialsSummary`)

재료 분석 페이지 상단에 표시되는 주요 지표(KPI) 카드를 계산합니다.

### 기능

- **가맹점 전체 현재고**: 모든 가맹점의 현재 재고 수량 총합을 계산합니다.
- **YTD(연초-어제) 집계**:
    - 총 발주 수량
    - 총 원가 (발주 수량 * 발주 단가)
    - 총 매출 (발주 수량 * 판매가)
- **파생 지표**:
    - **총이익**: 총 매출 - 총 원가
    - **평균 마진율**: (총이익 / 총 매출) * 100
    - **재고회전율 (근사치)**: 총 발주 수량 / 전체 현재고

### 쿼리 및 계산 로직

#### 1.1. 가맹점 전체 현재고 (`totalStoreInvQty`)

- **설명**: `StoreInventory` 테이블에서 모든 레코드의 `quantity` 필드를 합산하여 가맹점 전체의 총 재고량을 구합니다.
- **사용 엔티티**: `StoreInventory(si)`, `StoreMaterial(sm)`, `Store(s)`
- **쿼리 핵심**: `SELECT SUM(si.quantity) FROM store_inventory si ...`

#### 1.2. YTD 발주/원가/매출 집계

- **설명**: `ReceiveOrderDetail`을 기준으로, 연초부터 어제까지(`actualDeliveryDate` 기준)의 데이터를 집계합니다.
- **사용 엔티티**: `ReceiveOrderDetail(rod)`, `ReceiveOrder(ro)`, `StoreMaterial(sm)`, `Material(mat)`, `Store(s)`
- **계산 항목**:
    - **총 발주 수량 (`orderVolumeQty`)**: `SUM(rod.count)`
        - 모든 수주 상세 내역의 재료 수량을 합산합니다.
    - **총 원가 (`costSum`)**: `SUM(rod.count * rod.unitPrice)`
        - 각 재료의 수량과 당시 발주 단가(`unitPrice`)를 곱하여 총 원가를 계산합니다.
    - **총 매출 (`sellingSum`)**: `SUM(rod.count * COALESCE(sm.sellingPrice, rod.unitPrice))`
        - 각 재료의 수량과 `StoreMaterial`에 설정된 판매가(`sellingPrice`)를 곱하여 총 매출을 계산합니다.
        - 만약 판매가가 설정되지 않았다면, 발주 단가(`unitPrice`)를 판매가로 간주하여 계산합니다.

---

## 2. 재료 분석 상세 목록 (`findMaterials`)

지정된 기간 동안의 재료별/가맹점별 상세 데이터를 페이징하여 제공합니다. 일별 또는 월별 뷰를 지원합니다.

### 기능

- 기간, 가맹점, 뷰(일별/월별)를 기준으로 재료의 발주, 원가, 매출, 재고 관련 상세 지표를 제공합니다.
- 각 행은 **(가맹점, 재료, 날짜)**를 기준으로 집계됩니다.

### 쿼리 및 계산 로직

#### 2.1. 기본 데이터 집계 (일별/월별)

- **설명**: `ReceiveOrderDetail`을 기준으로 가맹점, 재료, 날짜(일/월)별로 그룹화하여 발주량, 원가, 매출을 집계합니다.
- **사용 엔티티**: `ReceiveOrderDetail(rod)`, `ReceiveOrder(ro)`, `StoreMaterial(sm)`, `Material(mat)`, `Store(s)`
- **계산 항목 (DB)**:
    - **발주 수량 (`purchaseOrderQty`)**: `SUM(rod.count)`
    - **발주 금액 (원가, `purchaseOrderAmount`)**: `SUM(rod.count * rod.unitPrice)`
    - **매출액 (`sellSum`)**: `SUM(rod.count * COALESCE(sm.sellingPrice, rod.unitPrice))`

#### 2.2. 재고 스냅샷 조회

- **설명**: 현재 페이지에 표시된 가맹점과 재료에 한해, 현재 시점의 재고량을 별도 쿼리로 조회하여 맵으로 만듭니다.
- **사용 엔티티**: `StoreMaterial(smInv)`, `StoreInventory(siInv)`
- **로직**:
    - `StoreMaterial`의 `quantity`를 우선적으로 가져옵니다.
    - 만약 `StoreMaterial.quantity`가 0이면, `StoreInventory`의 `quantity` 합계를 사용합니다. 이는 `StoreMaterial`이 집계성 스냅샷을 가지고, `StoreInventory`가 더 상세한 재고 내역을 가질 수 있음을 시사합니다.

#### 2.3. 파생 지표 계산 (Java)

- **이익 (`profit`)**: `매출액 - 원가`
- **마진율 (`margin`)**: `(이익 / 매출액) * 100`
- **기말재고 (`snapshot`)**: '재고 스냅샷' 쿼리에서 가져온 현재 재고량.
- **기초재고 (근사치, `opening`)**: `기말재고 + 기간 내 발주 수량`
    - 해당 기간의 재고 변동이 발주로만 이루어졌다고 가정한 근사치입니다.
- **평균재고 (`avgInventory`)**: `(기말재고 + 기초재고) / 2`
- **재고회전율 (`turnoverRate`)**: `기간 내 발주 수량 / 평균재고`
    - 재고가 얼마나 효율적으로 판매/사용되었는지를 나타내는 지표입니다.
- **일평균 사용량 (`avgUsage`)**:
    - **일별 뷰**: 해당일의 발주 수량.
    - **월별 뷰**: 해당월의 총 발주 수량 / 해당 월의 총 일수.

---

## 3. 페이징용 카운트 쿼리 (`countMaterials`)

- **설명**: `findMaterials` 목록의 전체 행 수를 계산하여 페이징 처리에 사용합니다.
- **로직**: `findMaterials`의 `GROUP BY` 기준과 동일하게 **(가맹점 ID, 재료 ID, 날짜 라벨)**의 고유한 조합 수를 `COUNT(DISTINCT ...)`로 계산합니다. 이를 통해 페이징의 전체 아이템 수와 실제 조회 결과의 수가 일치하도록 보장합니다.

---

## 4. 프론트엔드 (`materials.html`) 연동 분석

`materials.html` 페이지는 `AnalyticsRepositoryImpl`에서 제공하는 `MaterialsCardsDto`와 `Page<MaterialsRowDto>` 데이터를 사용하여 재료 분석 정보를 시각화합니다.

### 4.1. 상단 요약 카드 (MaterialsCardsDto)

`materials.html`은 `materialCard` 객체(MaterialsCardsDto 인스턴스)의 다음 필드를 사용하여 요약 정보를 표시합니다.

- **`Current Office Inventory Qty`**: `materialCard.currentOfficeInventoryQty`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 `0L`로 하드코딩되어 반환됩니다. (주석: `// (HQ 창고 테이블 없으면 0으로 유지)`)
    - **프론트엔드 출력**: 항상 `0`으로 표시됩니다.
- **`Current Total Store Inventory Qty`**: `materialCard.currentTotalStoreInventoryQty`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 모든 가맹점의 `StoreInventory.quantity` 합계로 계산됩니다.
    - **프론트엔드 출력**: 올바르게 계산된 총 가맹점 재고 수량이 표시됩니다.
- **`Order Volume (Qty)`**: `materialCard.orderVolumeQty`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 YTD 기간 동안의 `ReceiveOrderDetail.count` 합계로 계산됩니다.
    - **프론트엔드 출력**: 올바르게 계산된 총 발주 수량이 표시됩니다.
- **`Used Quantity`**: `materialCard.usedQty`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 현재 `orderVolumeQty`와 동일한 값으로 설정됩니다. (주석: `// 사용량 근사`)
    - **프론트엔드 출력**: `Order Volume (Qty)`와 동일한 값이 표시됩니다.
- **`Turnover Rate`**: `materialCard.turnoverRate`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 `(orderVolumeQty / totalStoreInvQty)`로 계산됩니다.
    - **프론트엔드 출력**: 올바르게 계산된 재고 회전율이 표시됩니다.
- **`Sales Amount`**: `materialCard.salesAmount`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 YTD 기간 동안의 총 매출액(`sellingSum`)으로 계산됩니다.
    - **프론트엔드 출력**: 올바르게 계산된 총 매출액이 표시됩니다.
- **`Profit Amount`**: `materialCard.profitAmount`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 `salesAmount - costSum`으로 계산됩니다.
    - **프론트엔드 출력**: 올바르게 계산된 총 이익이 표시됩니다.
- **`Avg. Margin`**: `materialCard.avgMargin`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterialsSummary()`에서 `(profit / salesAmount) * 100`으로 계산됩니다.
    - **프론트엔드 출력**: 올바르게 계산된 평균 마진율이 표시됩니다.

### 4.2. 목록 테이블 (Page<MaterialsRowDto>)

`materials.html`은 `materialrows.content` 객체(MaterialsRowDto 리스트)를 반복하여 상세 테이블을 구성합니다.

- **`Date`**: `r.orderDate`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `ro.actualDeliveryDate`를 포맷한 문자열(`yyyy-MM-dd` 또는 `yyyy-MM`)로 제공됩니다.
    - **프론트엔드 출력**: 조회 모드(일별/월별)에 따라 올바른 날짜/월 라벨이 표시됩니다.
- **`Store`**: `r.store`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `s.name`으로 제공됩니다.
    - **프론트엔드 출력**: 가맹점 이름이 표시됩니다.
- **`Material`**: `r.material`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `mat.name`으로 제공됩니다.
    - **프론트엔드 출력**: 재료 이름이 표시됩니다.
- **`StoreInventoryQty`**: `r.storeInventoryQty`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 해당 가맹점-재료 조합의 현재 재고 스냅샷(`snapshot`)으로 제공됩니다.
    - **프론트엔드 출력**: 현재 재고 수량이 표시됩니다.
- **`PurchaseOrderID` (일별 뷰 전용)**: `r.purchaseOrderId`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 일별 뷰일 경우 `ro.id.min()`으로 제공됩니다.
    - **프론트엔드 출력**: 일별 뷰에서만 발주 ID가 표시됩니다.
- **`PurchaseOrderDate` (일별 뷰 전용)**: `r.purchaseOrderDate`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `orderDate`와 동일한 날짜 라벨(`lbl`)로 제공됩니다.
    - **프론트엔드 출력**: 일별 뷰에서만 발주 날짜가 표시됩니다.
- **`PurchaseOrderQty`**: `r.purchaseOrderQty`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 해당 그룹의 총 발주 수량(`qtyL`)으로 제공됩니다.
    - **프론트엔드 출력**: 발주 수량이 표시됩니다.
- **`PurchaseOrderAmount`**: `r.purchaseOrderAmount`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 해당 그룹의 총 원가(`costSum`)로 제공됩니다.
    - **프론트엔드 출력**: 발주 금액(원가)이 표시됩니다.
- **`Turnover Rate`**: `r.turnoverRate`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `(usedBd / avgInventory)`로 계산되어 제공됩니다.
    - **프론트엔드 출력**: 재고 회전율이 표시됩니다.
- **`Profit`**: `r.profit`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `sellSum - costSum`으로 계산되어 제공됩니다.
    - **프론트엔드 출력**: 이익이 표시됩니다.
- **`Margin`**: `r.margin`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 `(profit / sellSum) * 100`으로 계산되어 제공됩니다.
    - **프론트엔드 출력**: 마진율이 표시됩니다.
- **`Avg.Usage`**: `r.avgUsage`
    - **백엔드 연동**: `AnalyticsRepositoryImpl.findMaterials()`에서 일별/월별 뷰 로직에 따라 계산되어 제공됩니다.
    - **프론트엔드 출력**: 평균 사용량이 표시됩니다.

### 4.3. 프론트엔드 출력 관련 특이사항 및 잠재적 불일치

`AnalyticsRepositoryImpl.java`의 백엔드 로직과 `materials.html`의 프론트엔드 렌더링을 비교했을 때, 데이터가 **출력되지 않는** 부분은 없습니다. 모든 필드가 백엔드에서 DTO에 담겨 프론트엔드로 전달되며, 프론트엔드에서는 해당 필드를 참조하여 값을 표시합니다.

다만, 다음 두 가지 필드는 백엔드 로직상 사용자의 기대와 다를 수 있는 "근사치" 또는 "하드코딩된 값"을 표시합니다.

1.  **`Current Office Inventory Qty` (상단 요약 카드)**:
    - 백엔드에서 `0L`로 고정되어 있습니다. 만약 본사 창고 재고를 실제로 관리하고 이를 프론트엔드에 표시해야 한다면, 백엔드에 본사 창고 재고를 조회하는 로직과 테이블이 추가되어야 합니다. 현재는 항상 `0`으로 표시됩니다.
2.  **`Used Quantity` (상단 요약 카드)**:
    - 백엔드에서 `Order Volume (Qty)`와 동일한 값으로 설정됩니다. 즉, "발주량"을 "사용량"으로 근사하여 표시합니다. 실제 재료의 "소진량" 또는 "사용량"을 정확히 추적하여 표시해야 한다면, 재료 소진 로직(예: 메뉴 판매 시 재료 차감)이 백엔드에 구현되고 해당 데이터를 집계하는 쿼리가 추가되어야 합니다. 현재는 발주량이 사용량으로 간주되어 표시됩니다.

이 두 가지 사항을 제외하고는 프론트엔드에서 백엔드로부터 받은 데이터를 올바르게 표시할 것으로 예상됩니다.

---
## **식별된 문제 해결을 위한 업데이트된 계획**

"엔티티 클래스 수정 불가"라는 사용자 제약 조건과 `domain/inventory/` 엔티티 분석을 통해 얻은 새로운 통찰력을 바탕으로, 문제 필드를 해결하기 위한 다음 계획을 제안합니다.

### 1. `Current Office Inventory Qty` (요약 카드) - **수정 완료**

*   **문제**: 이전에 `0L`로 하드코딩되었습니다.
*   **근본 원인**: `AnalyticsRepositoryImpl.findMaterialsSummary()`가 본사 재고를 위해 `Inventory` 엔티티를 쿼리하지 않았습니다.
*   **해결책**: `AnalyticsRepositoryImpl.findMaterialsSummary()`를 수정하여 `inv.quantity` (Inventory 엔티티에서 가져옴)를 쿼리하고 합산하여 총 현재 본사 재고를 가져옵니다.

### 2. `Used Quantity` (요약 카드) - **수정 완료**

*   **문제**: 현재 "사용량"을 "주문량"(`ReceiveOrderDetail.count`에서 가져온 `orderVolumeQty`)으로 근사합니다.
*   **근본 원인**: `AnalyticsRepositoryImpl.findMaterialsSummary()`가 실제 재료 출고/소비 대신 `ReceiveOrderDetail.count` (매장이 주문한 재료)를 사용하고 있습니다. `InventoryOut` 및 `InventoryOutLot` 엔티티는 재료 출고를 추적하기 위해 존재합니다.
*   **해결책**: `AnalyticsRepositoryImpl.findMaterialsSummary()`를 수정하여 관련 기간(YTD) 및 재료에 대한 `InventoryOut` (또는 `InventoryOutLot`) 엔티티의 `quantity`를 합산하여 `usedQty`를 계산합니다. 이는 재고에서 나간 재료를 더 정확하게 나타냅니다.

### 3. `Profit Amount` / `Profit` 및 `Avg. Margin` / `Margin` (요약 카드 및 테이블) - **수정 완료**

*   **문제**: `StoreMaterial.sellingPrice`가 데이터베이스에 `BIGINT`로 저장되어 소수점 값이 잘리기 때문에 값이 부정확했습니다.
*   **근본 원인**: `AnalyticsRepositoryImpl.findMaterialsSummary()` 및 `findMaterials()`에서 `sellingSum` 계산 시 `StoreMaterial.sellingPrice`를 사용했습니다.
*   **해결책**: `AnalyticsRepositoryImpl.findMaterialsSummary()` 및 `findMaterials()`를 수정하여 `sellingSum` 계산 시 `UnitPrice` 엔티티의 `sellingPrice`를 사용하도록 변경했습니다. `UnitPrice.sellingPrice`는 `DECIMAL(15,3)` 타입이며, `validFrom`/`validTo` 기간을 통해 정확한 단가 이력을 반영합니다. 이로써 `BIGINT`로 인한 소수점 잘림 문제를 해결하고 정확한 매출 및 이익 계산이 가능해졌습니다.

### 4. `StoreInventoryQty` (테이블) - **근본 원인 식별 (외부)**

*   **문제**: 표시된 수량이 매장 수준의 실제 현재 재고를 정확하게 반영하지 않을 수 있습니다.
*   **근본 원인**: `StoreMaterial.quantity` 또는 `StoreInventory.quantity` 필드의 업데이트 로직은 이 프로젝트(`ict05_final_admin`)의 백엔드가 아닌 **가맹점 프로젝트의 백엔드**에 존재합니다. 이 프로젝트의 분석 쿼리는 외부에서 관리되는 데이터를 사용하고 있습니다.
*   **영향**: `StoreInventoryQty`의 정확성은 가맹점 프로젝트 백엔드의 재고 관리 로직에 전적으로 의존합니다. 만약 `StoreInventoryQty`가 부정확하다면, 해당 문제는 가맹점 프로젝트의 백엔드에서 재고 이동(입고, 출고, 조정)에 따른 `StoreMaterial.quantity` 또는 `StoreInventory.quantity` 업데이트 로직을 조사해야 합니다.
*   **해결책**: 이 프로젝트의 백엔드에서는 `StoreMaterial.quantity`를 우선하고 `StoreInventory.quantity`의 합계를 대체로 사용하는 현재 쿼리 로직을 유지합니다. 정확성 문제는 가맹점 프로젝트 백엔드에서 해결되어야 합니다.