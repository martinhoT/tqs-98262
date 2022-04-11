package tqs.lab6.ex2;

import org.springframework.stereotype.Component;

@Component
public class CarMapper {

    public CarDTO toDto(Car car) {
        return new CarDTO(car.getCarId(), car.getMaker(), car.getModel());
    }

    // ID is ignored
    public Car toUnidentifiedEntity(CarDTO carDTO) {
        return new Car(null, carDTO.getMaker(), carDTO.getModel());
    }

}
