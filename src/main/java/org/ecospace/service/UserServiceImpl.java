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


        UUID userId = authenticationMetadata.getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Subscription subscription = subscriptionService.byId(subscriptionId);

        String merchantOrderId = "ECO-" + userId + "-" + subscriptionId + "-" + System.currentTimeMillis();


        user.setPendingPaymentOrderId(merchantOrderId);
        user.setSetPendingSubscriptionId(subscriptionId);
        userRepository.save(user);

        System.out.println("Stored pending payment: " + merchantOrderId);

        try {

            String stripeUrl = stripeService.createCheckoutSession(
                    BigDecimal.valueOf(subscription.getPrice()),
                    subscription.getNamePackage(),
                    merchantOrderId,
                    user.getEmail() != null ? user.getEmail() : "customer@example.com"
            );

            log.info("✅ Stripe payment initiated successfully");
            return stripeUrl;

        } catch (Exception e) {
            log.error("❌ Stripe payment initiation failed: " + e.getMessage());

            user.setPendingPaymentOrderId(null);
            user.setSetPendingSubscriptionId(null);
            userRepository.save(user);

            throw new PaymentException("Failed to initiate payment: " + e.getMessage());
        }
    }

    @Transactional
    @CacheEvict(value = {"products", "users"}, allEntries = true)
    public boolean completePayment(String sessionId) {

        try {

            boolean paymentVerified = stripeService.verifyPayment(sessionId);

            if (!paymentVerified) {
                return false;
            }

            Map<String, Object> paymentDetails = stripeService.getPaymentDetails(sessionId);

            Map<String, String> metadata = extractMetadataSafely(paymentDetails);
            String merchantOrderId = metadata.get("order_id");

            log.info("Extracted Order ID: " + merchantOrderId);

            processPaymentCompletion(merchantOrderId);
            return true;

        } catch (Exception e) {
            log.info("❌ ERROR in payment completion: " + e.getMessage());

            throw new PaymentException("Failed to complete payment: " + e.getMessage());
        }
    }

    private void processPaymentCompletion(String merchantOrderId) {

        log.info("Merchant Order ID: " + merchantOrderId);

        try {

            String[] orderParts = merchantOrderId.split("-");

            if (orderParts.length < 6) {
                log.error("❌ Invalid order ID format. Expected at least 6 parts, got: " + orderParts.length);
                throw new PaymentException("Invalid order ID format");
            }

            String userIdStr = orderParts[1] + "-" + orderParts[2] + "-" + orderParts[3] + "-" + orderParts[4] + "-" + orderParts[5];

            String subscriptionIdStr = orderParts[6] + "-" + orderParts[7] + "-" + orderParts[8] + "-" + orderParts[9] + "-" + orderParts[10];

            UUID userId = UUID.fromString(userIdStr);
            UUID subscriptionId = UUID.fromString(subscriptionIdStr);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            Subscription subscription = subscriptionService.byId(subscriptionId);
            if (!merchantOrderId.equals(user.getPendingPaymentOrderId()) ||
                    !subscriptionId.equals(user.getSetPendingSubscriptionId())) {
                log.error("Payment order ID mismatch. Expected: {}, Got: {}",
                        user.getPendingPaymentOrderId(), merchantOrderId);
                throw new PaymentException("Payment order ID mismatch");
            }

            createAndAssignProduct(subscription, user);
            user.setPendingPaymentOrderId(null);
            user.setSetPendingSubscriptionId(null);
            userRepository.save(user);

            log.info("✅ Payment processing completed successfully");

        } catch (Exception e) {
            log.error("❌ Error in processPaymentCompletion: " + e.getMessage());
            throw new PaymentException("Failed to process payment completion: " + e.getMessage());
        }
    }


    private Map<String, String> extractMetadataSafely(Map<String, Object> paymentDetails) {
        Object metadataObj = paymentDetails.get("metadata");
        Map<String, String> metadata = new HashMap<>();

        if (metadataObj instanceof Map<?, ?> rawMap) {
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String key) {
                    String value = entry.getValue() != null ? entry.getValue().toString() : null;
                    metadata.put(key, value);
                }
            }
        }
        return metadata;
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


    @Transactional
    @CacheEvict(value = {"products", "users"}, allEntries = true)
    public void cancelSubscription(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, UUID subsId) {

        Optional<User> userById = this.userRepository.findById(authenticationMetadata.getId());
        if (userById.isEmpty()) {
            throw new UserNotFoundException("User not found!");

        }
        Optional<Product> subsById = this.productRepository.findById(subsId);
        if (subsById.isEmpty()) {
            throw new SubscriptionNotFoundException(subsId);
        }
        User user = userById.get();
        if (!user.getProductList().contains(subsById.get())) {
            throw new IllegalArgumentException("User does not have subscription with id: " + subsId);
        }

        user.getProductList().remove(subsById.get());

        userRepository.save(user);
        productRepository.delete(subsById.get());


    }

    @Transactional
    public void clearPendingPaymentBySession(String sessionId) {
        try {
            Map<String, Object> paymentDetails = stripeService.getPaymentDetails(sessionId);
            Map<String, String> metadata = extractMetadataSafely(paymentDetails);
            String merchantOrderId = metadata.get("order_id");

            if (merchantOrderId != null) {
                clearPendingPaymentByOrderId(merchantOrderId);
            }
        } catch (Exception e) {
            log.warn("Could not clear pending payment by session, order ID might be unavailable: {}", e.getMessage());
        }
    }

        @Transactional
        public void clearPendingPaymentByOrderId (String merchantOrderId){
            try {
                String[] orderParts = merchantOrderId.split("-");
                if (orderParts.length >= 6) {
                    String userIdStr = orderParts[1] + "-" + orderParts[2] + "-" + orderParts[3] + "-" + orderParts[4] + "-" + orderParts[5];
                    UUID userId = UUID.fromString(userIdStr);

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException("User not found"));

                    if (merchantOrderId.equals(user.getPendingPaymentOrderId())) {
                        user.setPendingPaymentOrderId(null);
                        user.setSetPendingSubscriptionId(null);
                        userRepository.save(user);
                        log.info("✅ Cleared pending payment for order: {}", merchantOrderId);
                    }
                }
            } catch (Exception e) {
                log.error("❌ Failed to clear pending payment for order: {}", merchantOrderId, e);
            }
        }

    @Transactional
    public void clearPendingPaymentForUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String oldOrderId = user.getPendingPaymentOrderId();
        user.setPendingPaymentOrderId(null);
        user.setSetPendingSubscriptionId(null);
        userRepository.save(user);

        log.info("✅ Cleared pending payment for user: {}, previous order: {}", userId, oldOrderId);
    }

    }
















