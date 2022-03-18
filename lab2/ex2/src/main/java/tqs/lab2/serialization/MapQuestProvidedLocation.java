package tqs.lab2.serialization;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MapQuestProvidedLocation {
    
    private Map<String, Double> latLng;

}
