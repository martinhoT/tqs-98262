package tqs.assign.api.external;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import tqs.assign.TestUtils;
import tqs.assign.api.Api;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static tqs.assign.api.external.Covid19Api.Covid19Country;
import static tqs.assign.api.external.Covid19Api.Covid19Stats;

class Covid19ApiTest {

    public static MockWebServer mockWebServer;

    private Covid19Api covid19Api;

    private Set<Covid19Country> countries;



    @BeforeAll
    static void setUpAll() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDownAll() throws IOException {
        mockWebServer.close();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        Covid19Country countryPT = new Covid19Country("Portugal", "portugal", "PT");
        Covid19Country countryGB = new Covid19Country("Great Britain", "great-britain", "GB");
        countries = Set.of(countryPT, countryGB);

        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(countries) )
                .addHeader("Content-Type", "application/json"));

        String baseUrl = "http://localhost:" + mockWebServer.getPort();
        covid19Api = new Covid19Api(baseUrl);

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/countries", request.getPath());
    }



    @Test
    @DisplayName("Get supported countries")
    void whenGetSupportedCountries_thenGetSupportedCountries() {
        assertEquals(
                countries.stream().map(Covid19Country::getIso2).collect(Collectors.toSet()),
                covid19Api.getSupportedCountries());
    }

    @Test
    @DisplayName("Get stats with filtering by parameters")
    void whenGetStatsWithParams_thenReturnCorrectStats() throws InterruptedException {
        List<Covid19Stats> results = List.of(
                new Covid19Stats(
                        "World", "-",
                        521, 25, 26, 36,
                        LocalDateTime.of(LocalDate.of(2021, 1, 31), LocalTime.MIN)),
                new Covid19Stats(
                        "World", "-",
                        532, 55, 42, 36,
                        LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.MIN)
                )
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(results) )
                .addHeader("Content-Type", "application/json"));

        Covid19Stats last = results.get(results.size() - 1);
        Covid19Stats first = results.get(0);
        assertEquals(new Stats(
                last.getConfirmed(),
                last.getConfirmed() - first.getConfirmed(),
                last.getDeaths(),
                last.getDeaths() - first.getDeaths(),
                last.getRecovered(),
                last.getRecovered() - first.getRecovered(),
                last.getActive(),
                ((double) last.getDeaths() / last.getConfirmed()) * 100
        ), covid19Api.getStats(ApiQuery.builder()
                .before(LocalDate.of(2021, 12, 12))
                .atDate(LocalDate.of(2021, 2, 1))
                .build())
        );

        RecordedRequest request = mockWebServer.takeRequest();
        HttpUrl httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals("/world", httpUrl.encodedPath());
        assertEquals("2021-01-31T00:00:00", httpUrl.queryParameter("from"));
        assertEquals("2021-02-01T00:00:00", httpUrl.queryParameter("to"));


        results = List.of(
                new Covid19Stats(
                        "Portugal", "-",
                        300, 19, 20, 20,
                        LocalDateTime.of(LocalDate.of(2021, 1, 29), LocalTime.MIN)),
                new Covid19Stats(
                        "Portugal", "-",
                        400, 20, 23, 30,
                        LocalDateTime.of(LocalDate.of(2021, 1, 30), LocalTime.MIN)),
                new Covid19Stats(
                        "Portugal", "-",
                        521, 25, 26, 36,
                        LocalDateTime.of(LocalDate.of(2021, 1, 31), LocalTime.MIN)),
                new Covid19Stats(
                        "Portugal", "-",
                        532, 55, 42, 36,
                        LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.MIN)
                )
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(results) )
                .addHeader("Content-Type", "application/json"));

        last = results.get(results.size() - 1);
        first = results.get(0);
        assertEquals(new Stats(
                        last.getConfirmed(),
                        last.getConfirmed() - first.getConfirmed(),
                        last.getDeaths(),
                        last.getDeaths() - first.getDeaths(),
                        last.getRecovered(),
                        last.getRecovered() - first.getRecovered(),
                        last.getActive(),
                        ((double) last.getDeaths() / last.getConfirmed()) * 100
                ), covid19Api.getStats(ApiQuery.builder()
                        .atCountry("PT")
                        .after(LocalDate.of(2021, 1, 29))
                        .before(LocalDate.of(2021, 2, 1))
                        .build())
        );

        request = mockWebServer.takeRequest();
        httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals("/country/portugal", httpUrl.encodedPath());
        assertEquals("2021-01-29T00:00:00", httpUrl.queryParameter("from"));
        assertEquals("2021-02-01T00:00:00", httpUrl.queryParameter("to"));
    }

    @Test
    @DisplayName("UnavailableExternalApiException when no results")
    void whenNoResponse_thenThrowUnavailableExternalApiException() {
        ApiQuery worldQuery = ApiQuery.builder().build();

        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(Collections.emptyList()) )
                .addHeader("Content-Type", "application/json"));

        assertThrows(UnavailableExternalApiException.class, () -> covid19Api.getStats(worldQuery));
    }

    @Test
    @DisplayName("Priority to specific date argument in detriment to range date arguments")
    void whenGetStatsAtDateAndRange_thenPrioritizeAtDate() throws Exception {
        LocalDate dateAt = LocalDate.of(2022, 1, 1);
        LocalDate dateBefore = LocalDate.of(2022, 2, 2);
        LocalDate dateAfter = LocalDate.of(2021, 1, 1);

        Stats responseAtDate = TestUtils.randomStats();
        Stats responseAtRange = TestUtils.randomStats();

        ApiQuery queryAtDate = ApiQuery.builder()
                .atDate(dateAt)
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(List.of(responseAtDate)) ));

        ApiQuery queryBoth = ApiQuery.builder()
                .atDate(dateAt)
                .before(dateBefore)
                .after(dateAfter)
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(List.of(responseAtDate)) ));

        ApiQuery queryAtRange = ApiQuery.builder()
                .after(dateAfter)
                .before(dateBefore)
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody( TestUtils.gson.toJson(List.of(responseAtRange)) ));

        assertEquals(responseAtDate, covid19Api.getStats(queryAtDate));
        RecordedRequest request = mockWebServer.takeRequest();
        HttpUrl httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertThat(httpUrl.queryParameterNames()).contains("from", "to");
        assertEquals(dateAt.minusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                httpUrl.queryParameter("from"));
        assertEquals(dateAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                httpUrl.queryParameter("to"));

        assertEquals(responseAtDate, covid19Api.getStats(queryBoth));
        request = mockWebServer.takeRequest();
        httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertThat(httpUrl.queryParameterNames()).contains("from", "to");
        assertEquals(dateAt.minusDays(1L).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                httpUrl.queryParameter("from"));
        assertEquals(dateAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                httpUrl.queryParameter("to"));

        assertEquals(responseAtRange, covid19Api.getStats(queryAtRange));
        request = mockWebServer.takeRequest();
        httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertThat(httpUrl.queryParameterNames()).contains("from", "to");
        assertEquals(dateAfter.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                httpUrl.queryParameter("from"));
        assertEquals(dateBefore.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                httpUrl.queryParameter("to"));
    }

}
