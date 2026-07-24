package com.smartepm.service.impl;

import com.smartepm.exception.BadRequestException;
import com.smartepm.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Smart EPM — Your Password Reset OTP");
            helper.setText(
                    "Hello,\n\n" +
                    "You requested to reset your password for Smart Employee & Project Management System.\n\n" +
                    "Your OTP is: " + otp + "\n\n" +
                    "This OTP is valid for 5 minutes. If you did not request this, you can safely ignore this email.\n\n" +
                    "— Smart EPM Team",
                    false
            );

            mailSender.send(message);
            logger.info("OTP email sent to {}", toEmail);
        } catch (MessagingException | MailException ex) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
            throw new BadRequestException("Failed to send OTP email. Please check your email configuration and try again.");
        }
    }
}
