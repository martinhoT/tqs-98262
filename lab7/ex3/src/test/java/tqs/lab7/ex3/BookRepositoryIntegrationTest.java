package tqs.lab7.ex3;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test-containers-flyway")
class BookRepositoryIntegrationTest {

    @Container
    public static JdbcDatabaseContainer container = new PostgreSQLContainerProvider()
            .newInstance()
            .withUsername("lab5")
            .withPassword("tqs")
            .withDatabaseName("lab5-ex3");

    @Autowired
    private BookRepository bookRepository;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
    }

    @Test
    @Order(1)
    void insertBooks() {
        Book book1 = new Book();
        book1.setId(3L);
        book1.setTitle("Idiotas Úteis e Inúteis");
        book1.setAuthor("Ricardo Araújo Pereira");
        Book book2 = new Book();
        book2.setId(4L);
        book2.setTitle("Não sei mais livros");
        book2.setAuthor("Inculto");

        bookRepository.save(book1);
        bookRepository.save(book2);

        bookRepository.flush();
    }

    @Test
    @Order(2)
    void readBooks() {
        List<Book> books = bookRepository.findAll();

        assertThat(books).hasSize(5);
        assertThat(books).extracting(Book::getTitle).containsAll(List.of(
                "I have no mouth and I must scream",
                "The Art of War",
                "Os Sapatos do Pai Natal",
                "Idiotas Úteis e Inúteis",
                "Não sei mais livros"
        ));
        assertThat(books).extracting(Book::getAuthor).containsAll(List.of(
                "Harlan Ellison",
                "Sun Tzu",
                "José Fanha",
                "Ricardo Araújo Pereira",
                "Inculto"
        ));

        assertThat(bookRepository.findById(3L)).isPresent().get()
                .extracting(Book::getTitle).isEqualTo("Idiotas Úteis e Inúteis");
        assertThat(bookRepository.findById(4L)).isPresent().get()
                .extracting(Book::getAuthor).isEqualTo("Inculto");
    }

}
