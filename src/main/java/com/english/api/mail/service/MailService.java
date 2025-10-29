package com.english.api.mail.service;

/**
 * Created by hungpham on 9/24/2025
 */
public interface MailService {
    void sendEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml);
    void sendRegisterVerificationEmail(String email, String token, String templateName);
    void sendForgotPasswordEmail(String email, String otp, String templateName);
    void sendPaymentSuccessEmail(String email, Object order, Object payment, String templateName);
}
