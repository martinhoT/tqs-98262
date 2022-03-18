package tqs.lab2.serialization;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MapQuestResult {
    
    private MapQuestProvidedLocation providedLocation;
    private List<MapQuestLocation> locations;

}
