package tqs.lab6.ex2;

import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "car")
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long carId;

    @NonNull
    private String maker;
    @NonNull
    private String model;

    /**
     * Whether two cars are of the same maker and model (the same kind of vehicle basically)
     *
     * @param car the car to compare to
     * @return whether they are of the same kind
     */
    public boolean same(Car car) {
        return maker.equals(car.maker) && model.equals(car.model);
    }

}