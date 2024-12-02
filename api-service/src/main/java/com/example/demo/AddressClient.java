package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AddressClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AddressClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${addressClient.baseUrl}") String baseUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.baseUrl = baseUrl;
    }

    Address getAddressForCustomerId(long id) {
        return restTemplate.getForObject(String.format("%s/addresses/%d", baseUrl, id), Address.class);
    }

}
