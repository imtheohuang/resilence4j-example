package priv.theo.resilienceexample;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import priv.theo.resilienceexample.service.BulkheadService;
import priv.theo.resilienceexample.service.CircuitBreakerService;
import priv.theo.resilienceexample.service.RateLimiterService;
import priv.theo.resilienceexample.service.RemoteService;

/**
 * @author theohuang
 * @date 2021/4/15
 */
@SpringBootTest(classes = ResilienceExampleApplication.class)
public class Resilience4jTest {

    @Autowired
    private CircuitBreakerService circuitBreakerService;

    @Autowired
    private BulkheadService bulkheadService;

    @Autowired
    private RateLimiterService limiterService;


    @Autowired
    private RemoteService remoteService;

    @Test
    public void testCircuitBreakerRemoteMethodWithoutAop() {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> circuitBreakerService.circuitBreakerWithoutAop()).start();
        }

    }

    @Test
    public void testCircuitBreakerRemoteMethodWithAop() {
        for (int i = 0; i < 30; i++) {
            new Thread(remoteService::searchProduct).start();
        }
    }

    @Test
    public void testBulkheadRemoteMethodWithoutAop() {
        for (int i = 0; i < 30; i++) {
            new Thread(() -> bulkheadService.bulkheadWithoutAop()).start();
        }

    }

    @Test
    public void testBulkheadRemoteMethodWithAop() {
        for (int i = 0; i < 30; i++) {
            new Thread(() -> remoteService.searchProductWithAop()).start();
        }

    }

    @Test
    public void testRateLimiterMethodWithoutAop() {
        for (int i = 0; i < 10; i++) {
            new Thread(limiterService::rateLimiterWithoutAop).start();
        }
    }

}
