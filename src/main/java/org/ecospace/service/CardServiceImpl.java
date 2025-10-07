package org.ecospace.service;

import jakarta.servlet.http.HttpSession;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.UserCard;
import org.ecospace.model.dto.UserCardDto;
import org.ecospace.repository.CardRepository;

import org.ecospace.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import java.util.UUID;

@Service
public class CardServiceImpl {


    private final CardRepository cardRepository;
    private final SubscriptionServiceImpl subscriptionService;
    private final UserServiceImpl userService;
    private final UserRepository userRepository;


    public CardServiceImpl(CardRepository cardRepository, SubscriptionServiceImpl subscriptionService, UserServiceImpl userService, UserRepository userRepository) {

        this.cardRepository = cardRepository;
        this.subscriptionService = subscriptionService;


        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Transactional
    public void create(HttpSession httpSession, UserCardDto cardDto, UUID id) {
        if (httpSession.getAttribute("userId") == null) {
            throw new RuntimeException("You need to have account!");
        }

        UUID userId = (UUID) httpSession.getAttribute("userId");
        User byId =userService.byId(userId);

        Subscription subscription = subscriptionService.byId(id);

        subscription.setCreatedOn(LocalDateTime.now());
        LocalDateTime expire=userService.createSubscriptionPeriod(subscription);
        subscription.setExpiresOn(expire);

        List<Subscription> userSubscriptions = new ArrayList<>(userService.getClentSubs(userId));
        userSubscriptions.add(subscription);
        byId.setSubscriptions(userSubscriptions);

        UserCard card = new UserCard();
        card.setCard(cardDto.getCard());
        card.setName(cardDto.getName());
        card.setExpiresOn(YearMonth.parse(cardDto.getExpiresOn(), DateTimeFormatter.ofPattern("MM/yy")));
        card.setCvv(cardDto.getCvv());
        this.cardRepository.save(card);
        byId.setUserCard(card);


        this.userRepository.save(byId);


    }
}
