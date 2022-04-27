package tqs.lab3.ex2;

import com.google.gson.JsonElement;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.*;
import static io.restassured.module.mockmvc.matcher.RestAssuredMockMvcMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@WebMvcTest(CarController.class)
class CarControllerRestAssuredTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    private CarService carService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mvc);
    }

    @Test
    void whenListOfCarsAdded_thenListOfCarsPresent() throws Exception {
        List<Car> cars = List.of(
                new Car("Volkswagen", "idk"),
                new Car("Rover", "OG"),
                new Car("Hyundai", "Kauai"),
                new Car("Rover", "Maestro"),
                new Car("Rover", "CityRover"),
                new Car("Volkswagen", "¯\\_(ツ)_/¯")
        );

        cars.forEach((car) -> {
            try { mvcCreateCar(car).then().status(HttpStatus.OK); }
            catch (Exception e) { e.printStackTrace(); }
        });

        // Mocking
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            car.setCarId((long) i);
            doReturn(Optional.of(car)).when(carService).getCarDetails((long) i);
            doReturn(car).when(carService).save(car);
        }
        doReturn(cars).when(carService).getAllCars();

        List<Car> carsReturned = convertMvcResultIntoCarList(mvcGetAllCars());
        for (int i = 0; i < cars.size(); i++) {
            MockMvcResponse mvcResponse = mvcGetCarById((long) i);
            mvcResponse.then().status(HttpStatus.OK);
            Car carReturnedSingle = convertMvcResultIntoCar(mvcResponse);
            Car carCreated = cars.get(i);
            System.err.println(carCreated);
            assertEquals(carReturnedSingle.getMaker(), carCreated.getMaker());
            assertEquals(carReturnedSingle.getModel(), carCreated.getModel());

            Car carReturnedList = carsReturned.get(i);
            assertEquals(carReturnedList.getMaker(), carCreated.getMaker());
            assertEquals(carReturnedList.getModel(), carCreated.getModel());
        }

        assertThat(carsReturned).hasSize(cars.size());

        verify(carService, times(cars.size())).save(any());
    }

    @Test
    void whenQueryNonExistentCars_thenReturnNoCars() throws Exception {
        // Mocking
        when(carService.getCarDetails(anyLong())).thenReturn(Optional.empty());
        when(carService.getAllCars()).thenReturn(new ArrayList<>());

        mvcGetCarById(0L).then().status(HttpStatus.NOT_FOUND);
        mvcGetCarById(10L).then().status(HttpStatus.NOT_FOUND);

        assertThat(convertMvcResultIntoCarList(mvcGetAllCars())).isEmpty();
    }

    @Test
    void whenCarAdded_thenSameCarReturned() throws Exception {
        Car car = new Car("Rover", "OG");

        // Mocking
        when(carService.getCarDetails(anyLong())).thenReturn(Optional.empty());
        when(carService.save(ArgumentMatchers.any(Car.class))).thenReturn(car);

        MockMvcResponse response = mvcCreateCar(car);
        response.then().statusCode(HttpStatus.OK.value());
        Car carReturned = convertMvcResultIntoCar(response);

        // Can't test with normal 'equals' since Car is a Hibernate entity
        assertEquals(car.getMaker(), carReturned.getMaker());
        assertEquals(car.getModel(), carReturned.getModel());
    }

    @Test
    void whenCarAddedWithId_thenIgnoreId() throws Exception {
        Car car = new Car("Rover", "OG");
        when(carService.getCarDetails(car.getCarId())).thenReturn(Optional.of(car));

        mvcCreateCar(car).then().status(HttpStatus.CONFLICT);
    }

    private MockMvcResponse mvcCreateCar(Car car) throws Exception {
        return given().mockMvc(mvc)
                .contentType("application/json")
                .body(JsonUtils.gson.toJson(car))
        .when()
                .post("/api/cars");
    }

    private MockMvcResponse mvcGetCarById(Long id) throws Exception {
        return given().mockMvc(mvc)
                .contentType("application/json")
        .when()
                .get("/api/cars/{id}", id);
    }

    private MockMvcResponse mvcGetAllCars() throws Exception {
        return given().mockMvc(mvc)
                .contentType("application/json")
        .when()
                .get("/api/cars");
    }

    private Car convertMvcResultIntoCar(MockMvcResponse mockResponse) throws UnsupportedEncodingException {
        String response = mockResponse.getMockHttpServletResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonUtils.gson.fromJson(response, Car.class);
    }

    // jfc
    private List<Car> convertMvcResultIntoCarList(MockMvcResponse mockResponse) throws UnsupportedEncodingException {
        String response = mockResponse.getMockHttpServletResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonElement json = JsonUtils.gson.fromJson(response, JsonElement.class);
        List<Car> carsResponse = new ArrayList<>();
        json.getAsJsonArray().forEach((elem) -> carsResponse.add(JsonUtils.gson.fromJson(elem, Car.class)));
        return carsResponse;
    }

}
