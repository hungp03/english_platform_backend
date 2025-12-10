package com.english.api.mail.service;

/**
 * Created by hungpham on 9/24/2025
 */
public interface MailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendRegisterVerificationEmail(String email, String token, String templateName);
    void sendForgotPasswordEmail(String email, String otp, String templateName);
    void sendPaymentSuccessEmail(String email, Object order, Object payment, String templateName);
    void sendInstructorRequestReviewEmail(String email, String userName, boolean isApproved, String adminNotes);
    void sendInvoiceEmail(String email, Object order, Object payment, Object invoice);
    void sendInstructorRoleRevokedEmail(String email, String userName, String reason);
    void sendInstructorRoleRestoredEmail(String email, String userName, String reason);
    void sendAccountLockedEmail(String email, String userName, String reason);
    void sendAccountUnlockedEmail(String email, String userName, String reason);
}
