package com.demo.interceptor.RequestInterceptor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@SpringBootApplication
@Configuration
public class RequestInterceptorApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequestInterceptorApplication.class, args);
	}
    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(new HttpRetryInterceptor(restTemplate)));
        return restTemplate;
    }

}
