package tqs.lab7;

import io.restassured.matcher.RestAssuredMatchers;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;


class AppTest {

    @Test
    void whenGetAllTodos_thenCheckOk() {
        when()
                .get("https://jsonplaceholder.typicode.com/todos")
        .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    void whenGetSpecificTodos_thenCheckTitle() {
        int id = 4;
        String title = "et porro tempora";

        when()
                .get("https://jsonplaceholder.typicode.com/todos/{id}", id)
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", equalTo(id))
                .body("title", equalTo(title));
    }

    @Test
    void whenGetAllTodos_thenSpecificTodosPresent() {
        when()
                .get("https://jsonplaceholder.typicode.com/todos")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", hasItems(198, 199));
    }

}
