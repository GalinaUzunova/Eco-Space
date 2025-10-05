package org.ecospace.service;

import jakarta.servlet.http.HttpSession;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.UserCard;
import org.ecospace.model.dto.UserCardDto;
import org.ecospace.repository.CardRepository;
import org.ecospace.repository.SubscriptionRepository;
import org.ecospace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardServiceImpl {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final SubscriptionServiceImpl subscriptionService;

    public CardServiceImpl(UserRepository userRepository, CardRepository cardRepository, SubscriptionServiceImpl subscriptionService) {
        this.userRepository = userRepository;
        this.cardRepository = cardRepository;
        this.subscriptionService = subscriptionService;

    }

    @Transactional
    public void create(HttpSession httpSession, UserCardDto cardDto, UUID id) {
        if (httpSession == null) {
            throw new RuntimeException("You need to have account!");
        }

        UUID userId = (UUID) httpSession.getAttribute("userId");
        Optional<User> byId = userRepository.findById(userId);

        if (byId.isEmpty()) {
            throw new RuntimeException("User not exist");
        }
        Subscription subscription = subscriptionService.byId(id);
        List<Subscription> subscribtionsByUser = subscriptionService.getAllSubscriptionByUserId(byId.get().getId());
        if(subscribtionsByUser == null){
            subscribtionsByUser=new ArrayList<>();
        }
        subscribtionsByUser.add(subscription);
        UserCard card = new UserCard();
        card.setCard(cardDto.getCard());
        card.setName(cardDto.getName());
        card.setExpiresOn(YearMonth.parse(cardDto.getExpiresOn(), DateTimeFormatter.ofPattern("MM/yy")));
        card.setCvv(cardDto.getCvv());

        this.cardRepository.save(card);
        byId.get().setUserCard(card);
        byId.get().setSubscriptions(subscribtionsByUser);

        this.userRepository.save(byId.get());

    }
}
