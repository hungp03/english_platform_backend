package com.english.api.order.service.paypal;

import com.english.api.order.config.PayPalProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class PayPalClient {

    private final PayPalProperties properties;

    public JsonNode createOrder(ObjectNode payload) {
        return executeWithBearer(token -> restClient().post()
                .uri("/v2/checkout/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(payload)
                .retrieve()
                .body(JsonNode.class));
    }

    public JsonNode captureOrder(String paypalOrderId) {
        return executeWithBearer(token -> restClient().post()
                .uri("/v2/checkout/orders/{orderId}/capture", paypalOrderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(JsonNode.class));
    }

    public JsonNode post(String uri, ObjectNode payload) {
        return executeWithBearer(token -> restClient().post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .body(payload)
                .retrieve()
                .body(JsonNode.class));
    }

    public JsonNode get(String uri) {
        return executeWithBearer(token -> restClient().get()
                .uri(uri)
                .header(HttpHeaders.AUTHORIZATION, token)
                .retrieve()
                .body(JsonNode.class));
    }

    public String getAccessToken() {
        return accessToken();
    }

    private <T> T executeWithBearer(RestCall<T> call) {
        String token = "Bearer " + accessToken();
        try {
            return call.apply(token);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException("PayPal API call failed: " + ex.getResponseBodyAsString(), ex);
        }
    }

    private String accessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        try {
            PayPalAccessTokenResponse response = restClient().post()
                    .uri("/v1/oauth2/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .headers(headers -> headers.setBasicAuth(properties.getClientId(), properties.getClientSecret()))
                    .body(form)
                    .retrieve()
                    .body(PayPalAccessTokenResponse.class);
            if (response == null || response.accessToken() == null) {
                throw new RuntimeException("Failed to retrieve PayPal access token");
            }
            return response.accessToken();
        } catch (RestClientResponseException ex) {
            throw new RuntimeException("PayPal token request failed: " + ex.getResponseBodyAsString(), ex);
        }
    }

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();
    }

    @FunctionalInterface
    private interface RestCall<T> {
        T apply(String bearerToken);
    }
}
