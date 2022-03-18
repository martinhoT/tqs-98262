package tqs.lab2.serialization;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MapQuestInfo {
    
    public int statuscode;
    public MapQuestCopyright copyright;
    public List<String> messages;

}
