package tqs.lab2.serialization;

import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tqs.lab2.Address;

@Getter
@Setter
@NoArgsConstructor

public class MapQuestLocation {
    
    private String street;
    private String adminArea6;
    private String adminArea6Type;
    private String adminArea5;
    private String adminArea5Type;
    private String adminArea4;
    private String adminArea4Type;
    private String adminArea3;
    private String adminArea3Type;
    private String adminArea1;
    private String adminArea1Type;
    private String postalCode;
    private String geocodeQualityCode;
    private String geocodeQuality;
    private boolean dragPoint;
    private String sideOfStreet;
    private String linkId;
    private String unknownInput;
    private String type;
    private Map<String, Double> latLng;
    private Map<String, Double> displayLatLng;
    private String mapUrl;

    public Address toAddress() {
        Map<String, String> adminAreas = Map.of(
            adminArea1Type, adminArea1,
            adminArea3Type, adminArea3,
            adminArea4Type, adminArea4,
            adminArea5Type, adminArea5,
            adminArea6Type, adminArea6
        );

        String state = "";
        String city = "";
        String houseNumber = "";
        
        for (Entry<String, String> area : adminAreas.entrySet())
            switch (area.getKey()) {
                case "City": city = area.getValue(); break;
                case "State": state = area.getValue(); break;
                case "County": break;
                case "Country": break;
                case "Neighborhood": break;
            }
            
        return new Address(street, state, city, postalCode, houseNumber);
    }

}
