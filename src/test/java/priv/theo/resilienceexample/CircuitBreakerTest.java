package priv.theo.resilienceexample;

import io.github.resilience4j.circuitbreaker.utils.CircuitBreakerUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import priv.theo.resilienceexample.service.CircuitBreakerService;

/**
 * @author theohuang
 * @date 2021/4/15
 */
@SpringBootTest(classes = ResilienceExampleApplication.class)
public class CircuitBreakerTest {

    @Autowired
    private CircuitBreakerService mCircuitBreakerService;
    @Test
    public void test() {
        for (int i = 0; i < 5; i++) {
            new Thread(() -> mCircuitBreakerService.circuitBreakerWithoutAop()).start();
        }

    }
}
