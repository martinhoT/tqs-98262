package tqs.lab3.ex2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) { this.carService = carService; }

    @PostMapping("/api/cars")
    public ResponseEntity<Car> createCar(Car car) {
        return ResponseEntity.of( Optional.of(carService.save(car)) );
    }

    @GetMapping("/api/cars")
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    @GetMapping("/api/cars/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return ResponseEntity.of( carService.getCarDetails(id) );
    }

}
