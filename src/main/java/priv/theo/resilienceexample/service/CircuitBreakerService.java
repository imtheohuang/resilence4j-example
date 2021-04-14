package priv.theo.resilienceexample.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Service
public class CircuitBreakerService {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RemoteService remoteService;

    public List<String> circuitBreakerWithoutAop() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("backendA");


        Supplier<List<String>> listSupplier = circuitBreaker.decorateSupplier(remoteService::listUser);

        Try<List<String>> result = Try.ofSupplier(listSupplier)
                .recover(CallNotPermittedException.class, e -> {
                    log.info("circuit breaker open!");
                    return Collections.emptyList();
                })

                .recover(throwable -> {
                    log.info("方法降级");
                    return Collections.emptyList();
                });

        return result.get();


    }
}
