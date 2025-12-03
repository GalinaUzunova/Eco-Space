package org.ecospace.web;

import org.ecospace.init.CustomAuthenticationSuccessHandler;
import org.ecospace.model.*;
import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.SubscriptionServiceImpl;
import org.ecospace.service.UserServiceImpl;
import org.ecospace.web.controller.ManagerController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;



import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ManagerController.class)
public class ManagerControllerApiTest {


    @MockitoBean
    private UserServiceImpl userService;

    @MockitoBean
    private SubscriptionServiceImpl subscriptionService;
    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;


    @Autowired
    private MockMvc mockMvc;


    @Test
    void getManagerEndpoint_shouldReturnManagerPageSuccessfully() throws Exception {

        User user = manager();

        AuthenticationMetadata authenticationMetadata = new AuthenticationMetadata(
                user.getUsername(),
                user.getPassword(),
                user.getId(),
                user.getRole(),
                user.isActive()
        );
        when(userService.getAdmin(any(AuthenticationMetadata.class))).thenReturn(user);


        mockMvc.perform(get("/manager")
                        .with(user(authenticationMetadata)))
                .andExpect(view().name("user"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void getManagerEndpoint_withClientRole_shouldBeForbidden() throws Exception {
        User user = client();
        AuthenticationMetadata authenticationMetadata = new AuthenticationMetadata(
                user.getUsername(),
                user.getPassword(),
                user.getId(),
                user.getRole(),
                user.isActive()
        );
        MockHttpServletRequestBuilder httpRequest = get("/")
                .with(user(authenticationMetadata));

        mockMvc.perform(httpRequest)
                .andExpect(view().name("index"))
                .andExpect(status().isOk());

    }
    @Test

    void getSubscriptionEndPoint_shouldReturnSuccess() throws Exception {

        User user = manager();
        List<User>allUsers=users();

        AuthenticationMetadata authenticationMetadata = new AuthenticationMetadata(
                user.getUsername(),
                user.getPassword(),
                user.getId(),
                user.getRole(),
                user.isActive()
        );
        when(userService.getAdmin(any(AuthenticationMetadata.class))).thenReturn(user);
        when(userService.getAllUsersAndSubs()).thenReturn(allUsers);

        MockHttpServletRequestBuilder httpRequest=get("/subscriptions")
                .with(user(authenticationMetadata));


        mockMvc.perform(httpRequest)
                .andExpect(view().name("subscriptions"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("allUsers",allUsers));


    }


    public static User client() {
        UUID userId = UUID.randomUUID();
        User client=new User();
        client.setRole(UserRole.CLIENT);
        client.setUsername("NinaLina");
        client.setPhone("0845002097");
        client.setEmail("gbuzunova13@gmail.com");
        client.setPassword("1234567");
        client.setActive(true);
        client.setCreatedOn(LocalDateTime.now());
        client.setImage("/images/default-user.jpg");
        client.setId(userId);

      UUID subsId=UUID.randomUUID();
        Subscription subscription = Subscription.builder()
                .type(SubscriptionType.MAINTANACE)
                .price(1500.00)
                .namePackage("Monthly")
                .description("Monthly service")
                .build();
        subscription.setId(subsId);

        Product product = Product.builder()
                .price(1500.00)
                .description("Monthly mainatnace")
                .user(client)
                .createdOn(LocalDateTime.now())
                .expired(LocalDateTime.now())
                .type(SubscriptionType.MAINTANACE)
                .build();


        client.setProductList(List.of(product));

        return client;
    }

    public static User manager() {

        UUID userId = UUID.randomUUID();

        User manager=new User();
        manager.setRole(UserRole.ADMIN);
        manager.setUsername("Gina");
        manager.setPhone("0845002097");
        manager.setEmail("gbuzunova13@gmail.com");
        manager.setPassword("1234567");
        manager.setActive(true);
        manager.setCreatedOn(LocalDateTime.now());
        manager.setImage("/images/default-user.jpg");
        manager.setId(userId);

        Subscription subscription = Subscription.builder()
                .type(SubscriptionType.MAINTANACE)
                .price(1500.00)
                .namePackage("Monthly")
                .description("Monthly service")
                .build();


        manager.setSubscriptions(List.of(subscription));


        return manager;
    }

    public  static List<User>users(){
        User user1=client();

        return List.of(user1);

    }

}


