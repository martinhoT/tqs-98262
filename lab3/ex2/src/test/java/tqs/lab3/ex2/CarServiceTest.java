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

        when(carRepository.getById(0L)).thenReturn(car);

        assertThat(carService.getCarDetails(0L), equalTo(Optional.of(car)));
    }

    @Test
    public void whenGetInexistentCar_thenReturnNothing() {
        assertThat(carService.getCarDetails(0L), equalTo(Optional.empty()));
    }

    @Test
    public void whenListOfCarsSaved_thenListOfCarsPresent() {
        List<Car> cars = List.of(
                new Car("Ferrari", "ABC"),
                new Car("Porsche", "Bruh"),
                new Car("Smart", "JK")
        );

        when(carRepository.findAll()).thenReturn(cars);

        assertThat(carService.getAllCars(), containsInAnyOrder(cars));
    }

    @Test
    public void whenSaveCar_thenReturnSameCar() {
        Car car = new Car("Rover", "OG");

        when(carRepository.save(car)).thenReturn(car);

        assertThat(carService.save(car), equalTo(car));
    }

}
