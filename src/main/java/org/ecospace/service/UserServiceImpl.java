package org.ecospace.service;

import jakarta.servlet.http.HttpSession;
import org.ecospace.model.Product;
import org.ecospace.model.Subscription;
import org.ecospace.model.User;
import org.ecospace.model.UserRole;
import org.ecospace.model.dto.UserCardDto;
import org.ecospace.model.dto.UserDto;
import org.ecospace.repository.ProductRepository;
import org.ecospace.repository.UserRepository;
import org.ecospace.security.AuthenticationMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserDetailsService {



        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final SubscriptionServiceImpl subscriptionService;
        private final ProductRepository productRepository;





        @Autowired
        public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, SubscriptionServiceImpl subscriptionService, ProductRepository productRepository) {
            this.userRepository = userRepository;

            this.passwordEncoder = passwordEncoder;

            this.subscriptionService = subscriptionService;

            this.productRepository = productRepository;
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
            user.setCreatedOn(LocalDateTime.now());



            if(this.userRepository.count()<=0){
                user.setRole(UserRole.ADMIN);
            }else {
                user.setRole(UserRole.CLIENT);
            }

            userRepository.save(user);


            return true;

        }




        public User byId(UUID id) {
            Optional<User> userById = this.userRepository.findById(id);

            if (userById.isEmpty()) {
                throw new RuntimeException("Not exsist");


            }
            return userById.get();
        }

        public List<Product> getClentSubs(UUID id){

            return this.userRepository.findUserSubs(id);
        }
        @Transactional
        public boolean renew(@AuthenticationPrincipal AuthenticationMetadata authenticationPriciple, UserCardDto cardDto, UUID id) {


            Optional<User> user=userRepository.findById(authenticationPriciple.getId());
            if(user.isEmpty()){
                return false;

            }
            //chek if is not expired-comapre dates;
            //check if its enough funds;


          Product product= this.userRepository.findUserSubs(authenticationPriciple.getId())
                    .stream().filter(p->p.getId().equals(id)).findFirst().orElse(null);
                if(product == null){

                    return false;
                }
                product.setActive(true);
                product.setCreatedOn(LocalDateTime.now());
                product.setExpired(createSubscriptionPeriod(product.getNamePackage()));


            return true;

        }
 @Transactional
    public void buyProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationPriciple, UserCardDto cardDto, UUID id) {


        UUID userId = authenticationPriciple.getId();
       Optional<User>byId =userRepository.findById(userId);
       if(byId.isEmpty()){
           throw new RuntimeException("User dost exist!");
       }
        //chek if is not expired-comapre dates;
        //check if its enough funds;
      User user=byId.get();

       Subscription subscription = subscriptionService.byId(id);

      Product product=new Product();

        product.setNamePackage(subscription.getNamePackage());
        product.setPrice(subscription.getPrice());
        product.setDescription(subscription.getDescription());
        product.setCreatedOn(LocalDateTime.now());
        product.setType(subscription.getType());

        product.setExpired(createSubscriptionPeriod(subscription.getNamePackage()));
        product.setActive(true);

        this.productRepository.save(product);





        List<Product> productList = new ArrayList<>(userRepository.findUserSubs(userId));
        productList.add(product);

        user.setProductList(productList);
        this.userRepository.save(user);


    }

   public LocalDateTime createSubscriptionPeriod( String packageName) {

          LocalDateTime expiresOn=LocalDateTime.now();
        if (packageName.contains("Monthly")) {
            expiresOn=LocalDateTime.now().plusMonths(1);
        } else if (packageName.contains("Year")) {
            expiresOn=LocalDateTime.now().plusYears(1);
        } else if (packageName.contains("6-Month")) {
            expiresOn=LocalDateTime.now().plusMonths(6);

        }
        expiresOn= expiresOn.with(LocalTime.MAX);

        return expiresOn;
    }

    public List<User>getAllUsersAndSubs(){
            if( this.userRepository.findAllByAndProductList()!=null){
                return  this.userRepository.findAllByAndProductList();
            }

       return new ArrayList<>();
    }

    public List<User>getAllUsers(){
            List<User>allUsers=this.userRepository.getAllBy();
        return Objects.requireNonNullElseGet(allUsers, ArrayList::new);

    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

            User user=this.userRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("User doesn't exist"));
        return new AuthenticationMetadata(user.getUsername(),user.getPassword(),user.getId(),user.getRole(),user.isActive());
    }
}







