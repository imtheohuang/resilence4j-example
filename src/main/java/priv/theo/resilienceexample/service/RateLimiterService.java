package priv.theo.resilienceexample.service;

import io.github.resilience4j.core.exception.AcquirePermissionCancelledException;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
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
public class RateLimiterService {

    @Autowired
    private RateLimiterRegistry rateLimiterRegistry;
    @Autowired
    private RemoteService remoteService;

    public List<Product> rateLimiterWithoutAop() {
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("backendA");
        Supplier<List<Product>> listSupplier = RateLimiter.decorateSupplier(rateLimiter, remoteService::searchProduct);

        List<Product> products = Try.ofSupplier(listSupplier)
                .recover(RequestNotPermitted.class, e -> {
                    log.info("rate limiter: request not permitted. {}", e.getLocalizedMessage());
                    return Collections.emptyList();
                })
                .get();

        return products;
    }
}
