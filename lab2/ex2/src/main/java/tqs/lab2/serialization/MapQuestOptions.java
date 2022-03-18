package tqs.lab2.serialization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MapQuestOptions {
    
    private int maxResults;
    private boolean thumbMaps;
    private boolean ignoreLatLngInput;

}
