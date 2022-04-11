package tqs.lab6.ex2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class CarController {

    private final CarService carService;
    private final CarMapper carMapper;

    @Autowired
    public CarController(CarService carService, CarMapper carMapper) {
        this.carService = carService;
        this.carMapper = carMapper;
    }

    @PostMapping("/api/cars")
    public ResponseEntity<CarDTO> createCar(@RequestBody CarDTO carDTO) {
        Car car = carMapper.toUnidentifiedEntity(carDTO);
        return ResponseEntity.ok( carMapper.toDto(carService.save(car)) );
    }

    @GetMapping("/api/cars")
    public List<CarDTO> getAllCars() {
        return carService.getAllCars()
                .stream()
                .map(carMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/cars/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        return ResponseEntity.of( carService.getCarDetails(id).map(carMapper::toDto) );
    }

}
