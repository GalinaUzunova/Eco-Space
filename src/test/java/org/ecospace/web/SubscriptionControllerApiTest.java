package org.ecospace.web;

import org.ecospace.init.CustomAuthenticationSuccessHandler;
import org.ecospace.model.Subscription;
import org.ecospace.model.SubscriptionType;
import org.ecospace.model.User;
import org.ecospace.model.UserRole;
import org.ecospace.model.dto.AddSubDto;
import org.ecospace.security.AuthenticationMetadata;
import org.ecospace.service.SubscriptionServiceImpl;
import org.ecospace.web.controller.SubscriptionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubscriptionController.class)
public class SubscriptionControllerApiTest {
    @MockitoBean
    private SubscriptionServiceImpl subscriptionService;

    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @Autowired
    private MockMvc mockMvc;


    @Test
    void  postAddNewSubscription_shouldReturn302AndRedirectToMangerPage() throws Exception {
        User user = manager();

        AuthenticationMetadata authenticationMetadata = new AuthenticationMetadata(
                user.getUsername(),
                user.getPassword(),
                user.getId(),
                user.getRole(),
                user.isActive()
        );

        doNothing().when(subscriptionService).addNewSubscription(any(AddSubDto.class));

        MockHttpServletRequestBuilder httpRequest= MockMvcRequestBuilders.post("/add-subscription")
                .formField("type","DESIGN")
                .formField("namePackage","Monthly")
                .formField("price","1200.00")
                .formField("description","maintenance")
                .with(csrf())
                .with(user(authenticationMetadata));

        mockMvc.perform(httpRequest)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manager"))
                .andDo(print());

        verify(subscriptionService).addNewSubscription(any(AddSubDto.class));


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


}
