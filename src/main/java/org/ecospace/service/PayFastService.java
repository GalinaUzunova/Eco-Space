package org.ecospace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@Service
public class PayFastService {

    @Value("${payfast.merchant.id}")
    private String merchantId;

    @Value("${payfast.merchant.key}")
    private String merchantKey;

    @Value("${payfast.merchant.passphrase}")
    private String passphrase;

    @Value("${payfast.return.url}")
    private String returnUrl;

    @Value("${payfast.cancel.url}")
    private String cancelUrl;

    @Value("${payfast.notify.url}")
    private String notifyUrl;

    @Value("${payfast.test-mode:true}")
    private boolean testMode;

    public String createPayment(BigDecimal amount, String itemName, String merchantOrderId,
                                String customerEmail, String firstName, String lastName) {
        try {
            System.out.println("=== PAYFAST PAYMENT CREATION ===");
            System.out.println("Merchant ID: " + merchantId);
            System.out.println("Merchant Key: " + merchantKey);
            System.out.println("Passphrase: " + passphrase);

            // Create parameters in alphabetical order
            Map<String, String> parameters = new TreeMap<>();
            parameters.put("merchant_id", merchantId.trim());
            parameters.put("merchant_key", merchantKey.trim());
            parameters.put("return_url", returnUrl);
            parameters.put("cancel_url", cancelUrl);
            parameters.put("notify_url", notifyUrl);
            parameters.put("name_first", firstName);
            parameters.put("name_last", lastName.isEmpty() ? "User" : lastName);
            parameters.put("email_address", "sbtu01@payfast.co.za");
            parameters.put("m_payment_id", merchantOrderId);
            parameters.put("amount", String.format(Locale.ENGLISH, "%.2f", amount.doubleValue()));
            parameters.put("item_name", itemName);

            System.out.println("=== PARAMETERS ===");
            parameters.forEach((key, value) -> System.out.println(key + ": " + value));

            // Generate signature
            String signature = generateSignature(parameters);
            System.out.println("Generated signature: " + signature);

            parameters.put("signature", signature);

            String payfastUrl = buildPayFastUrl(parameters);
            System.out.println("Final PayFast URL: " + payfastUrl);

            return payfastUrl;

        } catch (Exception e) {
            System.out.println("Error in createPayment: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Payment creation failed", e);
        }
    }

    private String generateSignature(Map<String, String> parameters) {
        try {
            parameters.remove("signature");

            // Build parameter string in alphabetical order
            StringBuilder paramString = new StringBuilder();
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                    if (paramString.length() > 0) {
                        paramString.append("&");
                    }
                    paramString.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }

            // Add passphrase to the parameter string
            String stringToHash = paramString.toString();
            if (passphrase != null && !passphrase.trim().isEmpty()) {
                stringToHash += "&passphrase=" + passphrase.trim();
            }

            System.out.println("=== SIGNATURE DEBUG ===");
            System.out.println("String to hash: " + stringToHash);
            System.out.println("Passphrase included: " + (passphrase != null && !passphrase.trim().isEmpty()));

            // Generate MD5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(stringToHash.getBytes(StandardCharsets.UTF_8));

            // Convert to hex (lowercase)
            StringBuilder signature = new StringBuilder();
            for (byte b : digest) {
                signature.append(String.format("%02x", b & 0xff));
            }

            System.out.println("Final signature: " + signature);
            System.out.println("=== END SIGNATURE DEBUG ===");

            return signature.toString();

        } catch (Exception e) {
            System.out.println("Error generating signature: " + e.getMessage());
            throw new RuntimeException("Error generating signature", e);
        }
    }

    private String buildPayFastUrl(Map<String, String> parameters) {
        String baseUrl = testMode ?
                "https://sandbox.payfast.co.za/eng/process?" :
                "https://www.payfast.co.za/eng/process?";

        StringBuilder url = new StringBuilder(baseUrl);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            try {
                url.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .append("&");
            } catch (Exception e) {
                throw new RuntimeException("URL encoding failed", e);
            }
        }

        // Remove trailing &
        if (url.length() > 0) {
            url.setLength(url.length() - 1);
        }

        return url.toString();
    }

    public boolean verifyPayment(Map<String, String> payFastData) {
        try {
            System.out.println("=== PAYMENT VERIFICATION ===");

            Map<String, String> verifyData = new HashMap<>(payFastData);
            String receivedSignature = verifyData.remove("signature");

            if (receivedSignature == null) {
                System.out.println("ERROR: No signature in response");
                return false;
            }

            System.out.println("Received signature: " + receivedSignature);

            String calculatedSignature = generateSignature(verifyData);
            System.out.println("Calculated signature: " + calculatedSignature);

            boolean signatureValid = receivedSignature.equalsIgnoreCase(calculatedSignature);
            System.out.println("Signature valid: " + signatureValid);

            String paymentStatus = payFastData.get("payment_status");
            boolean paymentComplete = "COMPLETE".equalsIgnoreCase(paymentStatus);
            System.out.println("Payment status: " + paymentStatus);
            System.out.println("Payment complete: " + paymentComplete);

            boolean finalResult = signatureValid && paymentComplete;
            System.out.println("Final verification result: " + finalResult);

            return finalResult;

        } catch (Exception e) {
            System.out.println("ERROR in verification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Test with PayFast's official example to verify our signature generation
     */
    public void testOfficialSignature() {
        try {
            System.out.println("=== OFFICIAL PAYFAST SIGNATURE TEST ===");

            // These are the exact values from PayFast documentation
            Map<String, String> officialParams = new TreeMap<>();
            officialParams.put("merchant_id", "10000100");
            officialParams.put("merchant_key", "46f0cd694581a");
            officialParams.put("return_url", "https://www.example.com/success");
            officialParams.put("cancel_url", "https://www.example.com/cancel");
            officialParams.put("notify_url", "https://www.example.com/notify");
            officialParams.put("name_first", "First");
            officialParams.put("name_last", "Last");
            officialParams.put("email_address", "test@example.com");
            officialParams.put("m_payment_id", "1234");
            officialParams.put("amount", "100.00");
            officialParams.put("item_name", "Test Item");

            // Use the official passphrase
            String officialPassphrase = "jt7NOE43FZPn";

            // Save original passphrase
            String originalPassphrase = this.passphrase;
            this.passphrase = officialPassphrase;

            String signature = generateSignature(officialParams);

            // Restore original passphrase
            this.passphrase = originalPassphrase;

            String expectedSignature = "c48d5cec33ff8d4c6d23b90d38e60c5a";
            System.out.println("Generated signature: " + signature);
            System.out.println("Expected signature: " + expectedSignature);
            System.out.println("SIGNATURE MATCHES: " + expectedSignature.equals(signature));

            if (!expectedSignature.equals(signature)) {
                System.out.println("❌ SIGNATURE GENERATION IS BROKEN!");
                System.out.println("Check your parameter ordering and passphrase handling.");
            } else {
                System.out.println("✅ SIGNATURE GENERATION WORKS CORRECTLY!");
            }

        } catch (Exception e) {
            System.out.println("Error in official test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}