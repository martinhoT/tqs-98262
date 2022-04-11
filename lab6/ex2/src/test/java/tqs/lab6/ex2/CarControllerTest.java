package tqs.lab6.ex2;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import com.google.gson.JsonElement;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarController.class)
class CarControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CarService carService;

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
            try { mvcCreateCar(car).andExpect(status().isOk()); }
            catch (Exception e) { e.printStackTrace(); }
        });

        // Mocking
        for (int i = 0; i < cars.size(); i++) {
            Car car = cars.get(i);
            car.setCarId((long) i);
            when(carService.getCarDetails((long) i)).thenReturn(Optional.of(car));
        }
        when(carService.getAllCars()).thenReturn(cars);

        List<CarDTO> carsReturned = convertMvcResultIntoCarList(mvcGetAllCars());
        for (int i = 0; i < cars.size(); i++) {
            ResultActions mvcResponse = mvcGetCarById((long) i).andExpect(status().isOk());
            CarDTO carReturnedSingle = convertMvcResultIntoCar(mvcResponse);
            Car carCreated = cars.get(i);
            System.err.println(carCreated);
            assertEquals(carReturnedSingle.getMaker(), carCreated.getMaker());
            assertEquals(carReturnedSingle.getModel(), carCreated.getModel());

            CarDTO carReturnedList = carsReturned.get(i);
            assertEquals(carReturnedList.getMaker(), carCreated.getMaker());
            assertEquals(carReturnedList.getModel(), carCreated.getModel());
        }

        assertThat(carsReturned, hasSize(cars.size()));

        verify(carService, times(cars.size())).save(any());
    }

    @Test
    void whenQueryInexistentCars_thenReturnNoCars() throws Exception {
        // Mocking
        when(carService.getCarDetails(anyLong())).thenReturn(Optional.empty());
        when(carService.getAllCars()).thenReturn(new ArrayList<>());

        mvcGetCarById(0L).andExpect(status().isNotFound());
        mvcGetCarById(10L).andExpect(status().isNotFound());

        assertThat(convertMvcResultIntoCarList(mvcGetAllCars()), empty());
    }

    @Test
    void whenCarAdded_thenSameCarReturned() throws Exception {
        Car car = new Car(1L, "Rover", "OG");

        // Mocking
        when(carService.save(ArgumentMatchers.any(Car.class))).thenReturn(car);

        CarDTO carReturned = convertMvcResultIntoCar( mvcCreateCar(car).andExpect(status().isOk()) );

        // Can't test with normal 'equals' since Car is a Hibernate entity
        assertEquals(car.getMaker(), carReturned.getMaker());
        assertEquals(car.getModel(), carReturned.getModel());
    }

    private ResultActions mvcCreateCar(Car car) throws Exception {
        return mvc.perform(
                post("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.gson.toJson(car))
        );
    }

    private ResultActions mvcGetCarById(Long id) throws Exception {
        return mvc.perform(
                get("/api/cars/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private ResultActions mvcGetAllCars() throws Exception {
        return mvc.perform(
                get("/api/cars")
                .contentType(MediaType.APPLICATION_JSON)
        );
    }

    private CarDTO convertMvcResultIntoCar(ResultActions resultActions) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonUtils.gson.fromJson(response, CarDTO.class);
    }

    // jfc
    private List<CarDTO> convertMvcResultIntoCarList(ResultActions resultActions) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonElement json = JsonUtils.gson.fromJson(response, JsonElement.class);
        List<CarDTO> carsResponse = new ArrayList<>();
        json.getAsJsonArray().forEach((elem) -> carsResponse.add(JsonUtils.gson.fromJson(elem, CarDTO.class)));
        return carsResponse;
    }

}
