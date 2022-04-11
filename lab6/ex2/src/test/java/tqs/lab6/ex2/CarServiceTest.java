package tqs.lab6.ex2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    @Test
    void whenGetExistingCar_thenReturnCar() {
        Car car = new Car("Rover", "OG");

        when(carRepository.findByCarId(0L)).thenReturn(car);

        assertThat(carService.getCarDetails(0L), equalTo(Optional.of(car)));
    }

    @Test
    void whenGetInexistentCar_thenReturnNothing() {
        assertThat(carService.getCarDetails(0L), equalTo(Optional.empty()));
    }

    @Test
    void whenListOfCarsSaved_thenListOfCarsPresent() {
        List<Car> cars = List.of(
                new Car("Ferrari", "ABC"),
                new Car("Porsche", "Bruh"),
                new Car("Smart", "JK")
        );

        when(carRepository.findAll()).thenReturn(cars);

        List<Car> carsReturned = carService.getAllCars();
        assertThat(carsReturned, hasSize(cars.size()));
        assertThat(carsReturned, containsInRelativeOrder(cars.toArray()));
    }

    @Test
    void whenSaveCar_thenReturnSameCar() {
        Car car = new Car("Rover", "OG");

        when(carRepository.save(car)).thenReturn(car);

        assertThat(carService.save(car), equalTo(car));
    }

}
