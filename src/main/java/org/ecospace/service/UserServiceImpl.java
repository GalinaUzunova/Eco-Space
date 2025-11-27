package org.ecospace.service;

import lombok.extern.slf4j.Slf4j;
import org.ecospace.exception.*;
import org.ecospace.model.Product;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.UserRole;
import org.ecospace.model.dto.ProfileDto;
import org.ecospace.model.dto.UserCardDto;
import org.ecospace.model.dto.UserDto;
import org.ecospace.repository.ProductRepository;
import org.ecospace.repository.UserRepository;
import org.ecospace.security.AuthenticationMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Slf4j

@Service
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionServiceImpl subscriptionService;
    private final ProductRepository productRepository;
    private final StripeService stripeService;


    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, SubscriptionServiceImpl subscriptionService, ProductRepository productRepository, StripeService stripeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionService = subscriptionService;
        this.productRepository = productRepository;

        this.stripeService = stripeService;
    }


    public boolean userExists(UserDto userDto) {

        return userRepository.findByUsername(userDto.getUsername())
                .isPresent();
    }


    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void userRegister(UserDto userDto) {

        User user = new User();
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setActive(true);
        user.setCreatedOn(LocalDateTime.now());
        if (this.userRepository.count() <= 0) {
            user.setRole(UserRole.ADMIN);
        } else {
            user.setRole(UserRole.CLIENT);
        }
        userRepository.save(user);
    }


    @Cacheable(value = "users", unless = "#result == null")
    public User byId(UUID id) {

        Optional<User> userById = this.userRepository.findById(id);
        if (userById.isEmpty()) {
            throw new UserNotFoundException("User with id:" + id + " not exist");
        }
        return userById.get();
    }


    @Cacheable("products")
    public List<Product> getClientSubs(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not exist!");
        }
        if (userRepository.findUserSubs(id).isEmpty()) {
            return new ArrayList<>();
        }
        return this.userRepository.findUserSubs(id);
    }


    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void renew(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, UserCardDto cardDto, UUID id) {

        Optional<User> user = userRepository.findById(authenticationMetadata.getId());
        if (user.isEmpty()) {
            throw new UserNotFoundException("Not Authorized operation");

        }
        Product product = this.userRepository.findUserSubs(authenticationMetadata.getId())
                .stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ProductNotFound("Product with id " + id + " not found"));

        product.setActive(true);
        product.setCreatedOn(LocalDateTime.now());
        product.setExpired(createSubscriptionPeriod(product.getNamePackage()));
        this.productRepository.save(product);
        user.get().setNotified(false);
        userRepository.save(user.get());

    }


    @Cacheable("users")
    public List<User> getAllUsersAndSubs() {
        if (this.userRepository.findAllByAndProductList() != null) {
            return this.userRepository.findAllByAndProductList();
        }
        return new ArrayList<>();
    }

    @Cacheable("users")
    public List<User> getAllUsers() {
        List<User> allUsers = this.userRepository.getAllBy();
        return Objects.requireNonNullElseGet(allUsers, ArrayList::new);
    }


    @CacheEvict(value = "users", allEntries = true)
    public void editProfile(ProfileDto profileDto, @AuthenticationPrincipal AuthenticationMetadata authenticationPrinciple) {

        User user = userRepository.findById(authenticationPrinciple.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Not Authorized operation"));

        if (profileDto.getImageURL() != null && !profileDto.getImageURL().isEmpty()) {
            user.setImage(profileDto.getImageURL());

        }
        user.setEmail(profileDto.getEmail());
        user.setUsername(profileDto.getUsername());
        user.setPhone(profileDto.getPhone());
        this.userRepository.save(user);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = this.userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User doesn't exist"));
        return new AuthenticationMetadata(user.getUsername(), user.getPassword(), user.getId(), user.getRole(), user.isActive());
    }

    public User getAdmin(@AuthenticationPrincipal AuthenticationMetadata principal) {

        Optional<User> user = this.userRepository.findById(principal.getId());
        if (user.isPresent()) {
            return user.get();
        }
        throw new AccesDeniedException("Not Authorized operation");
    }

    @CacheEvict(value = "users", allEntries = true)
    public void changeRole(UUID id) {

        User user = this.userRepository.findById(id)
                .orElseThrow(() -> new AccesDeniedException("Not Authorized operation"));

        if (user.getRole() == UserRole.CLIENT) {
            user.setRole(UserRole.ADMIN);
        } else if (user.getRole() == UserRole.ADMIN) {
            user.setRole(UserRole.CLIENT);
        }

        this.userRepository.save(user);
    }

    @Transactional
    public String initiatePayment(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,
                                  UUID subscriptionId) {
        System.out.println("=== INITIATING STRIPE PAYMENT ===");

        UUID userId = authenticationMetadata.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Subscription subscription = subscriptionService.byId(subscriptionId);

        // Create unique order ID
        String merchantOrderId = "ECO-" + userId + "-" + subscriptionId + "-" + System.currentTimeMillis();

        // Store pending payment
        user.setPendingPaymentOrderId(merchantOrderId);
        user.setSetPendingSubscriptionId(subscriptionId);
        userRepository.save(user);

        System.out.println("Stored pending payment: " + merchantOrderId);

        try {
            // Use Stripe for payment
            String stripeUrl = stripeService.createCheckoutSession(
                    BigDecimal.valueOf(subscription.getPrice()),
                    subscription.getNamePackage(),
                    merchantOrderId,
                    user.getEmail() != null ? user.getEmail() : "customer@example.com"
            );

            System.out.println("✅ Stripe payment initiated successfully");
            return stripeUrl;

        } catch (Exception e) {
            System.err.println("❌ Stripe payment initiation failed: " + e.getMessage());
            // Clear pending payment on failure
            user.setPendingPaymentOrderId(null);
            user.setSetPendingSubscriptionId(null);
            userRepository.save(user);

            throw new PaymentException("Failed to initiate payment: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "users"}, allEntries = true)
    public boolean completePayment(String sessionId) {
        System.out.println("=== COMPLETING STRIPE PAYMENT ===");
        System.out.println("Session ID: " + sessionId);

        try {
            // Verify payment with Stripe
            boolean paymentVerified = stripeService.verifyPayment(sessionId);

            if (!paymentVerified) {
                System.out.println("❌ Payment verification failed");
                return false;
            }

            // Get payment details to extract our order ID
            Map<String, Object> paymentDetails = stripeService.getPaymentDetails(sessionId);
            Map<String, String> metadata = (Map<String, String>) paymentDetails.get("metadata");
            String merchantOrderId = metadata.get("order_id");

            System.out.println("Extracted Order ID: " + merchantOrderId);

            // Process the payment completion using existing logic
            return processPaymentCompletion(merchantOrderId);

        } catch (Exception e) {
            System.err.println("❌ ERROR in payment completion: " + e.getMessage());

            throw new PaymentException("Failed to complete payment: " + e.getMessage());
        }
    }

    /**
     * Keep your existing method - it works perfectly
     */
    private boolean processPaymentCompletion(String merchantOrderId) {
        System.out.println("=== PROCESSING PAYMENT COMPLETION ===");
        System.out.println("Merchant Order ID: " + merchantOrderId);

        try {
            // Split the order ID properly
            String[] orderParts = merchantOrderId.split("-");
            System.out.println("Order parts: " + Arrays.toString(orderParts));

            if (orderParts.length < 6) {
                System.err.println("❌ Invalid order ID format. Expected at least 6 parts, got: " + orderParts.length);
                throw new PaymentException("Invalid order ID format");
            }

            // Reconstruct the UUIDs properly
            // User ID: parts 1-5 (a1db991d-b298-4d97-8510-2575544d154f)
            String userIdStr = orderParts[1] + "-" + orderParts[2] + "-" + orderParts[3] + "-" + orderParts[4] + "-" + orderParts[5];

            // Subscription ID: parts 6-10 (4b60e93d-b13a-44ba-abf8-b89a89f4883f)
            String subscriptionIdStr = orderParts[6] + "-" + orderParts[7] + "-" + orderParts[8] + "-" + orderParts[9] + "-" + orderParts[10];

            UUID userId = UUID.fromString(userIdStr);
            UUID subscriptionId = UUID.fromString(subscriptionIdStr);

            System.out.println("User ID: " + userId);
            System.out.println("Subscription ID: " + subscriptionId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Subscription subscription = subscriptionService.byId(subscriptionId);
            System.out.println("Found subscription: " + subscription.getNamePackage());

            // Clear pending payment
            user.setPendingPaymentOrderId(null);
            user.setSetPendingSubscriptionId(null);
            userRepository.save(user);

            System.out.println("Creating and assigning product...");
            createAndAssignProduct(subscription, user);

            System.out.println("✅ Payment processing completed successfully");
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error in processPaymentCompletion: " + e.getMessage());

            throw new PaymentException("Failed to process payment completion: " + e.getMessage());
        }
    }


    private void createAndAssignProduct(Subscription subscription, User user) {

        Product product = createProductFromSubs(subscription);

        List<Product> productList = new ArrayList<>(userRepository.findUserSubs(user.getId()));
        productList.add(product);
        user.setProductList(productList);
        this.productRepository.save(product);

        this.userRepository.save(user);

    }

    private static Product createProductFromSubs(Subscription subscription) {
        Product product = new Product();
        product.setNamePackage(subscription.getNamePackage());
        product.setPrice(subscription.getPrice());
        product.setDescription(subscription.getDescription());
        product.setCreatedOn(LocalDateTime.now());
        product.setType(subscription.getType());
        product.setExpired(createSubscriptionPeriod(subscription.getNamePackage()));
        product.setActive(true);
        return product;
    }

    private static LocalDateTime createSubscriptionPeriod(String packageName) {
        LocalDateTime expiresOn = LocalDateTime.now();
        if (packageName.contains("Monthly")) {
            expiresOn = LocalDateTime.now().plusMonths(1);
        } else if (packageName.contains("Year")) {
            expiresOn = LocalDateTime.now().plusYears(1);
        } else if (packageName.contains("6-Month")) {
            expiresOn = LocalDateTime.now().plusMonths(6);
        }
        expiresOn = expiresOn.with(LocalTime.MAX);

        return expiresOn;
    }

//    @Transactional
//    public void storePendingPayment(UUID userId, String orderId, UUID subscriptionId) {
//        log.info("Storing pending payment for user: {}, order: {}", userId, orderId);
//
//        Optional<User> user = userRepository.findById(userId);
//        if (user.isEmpty()) {
//            throw new UserNotFoundException("User not found!");
//        }
//        user.get().setPendingPaymentOrderId(orderId);
//        user.get().setSetPendingSubscriptionId(subscriptionId);
//        userRepository.save(user.get());
//    }
//
//    @Transactional
//    public void clearPendingPayment(UUID userId) {
//        log.info("Clearing pending payment for user: {}", userId);
//        Optional<User> user = userRepository.findById(userId);
//        if (user.isEmpty()) {
//            throw new UserNotFoundException("User not found!");
//        }
//
//        user.get().setPendingPaymentOrderId(null);
//        user.get().setSetPendingSubscriptionId(null);
//        userRepository.save(user.get());
//    }

    @Transactional
    @CacheEvict(value = {"products", "users"}, allEntries = true)
    public void cancelSubscription(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata,UUID subsId){

        Optional<User> userById=this.userRepository.findById(authenticationMetadata.getId());
        if(userById.isEmpty()){
            throw new UserNotFoundException("User not found!");

        }
        Optional<Product> subsById = this.productRepository.findById(subsId);
        if(subsById.isEmpty()){
            throw  new SubscriptionNotFoundException(subsId);
        }
        User user=userById.get();
        if (!user.getProductList().contains(subsById.get())) {
            throw new IllegalArgumentException("User does not have subscription with id: " + subsId);
        }

        user.getProductList().remove(subsById.get());

        userRepository.save(user);
        productRepository.delete(subsById.get());





    }
}
















