package com.blood.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);

            helper.setText(body, true);

            mailSender.send(message);
            log.info("Đã gửi email thành công tới: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Gửi email thất bại tới {}. Chi tiết lỗi: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendEmailWithQrCode(String to, String subject, String htmlBody, String base64QrCode, String contentId) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            byte[] imageBytes = Base64.getDecoder().decode(base64QrCode);
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes);

            helper.addInline(contentId, imageResource, "image/png");

            mailSender.send(message);
        } catch (Exception e) {
            log.error("Lỗi gửi email kèm QR Code: {}", e.getMessage());
        }
    }
}
