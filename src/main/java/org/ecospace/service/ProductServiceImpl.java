package org.ecospace.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ecospace.exception.ProductNotFound;
import org.ecospace.model.Product;
import org.ecospace.model.User;
import org.ecospace.notification.client.MessageServiceClient;
import org.ecospace.notification.client.dto.SubscriptionRemainderRequest;
import org.ecospace.repository.ProductRepository;
import org.ecospace.repository.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class ProductServiceImpl {
    private final ProductRepository productRepository;
    private final MessageServiceClient client;
    private final UserRepository userRepository;


    public ProductServiceImpl(ProductRepository productRepository, MessageServiceClient client, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.client = client;
        this.userRepository = userRepository;
    }

    @Cacheable("products")
    public Product findById(UUID id) {

        Optional<Product> findById = this.productRepository.findById(id);
        if (findById.isEmpty()) {
            throw new ProductNotFound("Product doesn't exist!");
        }
        return findById.get();
    }

    @Scheduled(cron = "0 0 11 * * ?")
    @Transactional
    public void checkForExpired() {

        LocalDateTime warningDate = LocalDateTime.now().plusDays(7);
        LocalDateTime today = LocalDateTime.now();
        List<Product> allForRenew = productRepository.findAllByExpiredBetween(today, warningDate);
        if (!allForRenew.isEmpty()) {
            allForRenew.forEach(product -> {
                User user = product.getUser();
                if (user.getPhone() == null) {
                    throw new IllegalArgumentException("User doesn't have phone number!");
                }
                if (!product.isRenewalNotify()) {
                    SubscriptionRemainderRequest request = SubscriptionRemainderRequest.builder().
                            phone(user.getPhone())
                            .username(user.getUsername())
                            .expiredOn(product.getExpired())
                            .subsName(product.getNamePackage()).build();
                    client.sendSubscriptionReminder(request);
                    product.setRenewalNotify(true);
                    user.setNotified(true);

                    productRepository.save(product);
                    userRepository.save(user);

                }
            });
        } else {

            log.info("No expired subscriptions!");
        }
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void deleteExpiredSubscriptions() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Product> expiredProducts = productRepository
                .findAllByExpiredBeforeAndActiveTrue(sevenDaysAgo);

        if (!expiredProducts.isEmpty()) {

            expiredProducts.forEach(product -> {
                try {
                    User user = product.getUser();

                    user.getProductList().remove(product);
                    userRepository.save(user);

                    productRepository.delete(product);

                    log.info("Deleted expired subscription: '{}' for user: '{}' (expired on: {})",
                            product.getNamePackage(),
                            user.getUsername(),
                            product.getExpired());

                } catch (Exception e) {
                    log.error("Error deleting product {}: {}", product.getId(), e.getMessage());
                }
            });
        } else {
            log.debug("No expired products found for deletion");
        }
    }


}





