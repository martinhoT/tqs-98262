package tqs.lab2.serialization;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MapQuest {
    
    private MapQuestInfo info;
    private MapQuestOptions options;
    private List<MapQuestResult> results;

}
