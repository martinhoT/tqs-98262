package tqs.lab6.ex2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CarApplication {

    public static void main(String[] args) { SpringApplication.run(CarApplication.class, args); }

    @Bean
    public CarMapper getCarMapper() {
        return new CarMapper();
    }

}
