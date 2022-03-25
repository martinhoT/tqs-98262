package tqs.lab3.ex2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.google.gson.JsonElement;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarController.class)
public class CarControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CarService carService;

    @Test
    public void whenListOfCarsAdded_thenListOfCarsPresent() throws Exception {
        List<Car> cars = List.of(
                new Car("Volkswagen", "idk"),
                new Car("Rover", "OG"),
                new Car("Hyundai", "Kauai"),
                new Car("Rover", "Maestro"),
                new Car("Rover", "CityRover"),
                new Car("Volkswagen", "¯\\_(ツ)_/¯")
        );

        // Mocking
        for (int i = 0; i < cars.size(); i++) {
            when(carService.getCarDetails((long) i)).thenReturn(Optional.of(cars.get(i)));
            when(carService.save(cars.get(i))).thenReturn(cars.get(i));
        }
        when(carService.getAllCars()).thenReturn(cars);

        cars.forEach((car) -> {
            try { mvcCreateCar(car).andExpect(status().isOk()); }
            catch (Exception e) { e.printStackTrace(); }
        });

        for (int i = 0; i < cars.size(); i++)
            assertThat(mvcGetCarById((long) i), equalTo( ResponseEntity.ok(cars.get(i)) ));

        assertThat(convertMvcResultIntoCarList(mvcGetAllCars()), containsInAnyOrder(cars));
    }

    @Test
    public void whenQueryInexistentCars_thenReturnNoCarsAndThrow() throws Exception {
        // Mocking
        when(carService.getCarDetails(anyLong())).thenReturn(Optional.empty());
        when(carService.getAllCars()).thenReturn(new ArrayList<>());

        assertThat(mvcGetCarById(0L), equalTo(ResponseEntity.notFound()));
        assertThat(mvcGetCarById(10L), equalTo(ResponseEntity.notFound()));

        assertThat(convertMvcResultIntoCarList(mvcGetAllCars()), empty());
    }

    @Test
    public void whenCarAdded_thenSameCarReturned() throws Exception {
        Car car = new Car("Rover", "OG");

        // Mocking
        when(carService.save(car)).thenReturn(car);

        assertThat(mvcCreateCar(car), equalTo( ResponseEntity.ok(car)) );
    }

    @Test
    public void whenGetCarByIllegalId_thenThrowIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> mvcGetCarById(-1L));
        assertThrows(IllegalArgumentException.class, () -> mvcGetCarById(-10L));
        assertThrows(IllegalArgumentException.class, () -> mvcGetCarById(-176349076234907L));
    }

    private ResultActions mvcCreateCar(Car car) throws Exception {
        return mvc.perform(
                post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.gson.toJson(car))
        );
    }

    // TODO: jfc
    private ResultActions mvcGetCarById(Long id) throws Exception {
        return mvc.perform(
                get("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .param("id", String.valueOf(id))
        );
    }

    private ResultActions mvcGetAllCars() throws Exception {
        return mvc.perform(
                get("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private List<Car> convertMvcResultIntoCarList(ResultActions resultActions) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString();
        JsonElement json = JsonUtils.gson.fromJson(response, JsonElement.class);
        List<Car> carsResponse = new ArrayList<>();
        json.getAsJsonArray().forEach((elem) -> carsResponse.add(JsonUtils.gson.fromJson(elem, Car.class)));
        return carsResponse;
    }

}
