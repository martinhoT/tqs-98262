package tqs.lab3.ex2;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class CarControllerTest {

    @Mock
    private CarService carService;

    @InjectMocks
    private CarController carController;

    @Test
    public void whenListOfCarsAdded_thenListOfCarsPresent() {
        List<Car> cars = List.of(
                new Car("Volkswagen", "idk"),
                new Car("Rover", "OG"),
                new Car("Hyundai", "Kauai"),
                new Car("Rover", "Maestro"),
                new Car("Rover", "CityRover"),
                new Car("Volkswagen", "¯\\_(ツ)_/¯")
        );

        // Mocking
        for (int i = 0; i < cars.size(); i++)
            when(carService.getCarDetails((long) i)).thenReturn(Optional.of(cars.get(i)));
        when(carService.getAllCars()).thenReturn(cars);

        cars.forEach(carController::createCar);

        for (int i = 0; i < cars.size(); i++)
            assertThat(carController.getCarById((long) i), equalTo( ResponseEntity.ok(cars.get(i)) ));

        assertThat(carController.getAllCars(), containsInRelativeOrder(cars));
    }

    @Test
    public void whenQueryInexistentCars_thenReturnNoCarsAndThrow() {
        // Mocking
        when(carService.getCarDetails(anyLong())).thenReturn(Optional.empty());
        when(carService.getAllCars()).thenReturn(new ArrayList<>());

        assertThat(carController.getCarById(0L), equalTo(ResponseEntity.notFound()));
        assertThat(carController.getCarById(10L), equalTo(ResponseEntity.notFound()));

        assertThat(carController.getAllCars(), empty());
    }

    @Test
    public void whenCarAdded_thenSameCarReturned() {
        Car car = new Car("Rover", "OG");

        // Mocking
        when(carService.save(car)).thenReturn(car);

        assertThat(carController.createCar(car), equalTo( ResponseEntity.ok(car)) );
    }

    @Test
    public void whenGetCarByIllegalId_thenThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> carController.getCarById(-1L));
        assertThrows(IllegalArgumentException.class, () -> carController.getCarById(-10L));
        assertThrows(IllegalArgumentException.class, () -> carController.getCarById(-176349076234907L));
    }

}
