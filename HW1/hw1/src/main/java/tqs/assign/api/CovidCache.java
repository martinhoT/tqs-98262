package tqs.assign.api;

import org.springframework.stereotype.Component;
import tqs.assign.data.ResponseData;

@Component
public class CovidCache {

    public void store(String requestMethod, ResponseData response) {
        // TODO
    }

    public ResponseData get(String requestMethod) {
        // TODO
        return null;
    }

    public boolean stale(String requestMethod) {
        // TODO
        return false;
    }

}
