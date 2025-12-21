package com.english.api.order.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.mail.service.MailService;
import com.english.api.order.dto.response.InvoiceResponse;
import com.english.api.order.mapper.OrderMapper;
import com.english.api.order.model.Invoice;
import com.english.api.order.model.Order;
import com.english.api.order.model.Payment;
import com.english.api.order.repository.InvoiceRepository;
import com.english.api.order.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final S3AsyncClient s3Client;
    private final MailService mailService;
    private final SpringTemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    private final OrderMapper orderMapper;

    @Value("${cloud.public-url}")
    private String publicUrl;

    @Value("${cloud.bucket}")
    private String bucket;

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(VIETNAM_ZONE);
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(VIETNAM_ZONE);
    private static final NumberFormat VND_FORMAT = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));

    @Async
    @Transactional
    @Override
    public void generateAndSendInvoiceAsync(Order order, Payment payment) {
        try {
            // Generate invoice number
            String invoiceNumber = generateInvoiceNumber(order);

            // Generate PDF
            byte[] pdfBytes = generateInvoicePdf(order, payment, invoiceNumber);

            // Upload to S3
            String filename = invoiceNumber + ".pdf";
            String key = "invoices/" + filename;
            
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putRequest, AsyncRequestBody.fromBytes(pdfBytes)).join();

            String fileUrl = String.join("/", publicUrl.replaceAll("/+$", ""), key);

            // Save invoice record
            Map<String, Object> invoiceData = buildInvoiceData(order, payment, invoiceNumber);
            Invoice invoice = Invoice.builder()
                    .order(order)
                    .number(invoiceNumber)
                    .totalCents(order.getTotalCents())
                    .currency(order.getCurrency())
                    .data(objectMapper.writeValueAsString(invoiceData))
                    .fileUrl(fileUrl)
                    .build();
            invoiceRepository.save(invoice);

            // Send email with invoice attachment
            mailService.sendInvoiceEmail(
                    order.getUser().getEmail(),
                    order,
                    payment,
                    invoice
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generateInvoiceNumber(Order order) {
        String timestamp = order.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "INV" + timestamp + order.getId().toString().substring(0, 8).toUpperCase();
    }

    private byte[] generateInvoicePdf(Order order, Payment payment, String invoiceNumber) throws Exception {
        // Prepare context with data
        Context context = new Context();
        context.setVariable("invoiceNumber", invoiceNumber);
        context.setVariable("createdDate", formatDate(order.getCreatedAt()));
        context.setVariable("paidDate", formatDate(order.getPaidAt()));
        
        // Customer info
        context.setVariable("customerName", order.getUser().getFullName());
        context.setVariable("customerEmail", order.getUser().getEmail());
        context.setVariable("customerId", order.getUser().getId().toString());
        
        // Order info
        context.setVariable("orderId", order.getId().toString());
        context.setVariable("orderItems", order.getItems());
        
        // Payment info
        context.setVariable("paymentMethod", payment.getProvider().name());
        context.setVariable("paymentTxn", payment.getProviderTxn());
        context.setVariable("paymentTime", formatDateTime(payment.getConfirmedAt()));
        
        // Amount formatting
        Long subtotalCents = order.getTotalCents() + (order.getDiscountCents() != null ? order.getDiscountCents() : 0L);
        context.setVariable("currency", order.getCurrency().name());
        context.setVariable("subtotalAmount", formatCurrency(subtotalCents));
        context.setVariable("discountAmount", order.getDiscountCents() != null && order.getDiscountCents() > 0 ? 
                formatCurrency(order.getDiscountCents()) : null);
        context.setVariable("voucherCode", order.getVoucherCode());
        context.setVariable("totalAmount", formatCurrency(order.getTotalCents()));
        context.setVariable("totalAmountCents", order.getTotalCents());
        
        // Process template to HTML
        String htmlContent = templateEngine.process("invoice", context);

        // Convert HTML to PDF using OpenHTMLtoPDF with PDFBox
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            
            // Load fonts for Vietnamese character support
            try {
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans.ttf"), 
                        "DejaVu Sans", 400, 
                        com.openhtmltopdf.pdfboxout.PdfRendererBuilder.FontStyle.NORMAL, true);
                
                builder.useFont(() -> getClass().getResourceAsStream("/fonts/DejaVuSans-Bold.ttf"), 
                        "DejaVu Sans", 700, 
                        com.openhtmltopdf.pdfboxout.PdfRendererBuilder.FontStyle.NORMAL, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            builder.withHtmlContent(htmlContent, null);
            builder.toStream(os);
            builder.run();
            
            return os.toByteArray();
        }
    }

    private Map<String, Object> buildInvoiceData(Order order, Payment payment, String invoiceNumber) {
        Map<String, Object> data = new HashMap<>();
        data.put("invoiceNumber", invoiceNumber);
        data.put("orderId", order.getId().toString());
        data.put("paymentId", payment.getId().toString());
        data.put("customerEmail", order.getUser().getEmail());
        data.put("totalCents", order.getTotalCents());
        data.put("currency", order.getCurrency().name());
        data.put("paymentProvider", payment.getProvider().name());
        return data;
    }

    private String formatCurrency(Long cents) {
        return VND_FORMAT.format(cents) + "đ";
    }

    private String formatDate(java.time.OffsetDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.atZoneSameInstant(VIETNAM_ZONE).format(DATE_FORMATTER);
    }

    private String formatDateTime(java.time.OffsetDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.atZoneSameInstant(VIETNAM_ZONE).format(DATETIME_FORMATTER);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceByOrderId(UUID orderId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        Invoice invoice = invoiceRepository.findByOrderIdAndUserId(orderId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for order ID: " + orderId + " or access denied"));
        return orderMapper.toInvoiceResponse(invoice);
    }
}
