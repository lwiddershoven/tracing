package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

@Component
public class CustomerClient {

	private static final Logger logger = LoggerFactory.getLogger(CustomerClient.class);
	private RestTemplate restTemplate;
	private String baseUrl;

	public CustomerClient(
			RestTemplateBuilder restTemplateBuilder,
			@Value("${customerClient.baseUrl}") String baseUrl) {
		this.restTemplate = restTemplateBuilder.build();
		this.baseUrl = baseUrl;
	}

	Customer getCustomer(@PathVariable("id") long id){
		String url = String.format("%s/customers/%d", baseUrl, id);
		return restTemplate.getForObject(url, Customer.class);
	}

}
