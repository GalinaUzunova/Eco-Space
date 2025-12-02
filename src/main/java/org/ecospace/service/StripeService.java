package org.ecospace.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
@Slf4j
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
        log.info("✅ Stripe initialized successfully with version 24.0.0");
    }


    public String createCheckoutSession(BigDecimal amount, String productName,
                                        String orderId, String customerEmail) {
        try {

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

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .addLineItem(lineItem)
                    .putMetadata("order_id", orderId)
                    .putMetadata("product_name", productName);


            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                paramsBuilder.setCustomerEmail(customerEmail);

            }


            Session session = Session.create(paramsBuilder.build());
            String checkoutUrl = session.getUrl();

           log.info("✅ Stripe Session Created");
           log.info("Session ID: " + session.getId());
          log.info("Checkout URL: " + checkoutUrl);

            return checkoutUrl;

        } catch (StripeException e) {
          log.error("❌ Stripe Error: " + e.getMessage());
            throw new RuntimeException("Failed to create Stripe checkout session: " + e.getMessage(), e);
        }
    }

    public boolean verifyPayment(String sessionId) {
        try {


            Session session = Session.retrieve(sessionId);
            String paymentStatus = session.getPaymentStatus();

            boolean isPaid = "paid".equalsIgnoreCase(paymentStatus);
            System.out.println("Payment Verified: " + isPaid);

            return isPaid;

        } catch (StripeException e) {
          log.error("❌ Payment verification failed: " + e.getMessage());
            throw new RuntimeException("Payment verification failed: " + e.getMessage(), e);
        }
    }


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

