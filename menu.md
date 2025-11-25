# 메뉴 관리 기능 명세서 (기술 포함)

## 1. 개요

본사 관리자가 메뉴를 관리(등록, 조회, 수정)하는 기능입니다. 이 문서는 각 기능에 대한 화면 명세와 함께, 해당 기능을 처리하는 서버의 Controller, Service, DAO(Repository) 계층의 주요 메서드명을 명시합니다.

---

## 2. 기능별 기술 명세

### 2.1. 메뉴 목록 조회

- **화면 URL**: `/menu/list`
- **설명**: 등록된 모든 메뉴를 페이지네이션을 통해 표 형태로 조회합니다. 검색, 카테고리 필터, 판매 상태 필터, 페이지 크기 조절 기능을 제공합니다.

| 계층 | 클래스 | 메서드 | 설명 |
|---|---|---|---|
| **Controller** | `MenuController` | `listStoreMenu(...)` | - HTTP GET 요청을 받아 메뉴 목록 페이지를 렌더링합니다.<br>- 검색/필터 조건을 `MenuSearchDTO`로 받고, 페이징 정보를 `Pageable`로 처리합니다.<br>- `MenuService`를 호출하여 데이터를 조회하고, 그 결과를 Model에 담아 View로 전달합니다. |
| **Service** | `MenuService` | `selectAllStoreMenu(MenuSearchDTO, Pageable)` | - Controller로부터 전달받은 조건으로 `MenuRepository`를 호출하여 메뉴 목록을 조회합니다. |
| **DAO(Repository)** | `MenuRepositoryImpl` | `listMenu(MenuSearchDTO, pageable)` | - QueryDSL을 사용하여 동적 쿼리를 생성합니다.<br>- 검색어(메뉴명), 카테고리 ID, 판매 상태(SHOW/HIDE)에 따라 조건부 검색을 수행하고, 페이징 처리된 `Page<MenuListDTO>`를 반환합니다. |

### 2.2. 메뉴 등록

- **화면 URL**: `/menu/write` (등록 폼), `POST /menu/write` (저장)
- **설명**: 새로운 메뉴의 정보와 레시피를 입력받아 시스템에 저장합니다.

| 계층 | 클래스 | 메서드 | 설명 |
|---|---|---|---|
| **Controller** | `MenuController` | `writeForm(Model)` | - **[폼 로딩]** `GET /menu/write`<br>- 메뉴 등록 폼 페이지를 렌더링합니다.<br>- 카테고리, 재료 옵션, 단위 등의 초기 데이터를 Model에 담아 전달합니다. |
| | `MenuController` | `submitMenuWrite(...)` | - **[저장]** `POST /menu/write`<br>- `MenuWriteFormDTO`로 폼 데이터를 받아 유효성을 검증합니다.<br>- `MenuService.insertStoreMenu`를 호출하여 메뉴를 저장하고, 성공 시 상세 페이지로 리다이렉트합니다. |
| **Service** | `MenuService` | `insertStoreMenu(MenuWriteFormDTO)` | - DTO를 `Menu` 엔티티로 변환하여 저장합니다.<br>- `saveRecipes`를 호출하여 주재료 및 소스 레시피를 `MenuRecipe` 테이블에 저장합니다.<br>- **`createStoreMenusForNewMenu`를 호출하여 모든 가맹점에 신규 메뉴를 '판매중' 상태로 자동 등록합니다.** |
| **DAO(Repository)** | `MenuCategoryRepository` | `findById(Long)` | - DTO에 포함된 카테고리 ID로 `MenuCategory` 엔티티를 조회합니다. |
| | `MenuRepository` | `save(Menu)` | - `Menu` 엔티티를 DB에 저장합니다. |
| | `MenuRecipeRepository` | `save(MenuRecipe)` | - 레시피 항목들을 DB에 저장합니다. |
| | `StoreRepository` | `findAll()` | - `StoreMenu` 생성을 위해 모든 `Store` 엔티티를 조회합니다. |
| | `StoreMenuRepository`| `save(StoreMenu)` | - 각 가맹점별로 신규 메뉴의 판매 상태를 DB에 저장합니다. |

