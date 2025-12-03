package org.ecospace.web;

import org.ecospace.exception.SubscriptionNotFoundException;
import org.ecospace.exception.UserNotFoundException;
import org.ecospace.model.*;
import org.ecospace.model.dto.UserDto;
import org.ecospace.repository.ProductRepository;
import org.ecospace.repository.SubscriptionRepository;
import org.ecospace.repository.UserRepository;
import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;


@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
@SpringBootTest
public class SubscriptionITest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserServiceImpl userService;
    private AuthenticationMetadata metadata;
    private Subscription testSubs;
    private User user;

    @Test
    void initiateBuyingNewSubscription_successful() {

        UserDto userDto = new UserDto("Nik",
                "gbuzunova13@gmail.com",
                "000000",
                "000000");
        userService.userRegister(userDto);

        user = userRepository.findByUsername("Nik").orElse(null);
        assert user != null;
        List<Product> userSubscriptions = user.getProductList();


        testSubs = Subscription.builder()
                .namePackage("2D Basic")
                .description("Premium monthly subscription")
                .price(1500.00)
                .type(SubscriptionType.DESIGN)
                .build();


        subscriptionRepository.save(testSubs);

        metadata =
                new AuthenticationMetadata("Nik",
                        "0000000",
                        user.getId(),
                        UserRole.CLIENT,
                        true);


        String stripeUrl = userService.initiatePayment(metadata, testSubs.getId());

        assertEquals(0, userSubscriptions.size());

        assertThat(stripeUrl).isNotNull();
        assertThat(stripeUrl).isNotEmpty();
        assertThat(stripeUrl).contains("checkout.stripe.com");


        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getPendingPaymentOrderId()).isNotNull();
        assertThat(updatedUser.getPendingPaymentOrderId()).startsWith("ECO-");
        assertThat(updatedUser.getSetPendingSubscriptionId()).isEqualTo(testSubs.getId());


        String orderId = updatedUser.getPendingPaymentOrderId();
        assertThat(orderId).contains(user.getId().toString());
        assertThat(orderId).contains(testSubs.getId().toString());
        assertThat(orderId).matches("ECO-" +
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-" +
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-" +
                "\\d+");


    }

    @Test
    void initiatePayment_ValidatesPendingPaymentState() {

        userService.initiatePayment(metadata, testSubs.getId());

        User userWithPending = userRepository.findById(user.getId()).orElseThrow();
        String orderId = userWithPending.getPendingPaymentOrderId();
        UUID subscriptionId = userWithPending.getSetPendingSubscriptionId();

        assertThat(orderId.split("-")).hasSize(11);
        assertThat(subscriptionId).isEqualTo(testSubs.getId());
    }

    @Test
    void initiateRenewPayment_successes(){

        UserDto userDto = new UserDto("Nik",
                "gbuzunova13@gmail.com",
                "000000",
                "000000");
        userService.userRegister(userDto);

        user = userRepository.findByUsername("Nik").orElse(null);
        assert user != null;

        Product testProduct= Product.builder()
                .namePackage("Monthly")
                .expired(LocalDateTime.now().plusDays(5))
                .isRenewalNotify(true)
                .user(user)
                .createdOn(LocalDateTime.now().minusMonths(1))
                .type(SubscriptionType.MAINTANACE)
                .description("Monthly cleaning")
                .price(500.00)
                .build();
       productRepository.save(testProduct);
       user.getProductList().add(testProduct);
       userRepository.save(user);


        metadata =
                new AuthenticationMetadata("Nik",
                        "0000000",
                        user.getId(),
                        UserRole.CLIENT,
                        true);


        String stripeUrl = userService.initiateRenewPayment(metadata, testProduct.getId());
        assertEquals(1, user.getProductList().size());

        assertThat(stripeUrl).isNotNull();
        assertThat(stripeUrl).isNotEmpty();
        assertThat(stripeUrl).contains("checkout.stripe.com");


        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updatedUser.getPendingPaymentOrderId()).isNotNull();
        assertThat(updatedUser.getPendingPaymentOrderId()).startsWith("RENEW-");
        assertThat(updatedUser.getSetPendingSubscriptionId()).isEqualTo(testProduct.getId());


        String orderId = updatedUser.getPendingPaymentOrderId();
        assertThat(orderId).contains(user.getId().toString());
        assertThat(orderId).contains(testProduct.getId().toString());
        assertThat(orderId).matches("RENEW-" +
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-" +
                "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-" +
                "\\d+");




    }


    @Test
    void cancelSubscription_success() {

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .productList(new ArrayList<>())
                .build();
        User savedUser = userRepository.save(user);

        Product product = Product.builder()
                .namePackage("Premium Monthly")
                .description("Premium subscription")
                .price(1500.00)
                .type(SubscriptionType.MAINTANACE)
                .user(savedUser)
                .createdOn(LocalDateTime.now())
                .expired(LocalDateTime.now().plusMonths(1))
                .build();
        Product savedProduct = productRepository.save(product);

        savedUser.getProductList().add(savedProduct);
        userRepository.save(savedUser);

        metadata =
                new AuthenticationMetadata("Nik",
                        "0000000",
                        user.getId(),
                        UserRole.CLIENT,
                        true);

        userService.cancelSubscription(metadata, savedProduct.getId());

        assertThat(productRepository.findById(savedProduct.getId())).isEmpty();


        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getProductList()).doesNotContain(savedProduct);
        assertThat(updatedUser.getProductList()).isEmpty();


    }
    @Test
    void cancelSubscription_userNotFound_throwsException() {

        UUID nonExistentUserId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        AuthenticationMetadata metadata = new AuthenticationMetadata(
                "nonexistent",
                "password",
                nonExistentUserId,
                UserRole.CLIENT,
                true
        );


        assertThatThrownBy(() ->
                userService.cancelSubscription(metadata, productId)
        ).isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }


    @Test
    void cancelSubscription_productNotFound_throwsException() {

        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .productList(new ArrayList<>())
                .build();
        User savedUser = userRepository.save(user);

        UUID nonExistentProductId = UUID.randomUUID();

        AuthenticationMetadata metadata = new AuthenticationMetadata(
                savedUser.getUsername(),
                savedUser.getPassword(),
                savedUser.getId(),
                UserRole.CLIENT,
                true
        );


        assertThatThrownBy(() ->
                userService.cancelSubscription(metadata, nonExistentProductId)
        ).isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining(nonExistentProductId.toString());
    }


    @Test
    void cancelSubscription_userDoesNotOwnProduct_throwsException() {

        User user1 = User.builder()
                .username("user1")
                .email("user1@example.com")
                .password("password")
                .productList(new ArrayList<>())
                .build();
        User savedUser1 = userRepository.save(user1);

        User user2 = User.builder()
                .username("user2")
                .email("user2@example.com")
                .password("password")
                .productList(new ArrayList<>())
                .build();
        User savedUser2 = userRepository.save(user2);


        Product product = Product.builder()
                .namePackage("Premium Monthly")
                .description("Premium subscription")
                .price(1500.00)
                .type(SubscriptionType.MAINTANACE)
                .user(savedUser2)
                .createdOn(LocalDateTime.now())
                .expired(LocalDateTime.now().plusMonths(1))
                .build();
        Product savedProduct = productRepository.save(product);

        savedUser2.getProductList().add(savedProduct);
        userRepository.save(savedUser2);


        AuthenticationMetadata metadata = new AuthenticationMetadata(
                savedUser1.getUsername(),
                savedUser1.getPassword(),
                savedUser1.getId(),
                UserRole.CLIENT,
                true
        );


        assertThatThrownBy(() ->
                userService.cancelSubscription(metadata, savedProduct.getId())
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User does not have subscription with id: " + savedProduct.getId());
    }











}



