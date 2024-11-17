package com.happy.observator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmailWithHtml(String to, String subject, String htmlContent) throws Exception {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom("observator@gmail.com");
    
            mailSender.send(message);
            logger.info("업비트 자산 정보가 {}로 성공적으로 전송되었습니다!", to);
        } catch (Exception e) {
            logger.error("업비트 자산 정보를 {}로 전송하는 도중 오류 발생: {}", to, e.getMessage(), e);
            throw e;
        }
    }    
}
