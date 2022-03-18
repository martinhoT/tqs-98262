package tqs.lab2.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tqs.lab2.Address;
import tqs.lab2.AddressResolver;
import tqs.lab2.ISimpleHttpClient;

@ExtendWith(MockitoExtension.class)
public class AddressResolverTest {
    
    @Mock
    private ISimpleHttpClient httpClient;

    @InjectMocks
    private AddressResolver addressResolver;

    @BeforeEach
    public void beforeEach() {}

    @AfterEach
    public void afterEach() {}



    @Test
    public void testFindAddressForLocation() throws IOException, InterruptedException {
        String mainUrlFormat = "http://open.mapquestapi.com/geocoding/v1/reverse?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&location=%f,%f&includeRoadMetadata=true";
        
        List<double[]> locations = List.of(
            new double[]{40.6318, -8.658},
            new double[]{40.552331695304005, -8.506675482221313},
            new double[]{48.858540935311105, 2.294570443699227},
            new double[]{0.0, 0.0},
            new double[]{-90, 0},
            // API error test, the mocked response will be an error
            new double[]{4, 20}
        );
        
        List<String> responses = List.of(
            "{\"info\":{\"statuscode\":0,\"copyright\":{\"text\":\"\u00A9 2022 MapQuest, Inc.\",\"imageUrl\":\"http://api.mqcdn.com/res/mqlogo.gif\",\"imageAltText\":\"\u00A9 2022 MapQuest, Inc.\"},\"messages\":[]},\"options\":{\"maxResults\":1,\"thumbMaps\":true,\"ignoreLatLngInput\":false},\"results\":[{\"providedLocation\":{\"latLng\":{\"lat\":40.6318,\"lng\":-8.658}},\"locations\":[{\"street\":\"Parque Estacionamento da Reitoria - Univerisdade de Aveiro\",\"adminArea6\":\"\",\"adminArea6Type\":\"Neighborhood\",\"adminArea5\":\"Gl\u00F3ria e Vera Cruz\",\"adminArea5Type\":\"City\",\"adminArea4\":\"\",\"adminArea4Type\":\"County\",\"adminArea3\":\"Centro\",\"adminArea3Type\":\"State\",\"adminArea1\":\"PT\",\"adminArea1Type\":\"Country\",\"postalCode\":\"3810-193\",\"geocodeQualityCode\":\"P1AAA\",\"geocodeQuality\":\"POINT\",\"dragPoint\":false,\"sideOfStreet\":\"N\",\"linkId\":\"0\",\"unknownInput\":\"\",\"type\":\"s\",\"latLng\":{\"lat\":40.631803,\"lng\":-8.657881},\"displayLatLng\":{\"lat\":40.631803,\"lng\":-8.657881},\"mapUrl\":\"http://open.mapquestapi.com/staticmap/v5/map?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&type=map&size=225,160&locations=40.6318025,-8.657881|marker-sm-50318A-1&scalebar=true&zoom=15&rand=-826752461\",\"roadMetadata\":null}]}]}",
            "{\"info\":{\"statuscode\":0,\"copyright\":{\"text\":\"\u00A9 2022 MapQuest, Inc.\",\"imageUrl\":\"http://api.mqcdn.com/res/mqlogo.gif\",\"imageAltText\":\"\u00A9 2022 MapQuest, Inc.\"},\"messages\":[]},\"options\":{\"maxResults\":1,\"thumbMaps\":true,\"ignoreLatLngInput\":false},\"results\":[{\"providedLocation\":{\"latLng\":{\"lat\":40.552331695304005,\"lng\":-8.506675482221313}},\"locations\":[{\"street\":\"Carreiro Velho\",\"adminArea6\":\"\",\"adminArea6Type\":\"Neighborhood\",\"adminArea5\":\"Oi\u00E3\",\"adminArea5Type\":\"City\",\"adminArea4\":\"\",\"adminArea4Type\":\"County\",\"adminArea3\":\"Centro\",\"adminArea3Type\":\"State\",\"adminArea1\":\"PT\",\"adminArea1Type\":\"Country\",\"postalCode\":\"\",\"geocodeQualityCode\":\"P1AAA\",\"geocodeQuality\":\"POINT\",\"dragPoint\":false,\"sideOfStreet\":\"N\",\"linkId\":\"0\",\"unknownInput\":\"\",\"type\":\"s\",\"latLng\":{\"lat\":40.55192,\"lng\":-8.506838},\"displayLatLng\":{\"lat\":40.55192,\"lng\":-8.506838},\"mapUrl\":\"http://open.mapquestapi.com/staticmap/v5/map?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&type=map&size=225,160&locations=40.5519202,-8.506837889994905|marker-sm-50318A-1&scalebar=true&zoom=15&rand=773271381\",\"roadMetadata\":null}]}]}",
            "{\"info\":{\"statuscode\":0,\"copyright\":{\"text\":\"\u00A9 2022 MapQuest, Inc.\",\"imageUrl\":\"http://api.mqcdn.com/res/mqlogo.gif\",\"imageAltText\":\"\u00A9 2022 MapQuest, Inc.\"},\"messages\":[]},\"options\":{\"maxResults\":1,\"thumbMaps\":true,\"ignoreLatLngInput\":false},\"results\":[{\"providedLocation\":{\"latLng\":{\"lat\":48.858540935311105,\"lng\":2.294570443699227}},\"locations\":[{\"street\":\"pavillon Eiffel\",\"adminArea6\":\"\",\"adminArea6Type\":\"Neighborhood\",\"adminArea5\":\"Paris\",\"adminArea5Type\":\"City\",\"adminArea4\":\"\",\"adminArea4Type\":\"County\",\"adminArea3\":\"Ile-de-France\",\"adminArea3Type\":\"State\",\"adminArea1\":\"FR\",\"adminArea1Type\":\"Country\",\"postalCode\":\"75007\",\"geocodeQualityCode\":\"P1AAA\",\"geocodeQuality\":\"POINT\",\"dragPoint\":false,\"sideOfStreet\":\"N\",\"linkId\":\"0\",\"unknownInput\":\"\",\"type\":\"s\",\"latLng\":{\"lat\":48.858422,\"lng\":2.294729},\"displayLatLng\":{\"lat\":48.858422,\"lng\":2.294729},\"mapUrl\":\"http://open.mapquestapi.com/staticmap/v5/map?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&type=map&size=225,160&locations=48.85842220000001,2.294729499999993|marker-sm-50318A-1&scalebar=true&zoom=15&rand=1922665706\"}]}]}",
            "{\"info\":{\"statuscode\":0,\"copyright\":{\"text\":\"\u00A9 2022 MapQuest, Inc.\",\"imageUrl\":\"http://api.mqcdn.com/res/mqlogo.gif\",\"imageAltText\":\"\u00A9 2022 MapQuest, Inc.\"},\"messages\":[]},\"options\":{\"maxResults\":1,\"thumbMaps\":true,\"ignoreLatLngInput\":false},\"results\":[{\"providedLocation\":{\"latLng\":{\"lat\":0.0,\"lng\":0.0}},\"locations\":[{\"street\":\"\",\"adminArea6\":\"\",\"adminArea6Type\":\"Neighborhood\",\"adminArea5\":\"\",\"adminArea5Type\":\"City\",\"adminArea4\":\"\",\"adminArea4Type\":\"County\",\"adminArea3\":\"\",\"adminArea3Type\":\"State\",\"adminArea1\":\"XZ\",\"adminArea1Type\":\"Country\",\"postalCode\":\"\",\"geocodeQualityCode\":\"A1XAX\",\"geocodeQuality\":\"COUNTRY\",\"dragPoint\":false,\"sideOfStreet\":\"N\",\"linkId\":\"0\",\"unknownInput\":\"\",\"type\":\"s\",\"latLng\":{\"lat\":0.0,\"lng\":0.0},\"displayLatLng\":{\"lat\":0.0,\"lng\":0.0},\"mapUrl\":\"http://open.mapquestapi.com/staticmap/v5/map?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&type=map&size=225,160&locations=0.0,0.0|marker-sm-50318A-1&scalebar=true&zoom=2&rand=138347765\"}]}]}",
            "{\"info\":{\"statuscode\":0,\"copyright\":{\"text\":\"\u00A9 2022 MapQuest, Inc.\",\"imageUrl\":\"http://api.mqcdn.com/res/mqlogo.gif\",\"imageAltText\":\"\u00A9 2022 MapQuest, Inc.\"},\"messages\":[]},\"options\":{\"maxResults\":1,\"thumbMaps\":true,\"ignoreLatLngInput\":false},\"results\":[{\"providedLocation\":{\"latLng\":{\"lat\":-90.0,\"lng\":0.0}},\"locations\":[{\"street\":\"South Pole Station Airport\",\"adminArea6\":\"\",\"adminArea6Type\":\"Neighborhood\",\"adminArea5\":\"\",\"adminArea5Type\":\"City\",\"adminArea4\":\"\",\"adminArea4Type\":\"County\",\"adminArea3\":\"\",\"adminArea3Type\":\"State\",\"adminArea1\":\"\",\"adminArea1Type\":\"Country\",\"postalCode\":\"\",\"geocodeQualityCode\":\"P1AAA\",\"geocodeQuality\":\"POINT\",\"dragPoint\":false,\"sideOfStreet\":\"N\",\"linkId\":\"0\",\"unknownInput\":\"\",\"type\":\"s\",\"latLng\":{\"lat\":-90.0,\"lng\":0.0},\"displayLatLng\":{\"lat\":-90.0,\"lng\":0.0},\"mapUrl\":\"http://open.mapquestapi.com/staticmap/v5/map?key=uXSAVwYWbf9tJmsjEGHKKAo0gOjZfBLQ&type=map&size=225,160&locations=-89.9999999,0.0|marker-sm-50318A-1&scalebar=true&zoom=15&rand=1304013783\"}]}]}",
            "{\"info\":{\"statuscode\":0,\"copyright\":{\"text\":\"\u00A9 2022 MapQuest, Inc.\",\"imageUrl\":\"http://api.mqcdn.com/res/mqlogo.gif\",\"imageAltText\":\"\u00A9 2022 MapQuest, Inc.\"},\"messages\":[]},\"options\":{\"maxResults\":1,\"thumbMaps\":true,\"ignoreLatLngInput\":false},\"results\":[{\"providedLocation\":{\"latLng\":{\"lat\":4.0,\"lng\":20.0}},\"locations\":[]}]}"
        );
        
        Map<String, String> requestMap = new HashMap<>();
        for (int i = 0; i < locations.size(); i++)
            requestMap.put(String.format(mainUrlFormat, locations.get(i)[0], locations.get(i)[1]), responses.get(i));
        
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
                ""),
            null
        );

        for (Entry<String, String> entry : requestMap.entrySet())
            when(httpClient.doHttpGet(entry.getKey()))
                .thenReturn(entry.getValue());

        for (int i = 0; i < locations.size(); i++)
            assertEquals(addressResolver.findAddressForLocation(locations.get(i)[0], locations.get(i)[1]), Optional.ofNullable(results.get(i)));

        verify(httpClient, times(locations.size())).doHttpGet(anyString());
    }

    @Test
    public void testFindAddressForLocation_BadArguments() throws IOException, InterruptedException {
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(-100, 0));
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(0, 190));
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(91, 190));
        assertThrows(IllegalArgumentException.class, () -> addressResolver.findAddressForLocation(0, -200));
        
        // Check that the HTTP client wasn't called for these illegal argument exceptions
        verify(httpClient, never()).doHttpGet(anyString());
    }

}
