package tqs.lab3.ex2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    @Test
    public void whenGetExistingCar_thenReturnCar() {
        Car car = new Car("Rover", "OG");

        when(carRepository.findByCarId(0L)).thenReturn(car);

        assertThat(carService.getCarDetails(0L), equalTo(Optional.of(car)));
    }

    @Test
    public void whenGetInexistentCar_thenReturnNothing() {
        assertThat(carService.getCarDetails(0L), equalTo(Optional.empty()));
    }

    @Test
    public void whenListOfCarsSaved_thenListOfCarsPresent() {
        List<Car> cars = List.of(
                new Car(1L, "Ferrari", "ABC"),
                new Car(2L, "Porsche", "Bruh"),
                new Car(3L, "Smart", "JK")
        );

        when(carRepository.findAll()).thenReturn(cars);

        List<Car> carsReturned = carService.getAllCars();
        carsReturned.forEach((car) -> assertEquals( car, cars.get(Math.toIntExact(car.getCarId())) ));
        assertThat(carsReturned, hasSize(cars.size()));
    }

    @Test
    public void whenSaveCar_thenReturnSameCar() {
        Car car = new Car("Rover", "OG");

        when(carRepository.save(car)).thenReturn(car);

        assertThat(carService.save(car), equalTo(car));
    }

}
