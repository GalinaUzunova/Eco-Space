package org.ecospace.service;

import org.ecospace.exception.AccesDeniedException;
import org.ecospace.exception.ProductNotFound;
import org.ecospace.exception.UserNotFoundException;
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

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void buyProduct(@AuthenticationPrincipal AuthenticationMetadata authenticationMetadata, UserCardDto cardDto, UUID id) {

        UUID userId = authenticationMetadata.getId();
        Optional<User> byId = userRepository.findById(userId);
        if (byId.isEmpty()) {
            throw new UserNotFoundException("Not Authorized operation");
        }

        User user = byId.get();
        Subscription subscription = subscriptionService.byId(id);
        Product product = new Product();
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
    public void editProfile(ProfileDto profileDto,  @AuthenticationPrincipal AuthenticationMetadata authenticationPrinciple) {

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


}