### 2.3. 메뉴 상세 조회

- **화면 URL**: `/menu/detail/{menuId}`
- **설명**: 특정 메뉴의 모든 상세 정보를 조회합니다.

| 계층 | 클래스 | 메서드 | 설명 |
|---|---|---|---|
| **Controller** | `MenuController` | `detailStoreMenu(Long, Model)` | - PathVariable로 `menuId`를 받아 `MenuService`를 호출합니다.<br>- 조회된 `MenuDetailDTO`를 Model에 담아 상세 페이지를 렌더링합니다. |
| **Service** | `MenuService` | `MenuDetail(Long)` | - `menuId`로 `Menu` 엔티티와 연관된 `MenuRecipe` 목록을 조회합니다.<br>- 조회된 데이터를 `MenuDetailDTO`로 변환하여 반환합니다. |
| **DAO(Repository)** | `MenuRepository` | `findById(Long)` | - `menuId`를 기준으로 `Menu` 엔티티를 조회합니다. |

### 2.4. 메뉴 수정

- **화면 URL**: `/menu/modify/{menuId}` (수정 폼), `POST /API/menu/modify/{menuId}` (저장)
- **설명**: 기존 메뉴의 정보와 레시피를 수정합니다.

| 계층 | 클래스 | 메서드 | 설명 |
|---|---|---|---|
| **Controller** | `MenuController` | `modifyStoreMenu(Long, Model)` | - **[폼 로딩]** `GET /menu/modify/{menuId}`<br>- `MenuService.MenuDetail`을 호출하여 기존 메뉴 정보를 조회합니다.<br>- 조회된 정보를 `MenuModifyFormDTO`로 변환하여 Model에 담고, 수정 폼을 렌더링합니다. |
| | `MenuRestController` | `modifyMenu(...)` | - **[저장]** `POST /API/menu/modify/{menuId}`<br>- `MenuModifyFormDTO`로 폼 데이터를 받아 유효성을 검증합니다.<br>- `MenuService.menuModify`를 호출하여 수정을 처리하고, 성공/실패 결과를 JSON으로 반환합니다. |
| **Service** | `MenuService` | `menuModify(MenuModifyFormDTO)` | - `menuId`로 `Menu` 엔티티를 조회하여 기본 정보를 업데이트합니다.<br>- **`menuRecipeRepository.deleteAllByMenu`를 호출하여 기존 레시피를 모두 삭제합니다.**<br>- 폼에서 제출된 새로운 레시피 정보를 `MenuRecipe` 테이블에 다시 저장합니다. |
| **DAO(Repository)** | `MenuRepository` | `findById(Long)` | - 수정할 `Menu` 엔티티를 조회합니다. |
| | `MenuRecipeRepository` | `deleteAllByMenu(Menu)` | - 특정 메뉴에 연결된 모든 `MenuRecipe` 레코드를 삭제합니다. |
| | `MaterialRepository` | `findById(Long)` | - 레시피에 사용될 `Material` 엔티티를 조회합니다. |
| | `MenuRecipeRepository` | `save(MenuRecipe)` | - 수정된 레시피 항목들을 DB에 새로 저장합니다. |

---

## 3. 데이터 모델 (주요 엔티티)

- **`Menu`**: 메뉴의 핵심 정보를 담는 마스터 엔티티.
- **`MenuRecipe`**: 메뉴와 재료의 관계를 정의하는 레시피 엔티티 (역할: `MAIN`, `SAUCE`).
- **`MenuCategory`**: 메뉴의 분류를 나타내는 엔티티.
- **`StoreMenu`**: 특정 가맹점(`Store`)의 특정 메뉴(`Menu`)에 대한 판매 상태(`StoreMenuSoldout`)를 관리하는 엔티티.