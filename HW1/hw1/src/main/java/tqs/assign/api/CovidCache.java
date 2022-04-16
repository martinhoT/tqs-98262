package tqs.assign.api;

import org.springframework.stereotype.Component;
import tqs.assign.data.ResponseData;

import java.util.List;

@Component
public class CovidCache {

    public void store(ResponseData response, String requestMethod, List<String> requestArgs) {
        // TODO
    }

    public ResponseData get(String requestMethod, List<String> requestArgs) {
        // TODO
        return null;
    }

    public boolean stale(String requestMethod, List<String> requestArgs) {
        // TODO
        return false;
    }

}
