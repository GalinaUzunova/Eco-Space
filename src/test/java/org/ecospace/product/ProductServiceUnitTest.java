package org.ecospace.product;

import org.ecospace.model.Product;
import org.ecospace.model.User;
import org.ecospace.notification.client.MessageServiceClient;
import org.ecospace.notification.client.dto.SubscriptionRemainderRequest;
import org.ecospace.repository.ProductRepository;
import org.ecospace.service.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceUnitTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private MessageServiceClient client;
    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void checkForExpired_whenProductsNeedRenewal_thenSendNotificationsAndUpdateProducts() {
        User userWithPhone = User.builder()
                .username("john_doe")
                .phone("+1234567890")
                .isNotified(false)
                .build();
        userWithPhone.setId(UUID.randomUUID());

        Product product1 = Product.builder()
                .namePackage("Premium Plan")
                .expired(LocalDateTime.now().plusDays(3))
                .isRenewalNotify(false)
                .user(userWithPhone)
                .build();
        product1.setId(UUID.randomUUID());

        Product product2 = Product.builder()
                .namePackage("Basic Plan")
                .expired(LocalDateTime.now().plusDays(5))
                .isRenewalNotify(false)
                .user(userWithPhone)
                .build();
        product2.setId(UUID.randomUUID());
        List<Product> expiringProducts = Arrays.asList(product1, product2);


        when(productRepository.findAllByExpiredBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringProducts);

        productService.checkForExpired();
        verify(client, times(2)).sendSubscriptionReminder(any(SubscriptionRemainderRequest.class));
        verify(productRepository, times(2)).save(any(Product.class));
        assertTrue(product1.isRenewalNotify());
        assertTrue(product2.isRenewalNotify());


    }

    @Test
    void checkForExpired_whenUserAlreadyNotified_thenSkipNotification() {

        User userNotified = User.builder()
                .username("bob")
                .phone("034567890")
                .isNotified(true)
                .build();
        userNotified.setId(UUID.randomUUID());

        Product product = Product.builder()
                .namePackage("Business Plan")
                .expired(LocalDateTime.now().plusDays(4))
                .isRenewalNotify(true)
                .user(userNotified)
                .build();

        product.setId(UUID.randomUUID());


        List<Product> expiringProducts = List.of(product);


        when(productRepository.findAllByExpiredBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(expiringProducts);


        productService.checkForExpired();

        verify(client, never()).sendSubscriptionReminder(any());
        verify(productRepository,never()).save(product);

    }
}
