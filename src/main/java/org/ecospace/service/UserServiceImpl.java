package org.ecospace.service;

import jakarta.servlet.http.HttpSession;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.UserRole;
import org.ecospace.model.dto.LoginDto;
import org.ecospace.model.dto.SubscriptionDtos;
import org.ecospace.model.dto.UserCardDto;
import org.ecospace.model.dto.UserDto;
import org.ecospace.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl {



        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final SubscriptionServiceImpl subscriptionService;



        @Autowired
        public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, SubscriptionServiceImpl subscriptionService) {
            this.userRepository = userRepository;

            this.passwordEncoder = passwordEncoder;

            this.subscriptionService = subscriptionService;
        }

        @Transactional
        public boolean userRegister(UserDto userDto) {
            Optional<User> byUsernameAndEmail = this.userRepository.findByUsernameAndEmail(userDto.getUsername(), userDto.getEmail());

            if (byUsernameAndEmail.isPresent()) {

                return false;
            }

            User user = new User();
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setActive(true);
            user.setRole(UserRole.CLIENT);
            user.setRenew(false);
            user.setCreatedOn(LocalDateTime.now());

            userRepository.save(user);


            return true;

        }


        public User login(LoginDto loginDto) {

            Optional<User> existingUser = this.userRepository.findByUsername(loginDto.getUsername());

            if (existingUser.isEmpty()) {
                throw new RuntimeException("Not exist");

            }
            if (!passwordEncoder.matches(loginDto.getPassword(), existingUser.get().getPassword())) {
                throw new RuntimeException("Invalid password or username!");
            }
            return existingUser.get();
        }

        public User byId(UUID id) {
            Optional<User> userById = this.userRepository.findById(id);

            if (userById.isEmpty()) {
                throw new RuntimeException("Not exsist");


            }
            return userById.get();
        }

        public List<Subscription> getClentSubs(UUID id){

            return this.userRepository.findUserSubs(id);
        }

        public boolean renew(HttpSession httpSession, SubscriptionDtos subscriptionDtos) {

            UUID userId=(UUID)httpSession.getAttribute("userId");
            Optional<User> user=userRepository.findById(userId);
            if(user.isEmpty()){
                return false;

            }
            //chek if is not expired-comapre dates;
            //check if its enough funds;


            Subscription subscription= this.userRepository.findUserSubs(userId)
                    .stream().filter(s->s.getId().equals(subscriptionDtos.getId())).findFirst().orElse(null);
                if(subscription == null){

                    return false;
                }
                subscription.setActive(true);
                subscription.setCreatedOn(LocalDateTime.now());
                subscription.setExpiresOn(createSubscriptionPeriod(subscription));

            return true;

        }

    public void buySubscription(HttpSession httpSession, UserCardDto cardDto, UUID id) {
        if (httpSession.getAttribute("userId") == null) {
            throw new RuntimeException("You need to have account!");
        }

        UUID userId = (UUID) httpSession.getAttribute("userId");
       Optional<User>byId =userRepository.findById(userId);
       if(byId.isEmpty()){
           throw new RuntimeException("User dost exist!");
       }
        //chek if is not expired-comapre dates;
        //check if its enough funds;
      User user=byId.get();
        Subscription subscription = subscriptionService.byId(id);

        subscription.setCreatedOn(LocalDateTime.now());
        LocalDateTime expire=createSubscriptionPeriod(subscription);
        subscription.setExpiresOn(expire);

        List<Subscription> userSubscriptions = new ArrayList<>(userRepository.findUserSubs(userId));
        userSubscriptions.add(subscription);
        user.setSubscriptions(userSubscriptions);
        this.userRepository.save(user);


    }

   public LocalDateTime createSubscriptionPeriod( Subscription subscription) {

          LocalDateTime expiresOn=LocalDateTime.now();
        if (subscription.getNamePackage().contains("Montly")) {
            expiresOn=LocalDateTime.now().plusMonths(1);
        } else if (subscription.getNamePackage().contains("Year")) {
            expiresOn=LocalDateTime.now().plusYears(1);
        } else if (subscription.getNamePackage().contains("6 Month")) {
            expiresOn=LocalDateTime.now().plusMonths(6);

        }

        return expiresOn;
    }
}







