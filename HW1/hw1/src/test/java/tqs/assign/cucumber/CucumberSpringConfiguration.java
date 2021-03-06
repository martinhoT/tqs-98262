package tqs.assign.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import tqs.assign.TestUtils;
import tqs.assign.api.ApiQuery;
import tqs.assign.api.CovidApi;
import tqs.assign.api.CovidCache;
import tqs.assign.data.Stats;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "api.covid-fu.enabled=false",
                "api.johns-hopkins.enabled=false"
        })
@TestPropertySource("/application-test.properties")
public class CucumberSpringConfiguration {}
