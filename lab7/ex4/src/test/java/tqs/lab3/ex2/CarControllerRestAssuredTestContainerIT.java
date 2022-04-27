package tqs.lab3.ex2;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "spring.jpa.hibernate.ddl-auto=create")
class CarControllerRestAssuredTestContainerIT {

    @LocalServerPort
    int randomServerPort;

    @Container
    public static JdbcDatabaseContainer container = new MySQLContainerProvider()
            .newInstance()
            .withUsername("lab7")
            .withPassword("tqs")
            .withDatabaseName("lab7-ex4");

    @Autowired
    private CarRepository carRepository;

    private RequestSpecification requestSpec;

    // List of test cars to be used
    private List<Car> cars;
    private List<Long> carsIds;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @BeforeEach
    public void setUp() {
        requestSpec = new RequestSpecBuilder()
                .setPort(randomServerPort)
                .setContentType(ContentType.JSON)
                .build();

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
    void whenCarListAdded_thenCarListPersisted() {
        List<Car> carsTest = List.of(
                new Car("Ferrari", "abcd"),
                new Car("Porsche", "wot"),
                new Car("Toyota", "woah")
        );

        carsTest.forEach(car ->
            given()
                    .spec(requestSpec)
                    .body(car)
            .when()
                    .post("/api/cars")
            .then()
                    .statusCode(HttpStatus.OK.value())
        );

        List<Car> carsPersisted = carRepository.findAll();
        List<Car> carsAdded = new ArrayList<>();
        carsAdded.addAll(cars);
        carsAdded.addAll(carsTest);
        assertThat(carsPersisted).hasSize(cars.size() + carsTest.size()).allMatch((car) -> carsAdded.stream().anyMatch(car::same));
    }

    @Test
    void whenCarListPersisted_thenCarsReturned() {
        carRepository.saveAll(cars);

        // Testing with get by id
        List<Car> carsReturnedSingle = new ArrayList<>();

        for (Long id : carsIds) {
            carsReturnedSingle.add(
                given()
                    .spec(requestSpec)
                    .pathParam("id", id)
                .when()
                    .get("/api/cars/{id}")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .and()
                        .extract()
                        .body()
                        .as(Car.class)
            );
        }
        assertThat(carsReturnedSingle).hasSize(cars.size()).allMatch((car) -> cars.stream().anyMatch(car::same));

        // Testing with bulk get (list)
        List<Car> carsReturnedList = Arrays.stream(
                given()
                        .spec(requestSpec)
                .when()
                        .get("/api/cars")
                .then()
                        .statusCode(HttpStatus.OK.value())
                        .body("", notNullValue())
                        .and()
                            .extract()
                            .body()
                            .as(Car[].class)
        ).collect(Collectors.toList());
        assertThat(carsReturnedList).hasSize(cars.size()).allMatch((car) -> cars.stream().anyMatch(car::same));
    }



}
