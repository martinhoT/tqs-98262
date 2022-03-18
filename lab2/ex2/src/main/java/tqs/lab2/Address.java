package tqs.lab2;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Address {

    private String road;
    private String state;
    private String city;
    private String zip;
    private String houseNumber;

    @Override
    public String toString() {
        return road + ", " + state + ", " + city + ", " + zip + ", " + houseNumber;
    }

}
