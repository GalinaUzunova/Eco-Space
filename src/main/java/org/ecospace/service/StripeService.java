package org.ecospace.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Value("${stripe.currency}")
    private String currency;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
        System.out.println("✅ Stripe initialized successfully with version 24.0.0");
    }

    /**
     * Create a Stripe Checkout Session for one-time payment
     */
    public String createCheckoutSession(BigDecimal amount, String productName,
                                        String orderId, String customerEmail) {
        try {
            System.out.println("=== CREATING STRIPE CHECKOUT SESSION ===");
            System.out.println("Product: " + productName);
            System.out.println("Amount: " + amount + " " + currency);
            System.out.println("Order ID: " + orderId);

            // Build the line item
            SessionCreateParams.LineItem lineItem = SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(amount.multiply(BigDecimal.valueOf(100)).longValue()) // Convert to cents
                                    .setProductData(
                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                    .setName(productName)
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            // Build the session parameters
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl + "?order_id=" + orderId)
                    .addLineItem(lineItem)
                    .putMetadata("order_id", orderId)
                    .putMetadata("product_name", productName);

            // Add customer email if provided
            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                paramsBuilder.setCustomerEmail(customerEmail);
                System.out.println("Customer email: " + customerEmail);
            }

            // Create the session
            Session session = Session.create(paramsBuilder.build());
            String checkoutUrl = session.getUrl();

            System.out.println("✅ Stripe Session Created");
            System.out.println("Session ID: " + session.getId());
            System.out.println("Checkout URL: " + checkoutUrl);

            return checkoutUrl;

        } catch (StripeException e) {
            System.err.println("❌ Stripe Error: " + e.getMessage());
            throw new RuntimeException("Failed to create Stripe checkout session: " + e.getMessage(), e);
        }
    }

    /**
     * Verify if payment was successful
     */
    public boolean verifyPayment(String sessionId) {
        try {
            System.out.println("=== VERIFYING PAYMENT ===");
            System.out.println("Session ID: " + sessionId);

            Session session = Session.retrieve(sessionId);
            String paymentStatus = session.getPaymentStatus();

            System.out.println("Payment Status: " + paymentStatus);
            System.out.println("Session Status: " + session.getStatus());

            boolean isPaid = "paid".equalsIgnoreCase(paymentStatus);
            System.out.println("Payment Verified: " + isPaid);

            return isPaid;

        } catch (StripeException e) {
            System.err.println("❌ Payment verification failed: " + e.getMessage());
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get payment details
     */
    public Map<String, Object> getPaymentDetails(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);

            Map<String, Object> details = new HashMap<>();
            details.put("id", session.getId());
            details.put("payment_status", session.getPaymentStatus());
            details.put("status", session.getStatus());
            details.put("amount_total", session.getAmountTotal());
            details.put("currency", session.getCurrency());
            details.put("customer_email", session.getCustomerEmail());
            details.put("metadata", session.getMetadata());
            details.put("payment_intent", session.getPaymentIntent());

            return details;

        } catch (StripeException e) {
            throw new RuntimeException("Failed to get payment details: " + e.getMessage(), e);
        }
    }



}

