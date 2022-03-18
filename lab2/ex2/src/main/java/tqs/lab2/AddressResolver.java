package tqs.lab2;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import tqs.lab2.serialization.MapQuest;

public class AddressResolver {

    private final String mainUrlFormat = "http://open.mapquestapi.com/geocoding/v1/reverse?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&location=%f,%f&includeRoadMetadata=true";
    private final Gson gson = new Gson();
    
    private final ISimpleHttpClient httpClient;

    public AddressResolver(ISimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Optional<Address> findAddressForLocation(double lat, double lon) {
        if (lat < -90.0 || lat > 90.0 || lon < -180.0 || lon > 180.0)
            throw new IllegalArgumentException("The values for latitude/longitude are out of range! (lat: [-90,90], lon: [-180,180])");

        String jsonResponse;
        try {
            jsonResponse = httpClient.doHttpGet(String.format(Locale.US, mainUrlFormat, lat, lon));
        } catch (IOException e) {
            System.err.println("IOException when communicating with external service.");
            return Optional.empty();
        } catch (InterruptedException e) {
            System.err.println("InterruptedException when communicating with external service.");
            return Optional.empty();
        }

        MapQuest mapQuest;
        try {
            mapQuest = gson.fromJson(jsonResponse, MapQuest.class);
        } catch (JsonSyntaxException e) {
            System.err.println("The response has an invalid structure, could not extract Address object: " + jsonResponse);
            return Optional.empty();
        }
        
        if (mapQuest.getResults().size() == 0) {
            System.err.println("The response has 0 results: " + jsonResponse);
            return Optional.empty();
        }

        if (mapQuest.getResults().get(0).getLocations().size() == 0) {
            System.err.println("The response has 0 result locations: " + jsonResponse);
            return Optional.empty();
        }

        return Optional.ofNullable( mapQuest.getResults().get(0).getLocations().get(0).toAddress() );
    }

}
