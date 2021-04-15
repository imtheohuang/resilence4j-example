package priv.theo.resilienceexample.service;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
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
public class BulkheadService {
    @Autowired
    private BulkheadRegistry bulkheadRegistry;

    @Autowired
    private RemoteService remoteService;

    public List<Product> bulkheadWithoutAop() {

        Bulkhead bulkhead = bulkheadRegistry.bulkhead("backendA");
//        List<Product> products = bulkhead.executeSupplier(remoteService::searchProduct);
        Supplier<List<Product>> listSupplier = Bulkhead.decorateSupplier(bulkhead, remoteService::searchProductWithAop);

        Try<List<Product>> result = Try.ofSupplier(listSupplier)
                .recover(BulkheadFullException.class, e -> {
                    log.info("bulkhead full {}", e.getLocalizedMessage());
                    return Collections.emptyList();
                })
                .recover(throwable -> {
                    log.info("服务降级");
                    return Collections.emptyList();
                });
        return result.get();
    }
}
