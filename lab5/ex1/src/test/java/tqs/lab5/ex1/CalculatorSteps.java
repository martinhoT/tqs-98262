package tqs.lab5.ex1;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorSteps {

    private Calculator calculator;

    @Given("a calculator I just turned on")
    public void a_calculator_I_just_turned_on() {
        calculator = new Calculator();
    }

    @When("I {operation} {int} and/to {int}")
    public void I_add_two_integers(String operation, int a, int b) {
        switch (operation) {
            case "add": calculator.add(a, b); break;
            case "subtract": calculator.subtract(a, b); break;
        }
    }

    @Then("the result is {int}")
    public void the_result_is(int result) {
        assertEquals(calculator.result(), result);
    }

    @ParameterType("add|subtract")
    public String operation(String operation) { return operation; }

}
