package com.boot.ict05_final_admin.domain.notice.dto;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeCategory;
import com.boot.ict05_final_admin.domain.notice.entity.NoticePriority;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 공지사항 수정 폼 DTO
 *
 * <p>공지사항 수정 시 클라이언트에서 전달되는 데이터 전송 객체(DTO)이다.
 * 기존 공지사항 ID, 카테고리, 우선순위, 제목, 내용, 작성자 및 첨부파일 리스트를 포함한다.</p>
 *
 * <p>주요 필드:</p>
 * <ul>
 *     <li>id: 수정할 공지사항의 고유 ID</li>
 *     <li>noticeCategory: 공지사항 카테고리</li>
 *     <li>noticePriority: 공지사항 우선순위</li>
 *     <li>isShow: 공지사항 공개 여부</li>
 *     <li>title: 공지사항 제목</li>
 *     <li>body: 공지사항 내용</li>
 *     <li>writer: 작성자 이름</li>
 *     <li>files: 새로 첨부할 파일 리스트 (없을 수 있음)</li>
 * </ul>
 */
@Data
public class NoticeModifyFormDTO {

    /** 수정할 공지사항의 고유 ID */
    private Long id;

    /** 공지사항 카테고리 */
    private NoticeCategory noticeCategory;

    /** 공지사항 우선순위 */
    private NoticePriority noticePriority;

    /** 공지사항 공개 여부 */
    private Boolean isShow;

    /** 공지사항 제목 */
    private String title;

    /** 공지사항 내용 */
    private String body;

    /** 작성자 이름 */
    private String writer;

    /** 새로 첨부할 파일 리스트 */
    private List<MultipartFile> files;
}
