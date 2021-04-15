package priv.theo.resilienceexample.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.theo.resilienceexample.bean.Product;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
public class RetryService {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RetryRegistry retryRegistry;

    @Autowired
    private RemoteService remoteService;
    public List<Product> retryWithoutAop() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("backendA");

        Retry retry = retryRegistry.retry("backendA");
        Supplier<List<Product>> retrySupplier = Retry.decorateSupplier(retry, remoteService::retryMethodWithoutAop);
        Supplier<List<Product>> circuitBreakerSupplier = CircuitBreaker.decorateSupplier(circuitBreaker,retrySupplier);
        List<Product> products = Try.ofSupplier(circuitBreakerSupplier)
                .recover(CallNotPermittedException.class, e -> {
                    log.info("circuitBreaker is open");
                    return Collections.emptyList();
                })
                .recover(throwable -> {
                    log.info("retry failed", throwable);
                    return Collections.emptyList();
                })
                .get();
        return products;
    }

}
