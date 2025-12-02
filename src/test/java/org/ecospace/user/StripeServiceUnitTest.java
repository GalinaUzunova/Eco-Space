package org.ecospace.user;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.ecospace.service.StripeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class StripeServiceUnitTest {

    @Mock
    private Session mockSession;

    @InjectMocks
    private StripeService stripeService;

    @Test
    void createCheckoutSession_WithValidParameters_ReturnsCheckoutUrl() {

        BigDecimal amount = new BigDecimal("1500.00");
        String productName = "Premium Subscription";
        String orderId = "ECO-12345";
        String customerEmail = "test@example.com";
        String expectedUrl = "https://checkout.stripe.com/session_123";


        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            when(mockSession.getUrl()).thenReturn(expectedUrl);
            when(mockSession.getId()).thenReturn("cs_test_123");


            setPrivateField(stripeService, "currency", "usd");
            setPrivateField(stripeService, "successUrl", "https://example.com/success");
            setPrivateField(stripeService, "cancelUrl", "https://example.com/cancel");


            String result = stripeService.createCheckoutSession(amount, productName, orderId, customerEmail);

            assertThat(result).isEqualTo(expectedUrl);

            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)));

            verify(mockSession).getUrl();
            verify(mockSession).getId();
        }
    }

    @Test
    void createCheckoutSession_WithoutCustomerEmail_StillCreatesSession() throws StripeException {

        BigDecimal amount = new BigDecimal("19.99");
        String productName = "Basic Plan";
        String orderId = "ECO-67890";
        String expectedUrl = "https://checkout.stripe.com/session_456";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            when(mockSession.getUrl()).thenReturn(expectedUrl);
            when(mockSession.getId()).thenReturn("cs_test_456");

            setPrivateField(stripeService, "currency", "usd");
            setPrivateField(stripeService, "successUrl", "https://example.com/success");
            setPrivateField(stripeService, "cancelUrl", "https://example.com/cancel");


            String result = stripeService.createCheckoutSession(amount, productName, orderId, null);


            assertThat(result).isEqualTo(expectedUrl);
            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }

    @Test
    void createCheckoutSession_WithEmptyCustomerEmail_DoesNotSetEmail() throws StripeException {

        BigDecimal amount = new BigDecimal("9.99");
        String productName = "Trial Plan";
        String orderId = "ECO-11111";
        String expectedUrl = "https://checkout.stripe.com/session_789";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.create(any(SessionCreateParams.class)))
                    .thenReturn(mockSession);

            when(mockSession.getUrl()).thenReturn(expectedUrl);
            when(mockSession.getId()).thenReturn("cs_test_789");

            setPrivateField(stripeService, "currency", "usd");
            setPrivateField(stripeService, "successUrl", "https://example.com/success");
            setPrivateField(stripeService, "cancelUrl", "https://example.com/cancel");


            String result = stripeService.createCheckoutSession(amount, productName, orderId, "   ");


            assertThat(result).isEqualTo(expectedUrl);
            mockedSession.verify(() -> Session.create(any(SessionCreateParams.class)));
        }
    }


    private void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }


    @Test
    void verifyPayment_WhenSessionIsPaid_ReturnsTrue() throws StripeException {

        String sessionId = "cs_test_paid_123";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn("paid");


            boolean result = stripeService.verifyPayment(sessionId);

            assertThat(result).isTrue();

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WhenSessionIsUnpaid_ReturnsFalse() throws StripeException {

        String sessionId = "cs_test_unpaid_456";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn("unpaid");

            boolean result = stripeService.verifyPayment(sessionId);

            assertThat(result).isFalse();

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WhenPaymentStatusIsNull_ReturnsFalse()  {

        String sessionId = "cs_test_null_789";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn(null);


            boolean result = stripeService.verifyPayment(sessionId);


            assertThat(result).isFalse();

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WhenPaymentStatusIsEmpty_ReturnsFalse()  {

        String sessionId = "cs_test_empty_999";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn("");


            boolean result = stripeService.verifyPayment(sessionId);


            assertThat(result).isFalse();

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }



    @Test
    void verifyPayment_WithDifferentPaymentStatuses_ReturnsCorrectBoolean() {
        // Test multiple status values
        String[][] testCases = {
                {"paid", "true"},
                {"Paid", "true"},
                {"PAID", "true"},
                {"unpaid", "false"},
                {"Unpaid", "false"},
                {"no_payment_required", "false"},
                {"processing", "false"},
                {"failed", "false"}
        };

        for (String[] testCase : testCases) {
            String status = testCase[0];
            boolean expected = Boolean.parseBoolean(testCase[1]);
            String sessionId = "cs_test_" + status.toLowerCase();

            try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
                mockedSession.when(() -> Session.retrieve(sessionId))
                        .thenReturn(mockSession);

                when(mockSession.getPaymentStatus()).thenReturn(status);

                // Act
                boolean result = stripeService.verifyPayment(sessionId);

                // Assert
                assertThat(result)
                        .withFailMessage("Expected %s for status '%s' to be %b", sessionId, status, expected)
                        .isEqualTo(expected);

                mockedSession.verify(() -> Session.retrieve(sessionId));
                verify(mockSession).getPaymentStatus();

                // Reset mocks for next iteration
                reset(mockSession);
            }
        }
    }

    @Test
    void verifyPayment_WithWhitespaceInStatus_HandlesCorrectly() {
        // Arrange
        String sessionId = "cs_test_whitespace";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn(" paid ");

            // Act
            boolean result = stripeService.verifyPayment(sessionId);

            // Assert
            assertThat(result).isFalse(); // " paid " doesn't equal "paid" exactly

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WhenSessionIsRetrievedButStatusIsUnexpectedValue_ReturnsFalse()  {
        // Arrange
        String sessionId = "cs_test_unexpected";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn("expired");

            // Act
            boolean result = stripeService.verifyPayment(sessionId);

            // Assert
            assertThat(result).isFalse();

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WithVeryLongSessionId_WorksCorrectly() {
        // Arrange
        String sessionId = "cs_test_" + "x".repeat(100);

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn("paid");

            // Act
            boolean result = stripeService.verifyPayment(sessionId);

            // Assert
            assertThat(result).isTrue();

            mockedSession.verify(() -> Session.retrieve(anyString()));
            verify(mockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WhenCalledMultipleTimes_RetrievesFreshSessionEachTime() throws StripeException {
        // Arrange
        String sessionId = "cs_test_multi";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            // First call returns unpaid
            Session firstMockSession = mock(Session.class);
            when(firstMockSession.getPaymentStatus()).thenReturn("unpaid");

            // Second call returns paid
            Session secondMockSession = mock(Session.class);
            when(secondMockSession.getPaymentStatus()).thenReturn("paid");

            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(firstMockSession)
                    .thenReturn(secondMockSession);

            // Act - First call
            boolean firstResult = stripeService.verifyPayment(sessionId);

            // Act - Second call
            boolean secondResult = stripeService.verifyPayment(sessionId);

            // Assert
            assertThat(firstResult).isFalse();
            assertThat(secondResult).isTrue();

            mockedSession.verify(() -> Session.retrieve(sessionId), times(2));
            verify(firstMockSession).getPaymentStatus();
            verify(secondMockSession).getPaymentStatus();
        }
    }

    @Test
    void verifyPayment_WithSpecialCharactersInSessionId_HandlesCorrectly() {
        // Arrange
        String sessionId = "cs_test_special-123_456.789";

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            mockedSession.when(() -> Session.retrieve(sessionId))
                    .thenReturn(mockSession);

            when(mockSession.getPaymentStatus()).thenReturn("paid");

            // Act
            boolean result = stripeService.verifyPayment(sessionId);

            // Assert
            assertThat(result).isTrue();

            mockedSession.verify(() -> Session.retrieve(sessionId));
            verify(mockSession).getPaymentStatus();
        }
    }
}


