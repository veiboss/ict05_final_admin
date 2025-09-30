package com.boot.ict05_final_admin.domain.notice.service;

import com.boot.ict05_final_admin.domain.notice.entity.NoticeAttachment;
import com.boot.ict05_final_admin.domain.notice.repository.NoticeAttachmentRepository;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class NoticeAttachmentService {

    private static final String SFTP_HOST = "ict05.wwwbiz.kr";
    private static final int SFTP_PORT = 22;
    private static final String SFTP_USER = "ict05";
    private static final String SFTP_PASSWORD = "admin*1472";
    private static final String SFTP_BASE_DIR = "/httpdocs/"; // 서버 내 업로드 경로
    private static final String URL_BASE = "http://ict05.wwwbiz.kr";
    private final NoticeAttachmentRepository noticeAttachmentRepository;

    public NoticeAttachmentService(NoticeAttachmentRepository noticeAttachmentRepository) {
        this.noticeAttachmentRepository = noticeAttachmentRepository;
    }

    public String uploadFile(MultipartFile file) throws Exception {
        Session session = null;
        Channel channel = null;
        ChannelSftp channelSftp = null;

        try {
            // JSCH 세션 생성
            JSch jsch = new JSch();
            session = jsch.getSession(SFTP_USER, SFTP_HOST, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no"); // 키 체크 비활성화
            session.setConfig(config);
            session.connect();

            // SFTP 채널 열기
            channel = session.openChannel("sftp");
            channel.connect();
            channelSftp = (ChannelSftp) channel;

            // 서버 디렉토리 이동 (없으면 생성)
            try {
                channelSftp.cd(SFTP_BASE_DIR);
            } catch (Exception e) {
                channelSftp.mkdir(SFTP_BASE_DIR);
                channelSftp.cd(SFTP_BASE_DIR);
            }

            // 파일 이름 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + ext;

            // 파일 업로드
            InputStream inputStream = file.getInputStream();
            channelSftp.put(inputStream, newFileName);
            inputStream.close();

            // 업로드된 URL 반환
            return URL_BASE + "/" + newFileName;

        } finally {
            if (channelSftp != null) channelSftp.exit();
            if (channel != null) channel.disconnect();
            if (session != null) session.disconnect();
        }
    }


    public List<NoticeAttachment> findByNoticeId(Long noticeId) {

        List<NoticeAttachment> lists = noticeAttachmentRepository.findByNoticeId(noticeId);

        return lists;
    }
}
