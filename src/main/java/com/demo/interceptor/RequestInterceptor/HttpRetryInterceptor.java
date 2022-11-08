package com.demo.interceptor.RequestInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;

@RequiredArgsConstructor
@Component
@Slf4j
public class HttpRetryInterceptor implements ClientHttpRequestInterceptor {

    private static final int MAX_RETRY_COUNT = 3;
    private final RestTemplate restTemplate;
    private static final String X_RETRY_ATTEMPT = "X-RETRY-ATTEMPT";
    private static final long EXPONENTIAL_BACK_OFF_TIME = 100;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

        try{
            ClientHttpResponse clientHttpResponse = execution.execute(request, body);
            if (clientHttpResponse.getStatusCode() != HttpStatus.OK) {
                return retryOnFailure(request, body, execution);
            }
            return clientHttpResponse;
        }catch (ConnectException r){
            return retryOnFailure(request, body, execution);
        }

    }

    private ClientHttpResponse retryOnFailure(HttpRequest request, byte[] body, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
        int retryCount = getCurrentRetryCount(request.getHeaders());
        ClientHttpRequest clientHttpRequest = restTemplate.getRequestFactory().createRequest(request.getURI(), request.getMethod());
        clientHttpRequest.getHeaders().set(X_RETRY_ATTEMPT, String.valueOf(retryCount+1));

        ClientHttpResponse clientHttpResponse;

        try{
            long waitTime = EXPONENTIAL_BACK_OFF_TIME * (long) Math.pow(Math.E, retryCount);
            log.info("Waiting for {}ms time before attempting attempt number {}.", waitTime, retryCount+1);
            Thread.sleep(waitTime);
            log.info("Attempting to {} retry.", retryCount + 1);
            clientHttpResponse = clientHttpRequestExecution.execute(clientHttpRequest, body);
            log.info("In {} attempt, received response code as {}.", retryCount+1, clientHttpResponse.getStatusCode());
            if(clientHttpResponse.getStatusCode() != HttpStatus.OK){
                if(retryCount < MAX_RETRY_COUNT-1){
                    return retryOnFailure(clientHttpRequest, body, clientHttpRequestExecution);
                } else{
                    throw new IOException("Exceeded max retry attempt for url=" + request.getURI() + " .");
                }
            } else{
                return clientHttpResponse;
            }
        }catch (Exception e){
            log.info("In {} attempt, received exception {}.", retryCount+1, e.getMessage());
            if(retryCount < MAX_RETRY_COUNT-1){
                return retryOnFailure(clientHttpRequest, body, clientHttpRequestExecution);
            } else{
                throw new IOException("Exceeded max retry attempt for url=" + request.getURI() + " .");
            }
        }
    }

    private int getCurrentRetryCount(HttpHeaders httpHeaders) {

        if (httpHeaders.containsKey(X_RETRY_ATTEMPT) && !CollectionUtils.isEmpty(httpHeaders.get(X_RETRY_ATTEMPT))) {
            return Integer.parseInt(httpHeaders.getFirst(X_RETRY_ATTEMPT));
        }
        return 0;
    }

}