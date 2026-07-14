package com.lms.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.lms.payment.RazorpayConfigResolver;
import com.lms.settings.RazorpayConfig;
import com.lms.shared.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

/**
 * Ref: SRS 10.12, 16.9 - real HTTP calls to Razorpay's Orders API.
 * Credentials are resolved per call (RazorpayConfigResolver: administrator
 * settings first, environment variables as fallback) rather than captured
 * at construction, so an admin saving new keys via PATCH
 * /settings/payment-gateway takes effect on the next order without a
 * restart. This project has no live Razorpay credentials, so this path is
 * implemented correctly but untested end-to-end - swap in real test-mode
 * keys to exercise it live.
 */
@Service
public class RazorpayApiClient implements RazorpayClient {

    private static final Logger log = LoggerFactory.getLogger(RazorpayApiClient.class);
    private static final String BASE_URL = "https://api.razorpay.com/v1";

    private final RestClient restClient;
    private final RazorpayConfigResolver configResolver;

    public RazorpayApiClient(RazorpayConfigResolver configResolver) {
        this.configResolver = configResolver;
        this.restClient = RestClient.builder().baseUrl(BASE_URL).build();
    }

    @Override
    public RazorpayOrder createOrder(long amountInPaise, String currency, String receipt) {
        RazorpayConfig config = configResolver.resolve();
        if (!config.isConfigured()) {
            throw new ServiceUnavailableException("Razorpay is not configured for this environment.");
        }

        String credentials = config.keyId() + ":" + config.keySecret();
        String basicAuth = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        try {
            JsonNode response = restClient.post()
                    .uri("/orders")
                    .header("Authorization", basicAuth)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("amount", amountInPaise, "currency", currency, "receipt", receipt))
                    .retrieve()
                    .body(JsonNode.class);

            return new RazorpayOrder(
                    response.get("id").asText(),
                    response.get("amount").asLong(),
                    response.get("currency").asText()
            );
        } catch (RestClientException e) {
            log.error("Razorpay order creation failed", e);
            throw new ServiceUnavailableException("Unable to reach Razorpay. Please try again later.");
        }
    }
}
