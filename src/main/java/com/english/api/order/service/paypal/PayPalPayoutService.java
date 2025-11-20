package com.english.api.order.service.paypal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayPalPayoutService {
    
    private final PayPalClient payPalClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Create a payout to send money to instructor's PayPal account
     * 
     * @param paypalEmail Recipient's PayPal email
     * @param amountUSD Amount in USD
     * @param senderItemId Unique identifier for this payout (withdrawal request ID)
     * @param note Note for the recipient
     * @return PayPal payout batch ID and item ID
     */
    public PayoutResult createPayout(String paypalEmail, BigDecimal amountUSD, String senderItemId, String note) {
        String accessToken = payPalClient.getAccessToken();
        
        ObjectNode payload = objectMapper.createObjectNode();
        
        // Sender batch header
        ObjectNode senderBatchHeader = payload.putObject("sender_batch_header");
        senderBatchHeader.put("sender_batch_id", "batch_" + senderItemId);
        senderBatchHeader.put("email_subject", "You have a payout from English Platform");
        senderBatchHeader.put("email_message", "You have received a payout. Thank you for using our service!");
        
        // Items array
        ArrayNode items = payload.putArray("items");
        ObjectNode item = items.addObject();
        item.put("recipient_type", "EMAIL");
        item.put("amount", objectMapper.createObjectNode()
                .put("value", amountUSD.setScale(2, RoundingMode.DOWN).toPlainString())
                .put("currency", "USD"));
        item.put("receiver", paypalEmail);
        item.put("note", note != null ? note : "Instructor withdrawal");
        item.put("sender_item_id", senderItemId);
        
        try {
            JsonNode response = payPalClient.post("/v1/payments/payouts", payload);
            
            String batchId = response.path("batch_header").path("payout_batch_id").asText(null);
            String batchStatus = response.path("batch_header").path("batch_status").asText(null);
            
            // Get the first item's payout_item_id
            String payoutItemId = null;
            if (response.has("links") && response.get("links").isArray()) {
                for (JsonNode link : response.get("links")) {
                    if ("item".equals(link.path("rel").asText())) {
                        String href = link.path("href").asText();
                        // Extract item ID from URL
                        String[] parts = href.split("/");
                        if (parts.length > 0) {
                            payoutItemId = parts[parts.length - 1];
                        }
                    }
                }
            }
            
            log.info("PayPal payout created: batchId={}, status={}, itemId={}", batchId, batchStatus, payoutItemId);
            
            return new PayoutResult(batchId, payoutItemId, batchStatus, response);
            
        } catch (Exception e) {
            log.error("Failed to create PayPal payout", e);
            throw new RuntimeException("Failed to create PayPal payout: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check the status of a payout batch
     */
    public JsonNode getPayoutBatchStatus(String batchId) {
        return payPalClient.get("/v1/payments/payouts/" + batchId);
    }
    
    /**
     * Check the status of a specific payout item
     */
    public JsonNode getPayoutItemStatus(String payoutItemId) {
        return payPalClient.get("/v1/payments/payouts-item/" + payoutItemId);
    }
    
    public record PayoutResult(
            String batchId,
            String payoutItemId,
            String status,
            JsonNode rawResponse
    ) {}
}
