package com.example.demo;

import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.baggage.BaggageEntry;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@RestController
public class Controller {

    private CustomerClient customerClient;

    private AddressClient addressClient;

    private final Tracer tracer;

    private Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    public Controller(CustomerClient customerClient, AddressClient addressClient, Tracer tracer) {
        this.customerClient = customerClient;
        this.addressClient = addressClient;
        this.tracer = tracer;
    }

    @NewSpan("newspan")
    @GetMapping(path = "oldcustomers/{id}")
    public CustomerAndAddress getOldCustomerWithAddress(@SpanTag("spantag") @PathVariable("id") long customerId) {
        var usecase = "old-customers";
        addUsecaseToSpan(usecase);
        return withUseCaseBaggage(usecase, CustomerAndAddress.class, () -> {
            logger.info("COLLECTING OLD CUSTOMER AND ADDRESS WITH ID {} FROM UPSTREAM SERVICE", customerId);
            Customer customer = customerClient.getCustomer(customerId);
            Address address = addressClient.getAddressForCustomerId(customerId);
            return new CustomerAndAddress(customer, address);
        });
    }

    @GetMapping(path = "customers/{id}")
    public CustomerAndAddress getCustomerWithAddress(@PathVariable("id") long customerId) {
        var usecase = "customers";
        addUsecaseToSpan(usecase);
        return withUseCaseBaggage(usecase, CustomerAndAddress.class, () -> {
            logger.info("COLLECTING CUSTOMER AND ADDRESS WITH ID {} FROM UPSTREAM SERVICE", customerId);
            Customer customer = customerClient.getCustomer(customerId);
            Address address = addressClient.getAddressForCustomerId(customerId);
            return new CustomerAndAddress(customer, address);
        });
    }

    private void addUsecaseToSpan(String usecase) {
        var span = tracer.currentSpan();
        if (span != null) {
            span.tag("use-case-tag", usecase);
        }
    }

    private <T> T withUseCaseBaggage(String usecase, Class<T> clazz, Supplier<T> supplier) {
        try (var ignored = this.tracer.createBaggageInScope("use-case-baggage", usecase)) {
            return supplier.get();
        }
    }
}
