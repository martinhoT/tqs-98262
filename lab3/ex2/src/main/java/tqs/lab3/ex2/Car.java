package tqs.lab3.ex2;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class Car {

    private Long carId;
    private String maker;
    private String model;

    public Car(String maker, String model) {}

}
