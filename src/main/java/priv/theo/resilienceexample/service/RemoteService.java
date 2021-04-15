package priv.theo.resilienceexample.service;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import priv.theo.resilienceexample.bean.Product;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class RemoteService {

    @RateLimiter(name = "backendA", fallbackMethod = "fallback")
    public List<Product> rateLimiterMethod() {
        log.info("invoke remote service!");

        return Arrays.asList(new Product("1", "1"), new Product("2", "2"));
    }

    private List<Product> fallback(RequestNotPermitted ex) {
        log.info("rate limiter fallback method.", ex);
        return Collections.emptyList();
    }

    public List<Product> searchProduct() {
        int i = ThreadLocalRandom.current().nextInt(1, 5);
        if (i > 2) {
            log.info("invoke remote method failed");
            throw new RuntimeException("invoke remote method failed");
        }
        log.info("invoke remote method success");
        return Arrays.asList(new Product("1", "1"), new Product("2", "2"));
    }

        @Bulkhead(name = "backendA", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "fallback")
//    @CircuitBreaker(name = "backendA", fallbackMethod = "listUserFallBack")
    public List<Product> searchProductWithAop() {
//        int i = ThreadLocalRandom.current().nextInt(1, 5);
//        if (i > 2) {
//            log.info("invoke remote method failed");
//            throw new RuntimeException();
//        }
        log.info("invoke remote method success");
        return Arrays.asList(new Product("1", "1"), new Product("2", "2"));
    }


    private List<Product> fallback(Throwable throwable) {
        log.info(throwable.getLocalizedMessage() + ",方法降级了");

        return Arrays.asList(new Product("0", "0"));
    }

    private List<Product> fallback(BulkheadFullException e) {
        log.info("bulkhead full");
        return Collections.emptyList();
    }

    private List<Product> fallback(CallNotPermittedException e) {
        log.info("circuit open");
        return Collections.emptyList();
    }

    public List<Product> retryMethodWithoutAop() {
        int i = ThreadLocalRandom.current().nextInt(1, 5);
        if (i > 3) {
            throw new RuntimeException("invoke remote service failed");
        }
        log.info("invoke remote service success.");
        return Collections.emptyList();
    }

    @Retry(name = "backendA", fallbackMethod = "fallback")
    @CircuitBreaker(name = "backendA", fallbackMethod = "fallback")
    public List<Product> retryAndCircuitBreakerMethodWithAop() {

        int i = ThreadLocalRandom.current().nextInt(1, 5);
        if (i > 3) {
            throw new RuntimeException("invoke remote service failed");
        }
        log.info("invoke remote service success.");
        return Collections.emptyList();
    }

    private List<Product> fallback() {

        return Collections.emptyList();
    }
}
