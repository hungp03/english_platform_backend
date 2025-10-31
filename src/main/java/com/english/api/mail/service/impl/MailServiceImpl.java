package com.english.api.mail.service.impl;

import com.english.api.mail.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import java.nio.charset.StandardCharsets;

/**
 * Created by hungpham on 9/24/2025
 */
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.register-verification-link}")
    private String registerVerificationLink;
    @Override
    public void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml) {
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage,
                    isMultipart, StandardCharsets.UTF_8.name());
            message.setFrom("noreplymail@english.pro");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content, isHtml);
            this.javaMailSender.send(mimeMessage);
        } catch (MailException | MessagingException e) {
            System.out.println(e.getMessage());
        }
    }

    @Async
    @Override
    public void sendRegisterVerificationEmail(String email, String token, String templateName) {
        String newVerificationLink = registerVerificationLink + token;
        Context context = new Context();
        context.setVariable("email", email);
        context.setVariable("verificationLink", newVerificationLink);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmail(email, "Register Account at English Pro", content, false, true);
    }

    @Async
    @Override
    public void sendForgotPasswordEmail(String email, String otp, String templateName) {
        Context context = new Context();
        context.setVariable("EMAIL", email);
        context.setVariable("OTP", otp);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmail(email, "Forgot password - English Pro", content, false, true);
    }

    @Async
    @Override
    public void sendPaymentSuccessEmail(String email, Object order, Object payment, String templateName) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("payment", payment);
        String content = this.templateEngine.process(templateName, context);
        this.sendEmail(email, "Thanh toán thành công - English Pro", content, false, true);
    }

    @Async
    @Override
    public void sendInstructorRequestReviewEmail(String email, String userName, boolean isApproved, String adminNotes) {
        StringBuilder content = new StringBuilder();
        content.append("Dear ").append(userName).append(",\n\n");
        
        if (isApproved) {
            content.append("Congratulations! Your instructor request has been APPROVED.\n\n");
            content.append("You now have instructor privileges on English Pro platform.\n");
        } else {
            content.append("We regret to inform you that your instructor request has been REJECTED.\n\n");
        }
        
        if (adminNotes != null && !adminNotes.isEmpty()) {
            content.append("\nAdmin Notes:\n");
            content.append(adminNotes).append("\n");
        }
        
        content.append("\nBest regards,\n");
        content.append("English Pro Team");
        
        String subject = isApproved 
            ? "Instructor Request Approved - English Pro" 
            : "Instructor Request Rejected - English Pro";
            
        this.sendEmail(email, subject, content.toString(), false, false);
    }

}
