package com.demo.interceptor.RequestInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("hello")
    public String testRestTemplate(){

        return this.restTemplate.exchange("http://localhost:8081/users", HttpMethod.GET, null, String.class).getBody();
    }

    @GetMapping("helloworld")
    public String testRest2Template(){
        
        return "Hello";
    }

}