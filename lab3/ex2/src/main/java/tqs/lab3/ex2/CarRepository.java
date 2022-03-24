package tqs.lab3.ex2;

import java.util.List;

public interface CarRepository {

    public Car findById(Long id);

    public List<Car> findAll();

}
