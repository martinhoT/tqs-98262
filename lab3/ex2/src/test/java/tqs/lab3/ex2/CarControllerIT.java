package tqs.lab3.ex2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
//@AutoConfigureTestDatabase
@TestPropertySource(locations = "application-integrationtest.properties")
public class CarControllerIT {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CarRepository carRepository;

    // List of test cars to be used
    private List<Car> cars;
    private List<Long> carsIds;

    @BeforeEach
    public void initCars() {
        cars = List.of(
                new Car("Volkswagen", "idk"),
                new Car("Rover", "OG"),
                new Car("Hyundai", "Kauai"),
                new Car("Rover", "Maestro"),
                new Car("Rover", "CityRover")
        );
        carsIds = new ArrayList<>();
        cars.forEach((car) -> carsIds.add( carRepository.save(car).getCarId() )  );
        carRepository.flush();
    }

    @AfterEach
    public void resetDb() {
        carRepository.deleteAll();
    }

    @Test
    public void whenCarListAdded_thenCarListPersisted() {
        List<Car> carsTest = List.of(
                new Car("Ferrari", "abcd"),
                new Car("Porsche", "wot"),
                new Car("Toyota", "woah")
        );

        carsTest.forEach((car) -> assertThat(restTemplate
                        .postForEntity("/api/cars", car, Car.class)
                        .getStatusCode())
                .isEqualTo(HttpStatus.OK)
        );

        List<Car> carsPersisted = carRepository.findAll();
        List<Car> carsAdded = new ArrayList<>();
        carsAdded.addAll(cars);
        carsAdded.addAll(carsTest);
        assertThat(carsPersisted).hasSize(cars.size() + carsTest.size()).allMatch((car) -> carsAdded.stream().anyMatch(car::same));
    }

    @Test
    public void whenCarListPersited_thenCarsReturned() {
        carRepository.saveAll(cars);

        // Testing with get by id
        List<Car> carsReturnedSingle = new ArrayList<>();

        for (Long id : carsIds) {
            ResponseEntity<Car> carResponse = restTemplate.getForEntity("/api/cars/" + id, Car.class);
            assertThat( carResponse.getStatusCode() ).isEqualTo(HttpStatus.OK);
            carsReturnedSingle.add( carResponse.getBody() );
        }
        assertThat(carsReturnedSingle).hasSize(cars.size()).allMatch((car) -> cars.stream().anyMatch(car::same));

        // Testing with bulk get (list)
        ResponseEntity<Car[]> carsResponse = restTemplate.getForEntity("/api/cars", Car[].class);
        assertThat( carsResponse.getStatusCode() ).isEqualTo(HttpStatus.OK);
        assertNotNull( carsResponse.getBody() );
        List<Car> carsReturnedList = Arrays.asList( carsResponse.getBody() );
        assertThat(carsReturnedList).hasSize(cars.size()).allMatch((car) -> cars.stream().anyMatch(car::same));
    }



}
