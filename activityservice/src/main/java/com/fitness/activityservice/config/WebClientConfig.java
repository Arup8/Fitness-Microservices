package com.fitness.activityservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean // this is we declared so it can be autowired easily & we can use it anywhere
    @LoadBalanced // why we used this cause in eureka each service calls other service by their name not ip address
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

//    This is basically used to expose the user service so we can use it in this activity service and also we declared this @Bean so we can use it in everywhere in this service
    // so from now we can call apis of this user service
    @Bean
    public WebClient userServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl("http://USER-SERVICE")
                .build();
    }
}
