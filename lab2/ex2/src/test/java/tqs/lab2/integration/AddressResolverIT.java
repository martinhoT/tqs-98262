package tqs.lab2.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tqs.lab2.Address;
import tqs.lab2.AddressResolver;
import tqs.lab2.TqsBasicHttpClient;

public class AddressResolverIT {
    
    private AddressResolver addressResolver;

    @BeforeEach
    public void beforeEach() {
        addressResolver = new AddressResolver(new TqsBasicHttpClient());
    }

    @AfterEach
    public void afterEach() {}



    @Test
    public void testFindAddressForLocation() {
        List<double[]> locations = List.of(
            new double[]{40.6318, -8.658},
            new double[]{40.552331695304005, -8.506675482221313},
            new double[]{48.858540935311105, 2.294570443699227},
            new double[]{0.0, 0.0},
            new double[]{-90, 0}
        );
        
        List<Address> results = Arrays.asList(
            new Address(
                "Parque Estacionamento da Reitoria - Univerisdade de Aveiro",
                "Centro",
                "Glória e Vera Cruz",
                "3810-193",
                ""),
            new Address(
                "Carreiro Velho",
                "Centro",
                "Oiã",
                "",
                ""),
            new Address(
                "pavillon Eiffel",
                "Ile-de-France",
                "Paris",
                "75007",
                ""),
            new Address("", "", "", "", ""),
            new Address(
                "South Pole Station Airport",
                "",
                "",
                "",
                "")
        );

        for (int i = 0; i < locations.size(); i++)
            assertEquals(addressResolver.findAddressForLocation(locations.get(i)[0], locations.get(i)[1]), Optional.of(results.get(i)));
    }

    @Test
    public void testFindAddressForLocation_BadArguments() {
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(-100, 0));
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(0, 190));
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(91, 190));
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(0, -200));
    }

}
