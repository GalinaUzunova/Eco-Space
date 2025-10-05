package org.ecospace.config;

import org.ecospace.security.SessionInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 @Configuration
public class WebConfiguration implements WebMvcConfigurer {
  @Autowired
    private  SessionInterceptor interseptor;



     @Override
     public void addInterceptors(InterceptorRegistry registry) {
         registry.addInterceptor(interseptor)
                 .addPathPatterns("/**")
                 .excludePathPatterns("/css/**",  "/js/**", "/images/**");

     }
 }
