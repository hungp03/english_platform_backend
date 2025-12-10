package com.english.api.mail.service.impl;

import com.english.api.mail.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
            log.error("Failed to send email: {}", e.getMessage(), e);
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
        content.append("Xin chào ").append(userName).append(",\n\n");
        
        if (isApproved) {
            content.append("Chúc mừng! Yêu cầu trở thành giảng viên của bạn đã được PHÊ DUYỆT.\n\n");
            content.append("Bạn hiện đã có quyền giảng viên trên nền tảng English Pro.\n");
        } else {
            content.append("Chúng tôi rất tiếc phải thông báo rằng yêu cầu trở thành giảng viên của bạn đã bị TỪ CHỐI.\n\n");
        }
        
        if (adminNotes != null && !adminNotes.isEmpty()) {
            content.append("\nGhi chú từ quản trị viên:\n");
            content.append(adminNotes).append("\n");
        }
        
        content.append("\nTrân trọng,\n");
        content.append("Đội ngũ English Pro");
        
        String subject = isApproved 
            ? "Yêu cầu giảng viên đã được phê duyệt - English Pro" 
            : "Yêu cầu giảng viên bị từ chối - English Pro";
            
        this.sendEmail(email, subject, content.toString(), false, false);
    }

    @Async
    @Override
    public void sendInvoiceEmail(String email, Object order, Object payment, Object invoice) {
        Context context = new Context();
        context.setVariable("order", order);
        context.setVariable("payment", payment);
        context.setVariable("invoice", invoice);
        String content = this.templateEngine.process("invoice-email", context);
        this.sendEmail(email, "Hóa đơn thanh toán - English Pro", content, false, true);
    }

    @Async
    @Override
    public void sendInstructorRoleRevokedEmail(String email, String userName, String reason) {
        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(userName).append(",\n\n");
        content.append("Chúng tôi xin thông báo rằng quyền giảng viên của bạn trên nền tảng English Pro đã bị thu hồi.\n\n");
        content.append("Lý do thu hồi:\n");
        content.append(reason).append("\n\n");
        content.append("Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với đội ngũ quản trị viên của chúng tôi.\n\n");
        content.append("Trân trọng,\n");
        content.append("Đội ngũ English Pro");
        
        this.sendEmail(email, "Thông báo thu hồi quyền giảng viên - English Pro", content.toString(), false, false);
    }

    @Async
    @Override
    public void sendInstructorRoleRestoredEmail(String email, String userName, String reason) {
        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(userName).append(",\n\n");
        content.append("Chúng tôi xin thông báo rằng quyền giảng viên của bạn trên nền tảng English Pro đã được khôi phục.\n\n");
        content.append("Lý do khôi phục:\n");
        content.append(reason).append("\n\n");
        content.append("Bạn có thể tiếp tục tạo và quản lý các khóa học của mình trên nền tảng.\n\n");
        content.append("Cảm ơn bạn đã tiếp tục đồng hành cùng chúng tôi.\n\n");
        content.append("Trân trọng,\n");
        content.append("Đội ngũ English Pro");
        
        this.sendEmail(email, "Thông báo khôi phục quyền giảng viên - English Pro", content.toString(), false, false);
    }

    @Async
    @Override
    public void sendAccountLockedEmail(String email, String userName, String reason) {
        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(userName).append(",\n\n");
        content.append("Chúng tôi xin thông báo rằng tài khoản của bạn trên nền tảng English Pro đã bị khóa.\n\n");
        content.append("Lý do khóa tài khoản:\n");
        content.append(reason).append("\n\n");
        content.append("Nếu bạn có bất kỳ thắc mắc nào, vui lòng liên hệ với đội ngũ quản trị viên của chúng tôi.\n\n");
        content.append("Trân trọng,\n");
        content.append("Đội ngũ English Pro");
        
        this.sendEmail(email, "Thông báo khóa tài khoản - English Pro", content.toString(), false, false);
    }

    @Async
    @Override
    public void sendAccountUnlockedEmail(String email, String userName, String reason) {
        StringBuilder content = new StringBuilder();
        content.append("Xin chào ").append(userName).append(",\n\n");
        content.append("Chúng tôi xin thông báo rằng tài khoản của bạn trên nền tảng English Pro đã được mở khóa.\n\n");
        content.append("Lý do mở khóa:\n");
        content.append(reason).append("\n\n");
        content.append("Bạn có thể tiếp tục sử dụng các dịch vụ trên nền tảng của chúng tôi.\n\n");
        content.append("Cảm ơn bạn đã tiếp tục đồng hành cùng chúng tôi.\n\n");
        content.append("Trân trọng,\n");
        content.append("Đội ngũ English Pro");
        
        this.sendEmail(email, "Thông báo mở khóa tài khoản - English Pro", content.toString(), false, false);
    }

}
