package tqs.lab6.ex2;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CarDTO {

    private Long carId;
    private String maker;
    private String model;

}
