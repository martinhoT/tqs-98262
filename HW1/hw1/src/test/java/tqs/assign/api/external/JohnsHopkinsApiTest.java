package tqs.assign.api.external;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static tqs.assign.api.external.JohnsHopkinsApi.JohnsHopkinsCountry;
import static tqs.assign.api.external.JohnsHopkinsApi.JohnsHopkinsStats;
import static tqs.assign.Utils.gson;

class JohnsHopkinsApiTest {

    public static MockWebServer mockWebServer;

    private JohnsHopkinsApi johnsHopkinsApi;

    private Set<JohnsHopkinsCountry> countries;
    private Set<String> countriesIso2;

    private final String baseJson = "{\"data\":%s}";

   private String baseUrl;



    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        JohnsHopkinsCountry countryPT = new JohnsHopkinsCountry("CHN", "China");
        JohnsHopkinsCountry countryGB = new JohnsHopkinsCountry("USA", "US");
        countries = Set.of(countryPT, countryGB);
        countriesIso2 = Set.of("CN", "US");

        mockWebServer.enqueue(new MockResponse()
                .setBody( baseJson.formatted(gson.toJson(countries)) )
                .addHeader("Content-Type", "application/json"));

        baseUrl = "http://localhost:" + mockWebServer.getPort();
        johnsHopkinsApi = new JohnsHopkinsApi(baseUrl, null, true, true);

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/regions", request.getPath());

    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }



    @Test
    @DisplayName("Don't automatically fetch countries if built with its flag disabled")
    void whenBuiltWithDisabledAutoFetchCountries_thenNoRequests() throws InterruptedException {
        new JohnsHopkinsApi(baseUrl, null, true, false);
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNull(request);
    }

    @Test
    @DisplayName("Get supported countries")
    void whenGetSupportedCountries_thenGetSupportedCountries() {
        assertEquals(
                countriesIso2,
                johnsHopkinsApi.getSupportedCountries());
    }

    @Test
    @DisplayName("Test conversion from example JSON response")
    void whenObtainJSONResponse_thenSuccessfullyConvert() {
        String responseBody = """
                {
                "data":{
                "date":"2020-04-07",
                "last_update":"2020-04-07 23:11:31",
                "confirmed":1426096,
                "confirmed_diff":80995,
                "deaths":81865,
                "deaths_diff":7300,
                "recovered":300054,
                "recovered_diff":23539,
                "active":1044177,
                "active_diff":50156,
                "fatality_rate":0.0574
                }
                }""";

        Stats responseStats = new Stats(
                1426096, 80995,
                81865, 7300,
                300054, 23539,
                1044177, 50156,
                0.0574
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        assertEquals(responseStats, johnsHopkinsApi.getStats(ApiQuery.builder().build()));
    }

    @Test
    @DisplayName("Get stats with filtering by parameters")
    void whenGetStatsWithParams_thenReturnCorrectStats() throws InterruptedException {
        JohnsHopkinsStats resultsWorld = new JohnsHopkinsStats(
                    LocalDate.of(2021, 1, 31),
                    500, 400, 300 , 200,
                    50, 40 , 30, 20, 13.0
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody( baseJson.formatted(gson.toJson(resultsWorld)) )
                .addHeader("Content-Type", "application/json"));

        assertEquals(new Stats(
                        resultsWorld.getConfirmed(),
                        resultsWorld.getConfirmedDiff(),
                        resultsWorld.getDeaths(),
                        resultsWorld.getDeathsDiff(),
                        resultsWorld.getRecovered(),
                        resultsWorld.getRecoveredDiff(),
                        resultsWorld.getActive(),
                        resultsWorld.getActiveDiff(),
                        resultsWorld.getFatalityRate()
                ), johnsHopkinsApi.getStats(ApiQuery.builder()
                        .atDate(LocalDate.of(2021, 2, 1))
                        .build())
        );

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        HttpUrl httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals("/reports/total", httpUrl.encodedPath());
        assertEquals("2021-02-01", httpUrl.queryParameter("date"));


        JohnsHopkinsStats resultsCountry = new JohnsHopkinsStats(
                LocalDate.of(2021, 3, 20),
                123, 456, 789, 100,
                321, 654, 987, 100, 0.5
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody( baseJson.formatted(gson.toJson(resultsCountry)) )
                .addHeader("Content-Type", "application/json"));


        assertEquals(new Stats(
                        resultsCountry.getConfirmed(),
                        resultsCountry.getConfirmedDiff(),
                        resultsCountry.getDeaths(),
                        resultsCountry.getDeathsDiff(),
                        resultsCountry.getRecovered(),
                        resultsCountry.getRecoveredDiff(),
                        resultsCountry.getActive(),
                        resultsCountry.getActiveDiff(),
                        resultsCountry.getFatalityRate()
                ), johnsHopkinsApi.getStats(ApiQuery.builder()
                        .atCountry("US")
                        .atDate(LocalDate.of(2021, 3, 20))
                        .build())
        );

        request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals("/reports", httpUrl.encodedPath());
        assertEquals("USA", httpUrl.queryParameter("iso"));
        assertEquals("2021-03-20", httpUrl.queryParameter("date"));
    }

    @Test
    @DisplayName("UnavailableExternalApiException when no results")
    void whenNoResponse_thenThrowUnavailableExternalApiException() {
        ApiQuery worldQuery = ApiQuery.builder().build();

        mockWebServer.enqueue(new MockResponse()
                .setBody( baseJson.formatted(gson.toJson(Collections.emptyList())) )
                .addHeader("Content-Type", "application/json"));

        assertThrows(UnavailableExternalApiException.class, () -> johnsHopkinsApi.getStats(worldQuery));
    }

    @Test
    @DisplayName("UnavailableExternalApiException when request with unsupported parameters is made")
    void whenUnsupportedParameters_thenThrowUnavailableApiException() {
        ApiQuery queryBefore = ApiQuery.builder().before(LocalDate.now()).build();
        ApiQuery queryAfter = ApiQuery.builder().before(LocalDate.now()).build();

        assertThrows(UnavailableExternalApiException.class,
                () -> johnsHopkinsApi.getStats(queryBefore));
        assertThrows(UnavailableExternalApiException.class,
                () -> johnsHopkinsApi.getStats(queryAfter));
    }

}
