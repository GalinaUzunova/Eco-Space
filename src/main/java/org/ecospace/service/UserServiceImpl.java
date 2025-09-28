package org.ecospace.service;

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
import java.util.Optional;

@Service

public class UserServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


     @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;

         this.passwordEncoder = passwordEncoder;
     }

    @Transactional
    public  boolean userRegister(UserDto userDto){

        Optional<User>byUsernameAndEmail=this.userRepository.findByUsernameAndEmail(userDto.getUsername(), userDto.getEmail());


        if(byUsernameAndEmail.isPresent()){

            return false;
        }


        User user=new User();
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setActive(true);
        user.setRole(UserRole.CLIENT);
        user.setCreatedOn(LocalDateTime.now());
        user.setRenew(false);
        user.setSubscriptionType(null);
        user.setSubscriptionType(userDto.getSubscriptionType());
        user.setClientProjects(new ArrayList<>());


      userRepository.save(user);


        return true;

    }

    public  boolean isUnique(UserDto userDto){

         Optional<User>byUsername=this.userRepository.findByUsername(userDto.getUsername());
         if(byUsername.isPresent()){
             return false;
         }
         return true;
    }

    public boolean login(LoginDto loginDto) {

         Optional<User>existingUser=this.userRepository.findByUsername(loginDto.getUsername());

         if(existingUser.isEmpty()){
             return false;

         }
         if(!passwordEncoder.matches(loginDto.getPassword(),existingUser.get().getPassword())){
             return false;
        }
         return true;
    }
}
