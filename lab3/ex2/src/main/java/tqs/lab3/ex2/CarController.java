package tqs.lab3.ex2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) { this.carService = carService; }

    @PostMapping("/api/cars")
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        if (car.getCarId() != null && carService.getCarDetails(car.getCarId()).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        return ResponseEntity.ok( carService.save(car) );
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
