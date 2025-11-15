package org.ecospace.service;

import jakarta.transaction.Transactional;
import org.ecospace.event.SubscriptionRenewEvent;
import org.ecospace.model.Product;
import org.ecospace.model.User;
import org.ecospace.repository.ProductRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl {
    private final ProductRepository productRepository;
    private ApplicationEventPublisher eventPublisher;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product findById(UUID id) {
        Optional<Product>findById=this.productRepository.findById(id);
        if(findById.isEmpty()){
            throw new RuntimeException("Product dost exist!");
        }
        return findById.get();
    }

    @Scheduled(cron = "0 0 11 * * ?")
    @Transactional
    public void checkForExpired(){
        LocalDateTime warningDate=LocalDateTime.now().plusDays(7);
        LocalDateTime today=LocalDateTime.now();
        List<Product>allForRenew=productRepository.findAllByExpiredBetween(today,warningDate);
        if(!allForRenew.isEmpty()) {
                allForRenew.forEach(product -> {
                    User user = product.getUser();
                    if (!user.isNotified()) {
                        SubscriptionRenewEvent event = new SubscriptionRenewEvent();
                        event.setUsername(user.getUsername());
                        event.setEmail(user.getEmail());
                        event.setExpiredOn(product.getExpired());
                        event.setPrice(product.getPrice());
                        event.setSubscriptionName(product.getNamePackage());
                        eventPublisher.publishEvent(event);
                        product.setRenewalNotify(true);
                        productRepository.save(product);
                        System.out.println("Expiring:" + product + "-" + product.getCreatedOn() + "-" + user);
                    }
                });
        }else{
            System.out.println("No expired subscriptions");
        }
    }
}
