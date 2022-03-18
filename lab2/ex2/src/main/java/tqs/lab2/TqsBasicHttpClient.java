package tqs.lab2;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TqsBasicHttpClient implements ISimpleHttpClient {

    private final HttpClient httpClient;

    public TqsBasicHttpClient() {
        httpClient = HttpClient.newHttpClient();
    }

    @Override
    public String doHttpGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .GET()
            .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
    
}
