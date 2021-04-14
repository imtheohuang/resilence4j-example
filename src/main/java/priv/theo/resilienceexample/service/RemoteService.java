package priv.theo.resilienceexample.service;

import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class RemoteService {

    List<String> listUser() {
        return Collections.emptyList();
    }
}
