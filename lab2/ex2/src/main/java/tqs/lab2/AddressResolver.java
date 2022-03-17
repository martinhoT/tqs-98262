package tqs.lab2;

import java.util.Optional;

public class AddressResolver {

    private ISimpleHttpClient httpClient;

    public AddressResolver(ISimpleHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Optional<Address> findAddressForLocation(double lat, double lon) {
        // TODO
    }

}
