package org.ecospace.service;

import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.UserRole;
import org.ecospace.model.dto.LoginDto;
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

        return userById.get();
    }


}
