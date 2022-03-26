package tqs.lab3.ex2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class CarRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CarRepository carRepository;

    @Test
    public void whenCarSaved_thenCarPersisted() {
        Car car = new Car("Rover", "OG");

        entityManager.persistAndFlush(car);
        Car persisted = carRepository.findByCarId(1L);
        assertThat(persisted).isEqualTo(car);
        assertTrue(persisted.same(car));

        car.setMaker("Peugeot");
        car.setModel("The");
        entityManager.persistAndFlush(car);
        persisted = carRepository.findByCarId(1L);
        assertThat(persisted).isEqualTo(car);
        assertTrue(persisted.same(car));
    }

    @Test
    public void whenCarInexistent_thenReturnNull() {
        Car obtained = carRepository.findByCarId(0L);
        assertThat(obtained).isNull();
    }

    @Test
    public void whenMultipleCarsAdded_thenAllCarsPersisted() {
        List<Car> cars = List.of(
                new Car("Volkswagen", "idk"),
                new Car("Rover", "OG"),
                new Car("Hyundai", "Kauai"),
                new Car("Rover", "Maestro"),
                new Car("Rover", "CityRover"),
                new Car("Volkswagen", "¯\\_(ツ)_/¯")
        );

        cars.forEach(entityManager::persist);
        entityManager.flush();

        IntStream.range(0, cars.size()).forEach((id) -> assertTrue( carRepository.getById((long) id+1).same( cars.get(id) ) ));
        assertThat(cars).hasSize(cars.size());
    }

}
