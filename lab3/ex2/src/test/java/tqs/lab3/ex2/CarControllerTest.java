package tqs.lab3.ex2;

import org.junit.jupiter.api.Test;
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
            Car car = cars.get(i);
            car.setCarId((long) i);
            when(carService.getCarDetails((long) i)).thenReturn(Optional.of(car));
            when(carService.save(car)).thenReturn(car);
        }
        when(carService.getAllCars()).thenReturn(cars);

        cars.forEach((car) -> {
            try { mvcCreateCar(car).andExpect(status().isOk()); }
            catch (Exception e) { e.printStackTrace(); }
        });

        List<Car> carsReturned = convertMvcResultIntoCarList(mvcGetAllCars());
        for (int i = 0; i < cars.size(); i++) {
            ResultActions mvcResponse = mvcGetCarById((long) i).andExpect(status().isOk());
            Car carReturnedSingle = convertMvcResultIntoCar(mvcResponse);
            Car carCreated = cars.get(i);
            System.err.println(carCreated);
            assertEquals(carReturnedSingle.getMaker(), carCreated.getMaker());
            assertEquals(carReturnedSingle.getModel(), carCreated.getModel());

            Car carReturnedList = carsReturned.get(i);
            assertEquals(carReturnedList.getMaker(), carCreated.getMaker());
            assertEquals(carReturnedList.getModel(), carCreated.getModel());
        }

        assertThat(carsReturned, hasSize(cars.size()));

        verify(carService, times(cars.size())).save(any());
    }

    @Test
    public void whenQueryInexistentCars_thenReturnNoCars() throws Exception {
        // Mocking
        when(carService.getCarDetails(anyLong())).thenReturn(Optional.empty());
        when(carService.getAllCars()).thenReturn(new ArrayList<>());

        mvcGetCarById(0L).andExpect(status().isNotFound());
        mvcGetCarById(10L).andExpect(status().isNotFound());

        assertThat(convertMvcResultIntoCarList(mvcGetAllCars()), empty());
    }

    @Test
    public void whenCarAdded_thenSameCarReturned() throws Exception {
        Car car = new Car("Rover", "OG");
        car.setCarId(0L);

        // Mocking
        when(carService.save(car)).thenReturn(car);

        Car carReturned = convertMvcResultIntoCar( mvcCreateCar(car).andExpect(status().isOk()) );
        // Test if they are the same Hibernate entity (equals() is implemented that way)
        assertEquals(carReturned, car);
        assertEquals(carReturned.getMaker(), car.getMaker());
        assertEquals(carReturned.getModel(), car.getModel());
    }

    @Test
    public void whenCarAddedWithId_thenIgnoreId() throws Exception {
        Car car = new Car("Rover", "OG");
        car.setCarId(0L);
        when(carService.getCarDetails(car.getCarId())).thenReturn(Optional.of(car));

        mvcCreateCar(car).andExpect(status().isConflict());
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

    private Car convertMvcResultIntoCar(ResultActions resultActions) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonUtils.gson.fromJson(response, Car.class);
    }

    // jfc
    private List<Car> convertMvcResultIntoCarList(ResultActions resultActions) throws UnsupportedEncodingException {
        String response = resultActions.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonElement json = JsonUtils.gson.fromJson(response, JsonElement.class);
        List<Car> carsResponse = new ArrayList<>();
        json.getAsJsonArray().forEach((elem) -> carsResponse.add(JsonUtils.gson.fromJson(elem, Car.class)));
        return carsResponse;
    }

}
