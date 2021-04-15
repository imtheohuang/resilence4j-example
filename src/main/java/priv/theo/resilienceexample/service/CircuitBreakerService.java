package priv.theo.resilienceexample.service;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import priv.theo.resilienceexample.bean.Product;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Slf4j
@Service
public class CircuitBreakerService {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    @Autowired
    private TimeLimiterRegistry timeLimiterRegistry;
    @Autowired
    private RemoteService remoteService;

    public List<Product> circuitBreakerWithoutAop() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("backendA");

        Supplier<List<Product>> listSupplier = circuitBreaker.decorateSupplier(remoteService::searchProduct);

//        Supplier<List<Product>> listSupplier1 = CircuitBreaker.decorateSupplier(circuitBreaker, remoteService::searchProduct);
        // List<Product> products = listSupplier.get();

        Try<List<Product>> result = Try.ofSupplier(listSupplier)
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

    public List<Product> circuitBreakerAndTimeLimiter() {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("backendA");
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter("backendA");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Supplier<Future<List<Product>>> futureSupplier = () -> executorService.submit(remoteService::searchProduct);

        Callable<List<Product>> timeLimiterCallable = timeLimiter.decorateFutureSupplier(futureSupplier);
        Callable<List<Product>> circuitBreakerCallable = circuitBreaker.decorateCallable(timeLimiterCallable);
        List<Product> result = Try.ofCallable(circuitBreakerCallable)
                .recover(CallNotPermittedException.class, e -> {
                    log.info("circuit breaker open!");
                    return Collections.emptyList();
                })
                .recover(throwable -> {
                    log.info("fall back");
                    return Collections.emptyList();
                })
                .get();
        return result;
    }


}
