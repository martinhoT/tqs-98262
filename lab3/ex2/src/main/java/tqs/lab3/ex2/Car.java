package tqs.lab3.ex2;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class Car {

    private Long carId;
    @NonNull
    private String maker;
    @NonNull
    private String model;

}
