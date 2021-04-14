package priv.theo.resilienceexample.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class RemoteService {

    @CircuitBreaker(name = "backendA", fallbackMethod = "listUserFallBack")
    public List<String> listUser() {
        return Collections.emptyList();
    }


    private List<String> listUserFallBack(Throwable throwable) {
      log.info(throwable.getLocalizedMessage() + ",方法降级了");

        return Collections.emptyList();
    }

    private List<String> listUserFallBack(CallNotPermittedException e) {
        log.info("circuit open");
        return Collections.emptyList();
    }
}
