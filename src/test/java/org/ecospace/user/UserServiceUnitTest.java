package org.ecospace.user;

import org.ecospace.exception.UserNotFoundException;
import org.ecospace.model.Product;
import org.ecospace.model.User;
import org.ecospace.model.dto.ProfileDto;
import org.ecospace.model.dto.UserDto;
import org.ecospace.repository.ProductRepository;
import org.ecospace.repository.UserRepository;
import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.SubscriptionServiceImpl;
import org.ecospace.service.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SubscriptionServiceImpl subscriptionService;
    @Mock
    private ProductRepository productRepository;

    @Mock
    private AuthenticationMetadata authenticationMetadata;


    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void userRegister_WhenNoUsersExist_ShouldCreateAdminUser() {

        UserDto userDto = new UserDto("Nik", "nik@abv", "123", "123");
        when(userRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");

        userService.userRegister(userDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("ADMIN", savedUser.getRole().name());
        assertEquals("Nik", savedUser.getUsername());
        assertEquals("nik@abv", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
    }

    @Test
    void userRegister_WhenUsersExist_ShouldCreateClientUser() {

        UserDto userDto = new UserDto("John", "john@abv", "456", "456");
        when(userRepository.count()).thenReturn(5L);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword456");

        userService.userRegister(userDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("CLIENT", savedUser.getRole().name());
        assertEquals("John", savedUser.getUsername());
    }


    @Test
    void getClientSubs_WhenUserExistsAndHasSubscriptions_ThenReturnListOfSubs() {

        UUID userId = UUID.randomUUID();
        List<Product> expectedSubscriptions = Arrays.asList(
                Product.builder().namePackage("Product1").build(),
                Product.builder().namePackage("Product2").build()
        );
        User user = User.builder().username("Nik")
                .build();


        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findUserSubs(userId)).thenReturn(expectedSubscriptions);


        List<Product> result = userService.getClientSubs(userId);


        assertEquals(expectedSubscriptions, result);
        verify(userRepository).findById(userId);
        verify(userRepository, times(2)).findUserSubs(userId);
    }

    @Test
    void getClientSubscriptions_whenClientNotExsist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getClientSubs(userId));


    }

    @Test
    void userEditProfile_whenUserIsAuthenticated_andUserImgIsNotNullOrEmpty_thenUpdateProfile() {

        UUID userId = UUID.randomUUID();
        ProfileDto dto = new ProfileDto(userId, "Nik",
                "nik@abv",
                "www.unsplash",
                "0845222");
        User userRetrieveFromDb = User.builder()
                .email("zak@bg")
                .username("Niko")
                .build();
        when(authenticationMetadata.getId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userRetrieveFromDb));

        userService.editProfile(dto, authenticationMetadata);

        assertEquals("Nik", userRetrieveFromDb.getUsername());
        assertEquals("nik@abv", userRetrieveFromDb.getEmail());
        assertEquals("www.unsplash", userRetrieveFromDb.getImage());
        assertEquals("0845222", userRetrieveFromDb.getPhone());
        verify(userRepository).save(any(User.class));
    }


    @Test
    void editUserProfile_whenUserIdNotFound_thenThrowException(){

        UUID userId=UUID.randomUUID();
        ProfileDto dto = new ProfileDto(userId, "Nik",
                "nik@abv",
                "www.unsplash",
                "0845222");

        when(authenticationMetadata.getId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,()-> userService.editProfile(dto,authenticationMetadata));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }


}

