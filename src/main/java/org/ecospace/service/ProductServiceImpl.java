package org.ecospace.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.ecospace.model.Product;
import org.ecospace.model.User;
import org.ecospace.notification.client.MessageServiceClient;
import org.ecospace.notification.client.dto.SubscriptionRemainderRequest;
import org.ecospace.repository.ProductRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ProductServiceImpl {
    private final ProductRepository productRepository;
    private MessageServiceClient client;
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;

    }
    public Product findById(UUID id) {
        Optional<Product> findById = this.productRepository.findById(id);
        if (findById.isEmpty()) {
            throw new RuntimeException("Product dost exist!");
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
                if (!user.isNotified() && user.getPhone() != null) {
                    SubscriptionRemainderRequest request = SubscriptionRemainderRequest.builder().
                            phone(user.getPhone())
                            .username(user.getUsername())
                            .expiredOn(product.getExpired())
                            .subsName(product.getNamePackage()).build();

                    client.sendSubscriptionReminder(request);

                    product.setRenewalNotify(true);
                    productRepository.save(product);

                }
            });
        } else {
            log.info("No expired subscriptions");
        }
    }
}
