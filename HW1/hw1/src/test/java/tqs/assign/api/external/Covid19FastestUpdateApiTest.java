package tqs.assign.api.external;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import tqs.assign.api.ApiQuery;
import tqs.assign.data.Stats;
import tqs.assign.exceptions.UnavailableExternalApiException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static tqs.assign.api.external.Covid19FastestUpdateApi.Covid19Country;
import static tqs.assign.api.external.Covid19FastestUpdateApi.Covid19CountryStats;
import static tqs.assign.api.external.Covid19FastestUpdateApi.Covid19WorldStats;
import static tqs.assign.Utils.gson;

class Covid19FastestUpdateApiTest {

    public static MockWebServer mockWebServer;

    private Covid19FastestUpdateApi covid19FastestUpdateApi;

    private Set<Covid19Country> countries;

    private String baseUrl;



    @BeforeEach
    void setUp() throws InterruptedException, IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        Covid19Country countryPT = new Covid19Country("Portugal", "portugal", "PT");
        Covid19Country countryGB = new Covid19Country("Great Britain", "great-britain", "GB");
        countries = Set.of(countryPT, countryGB);

        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(countries) )
                .addHeader("Content-Type", "application/json"));

        baseUrl = "http://localhost:" + mockWebServer.getPort();
        covid19FastestUpdateApi = new Covid19FastestUpdateApi(baseUrl, true, true, true);

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        assertEquals("/countries", request.getPath());
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close();
    }



    @Test
    @DisplayName("Don't automatically fetch countries if built with its flag disabled")
    void whenBuiltWithDisabledAutoFetchCountries_thenNoRequests() throws InterruptedException {
        new Covid19FastestUpdateApi(baseUrl, true, true, false);
        RecordedRequest request = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNull(request);
    }

    @Test
    @DisplayName("Get supported countries")
    void whenGetSupportedCountries_thenGetSupportedCountries() {
        assertEquals(
                countries.stream().map(Covid19Country::getIso2).collect(Collectors.toSet()),
                covid19FastestUpdateApi.getSupportedCountries());
    }

    @Test
    @DisplayName("Test conversion from example JSON response")
    void whenObtainJSONResponse_thenSuccessfullyConvert() {
        String responseBody = "[{\"NewConfirmed\":744386,\"TotalConfirmed\":436365582,\"NewDeaths\":4808,\"TotalDeaths\":5951993,\"NewRecovered\":0,\"TotalRecovered\":0,\"Date\":\"2022-03-01T21:50:39.652Z\"},{\"NewConfirmed\":655339,\"TotalConfirmed\":437900927,\"NewDeaths\":5450,\"TotalDeaths\":5960298,\"NewRecovered\":0,\"TotalRecovered\":0,\"Date\":\"2022-03-02T22:00:35.19Z\"},{\"NewConfirmed\":779149,\"TotalConfirmed\":439552778,\"NewDeaths\":5031,\"TotalDeaths\":5968058,\"NewRecovered\":0,\"TotalRecovered\":0,\"Date\":\"2022-03-03T22:08:17.177Z\"}]";

        Stats responseStats = new Stats(
                439552778, 3187196,
                5968058, 16065,
                0, 0,
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                ((double) 5968058/439552778)*100
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody(responseBody)
                .addHeader("Content-Type", "application/json"));

        assertEquals(responseStats, covid19FastestUpdateApi.getStats(ApiQuery.builder().build()));
    }

    @Test
    @DisplayName("Get stats with filtering by parameters")
    void whenGetStatsWithParams_thenReturnCorrectStats() throws InterruptedException {
        List<Covid19WorldStats> resultsWorld = List.of(
                new Covid19WorldStats(
                        521, 25, 26,
                        LocalDateTime.of(LocalDate.of(2021, 1, 31), LocalTime.MIN)),
                new Covid19WorldStats(
                        532, 55, 42,
                        LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.MIN)
                )
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(resultsWorld) )
                .addHeader("Content-Type", "application/json"));

        Covid19WorldStats lastWorld = resultsWorld.get(resultsWorld.size() - 1);
        Covid19WorldStats firstWorld = resultsWorld.get(0);
        assertEquals(new Stats(
                        lastWorld.getConfirmed(),
                        lastWorld.getConfirmed() - firstWorld.getConfirmed(),
                        lastWorld.getDeaths(),
                        lastWorld.getDeaths() - firstWorld.getDeaths(),
                        lastWorld.getRecovered(),
                        lastWorld.getRecovered() - firstWorld.getRecovered(),
                        Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                        ((double) lastWorld.getDeaths() / lastWorld.getConfirmed()) * 100
            ), covid19FastestUpdateApi.getStats(ApiQuery.builder()
                    .before(LocalDate.of(2021, 12, 12))
                    .atDate(LocalDate.of(2021, 2, 1))
                    .build())
        );

        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        HttpUrl httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals("/world", httpUrl.encodedPath());
        assertEquals("2021-01-31", httpUrl.queryParameter("from"));
        assertEquals("2021-02-01", httpUrl.queryParameter("to"));


        List<Covid19CountryStats> resultsCountry = List.of(
                new Covid19CountryStats(
                        "Portugal", "-",
                        300, 19, 20, 20,
                        LocalDateTime.of(LocalDate.of(2021, 1, 29), LocalTime.MIN)),
                new Covid19CountryStats(
                        "Portugal", "-",
                        400, 20, 23, 30,
                        LocalDateTime.of(LocalDate.of(2021, 1, 30), LocalTime.MIN)),
                new Covid19CountryStats(
                        "Portugal", "-",
                        521, 25, 26, 36,
                        LocalDateTime.of(LocalDate.of(2021, 1, 31), LocalTime.MIN)),
                new Covid19CountryStats(
                        "Portugal", "-",
                        532, 55, 42, 36,
                        LocalDateTime.of(LocalDate.of(2021, 2, 1), LocalTime.MIN)
                )
        );

        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(resultsCountry) )
                .addHeader("Content-Type", "application/json"));

        Covid19CountryStats lastCountry = resultsCountry.get(resultsCountry.size() - 1);
        Covid19CountryStats firstCountry = resultsCountry.get(0);
        assertEquals(new Stats(
                        lastCountry.getConfirmed(),
                        lastCountry.getConfirmed() - firstCountry.getConfirmed(),
                        lastCountry.getDeaths(),
                        lastCountry.getDeaths() - firstCountry.getDeaths(),
                        lastCountry.getRecovered(),
                        lastCountry.getRecovered() - firstCountry.getRecovered(),
                        lastCountry.getActive(),
                        lastCountry.getActive() - firstCountry.getActive(),
                        ((double) lastCountry.getDeaths() / lastCountry.getConfirmed()) * 100
                ), covid19FastestUpdateApi.getStats(ApiQuery.builder()
                        .atCountry("PT")
                        .after(LocalDate.of(2021, 1, 29))
                        .before(LocalDate.of(2021, 2, 1))
                        .build())
        );

        request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertEquals("/country/portugal", httpUrl.encodedPath());
        assertEquals("2021-01-29", httpUrl.queryParameter("from"));
        assertEquals("2021-02-01", httpUrl.queryParameter("to"));
    }

    @Test
    @DisplayName("UnavailableExternalApiException when no results")
    void whenNoResponse_thenThrowUnavailableExternalApiException() {
        ApiQuery worldQuery = ApiQuery.builder().build();

        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(Collections.emptyList()) )
                .addHeader("Content-Type", "application/json"));

        assertThrows(UnavailableExternalApiException.class, () -> covid19FastestUpdateApi.getStats(worldQuery));
    }

    @Test
    @DisplayName("Priority to specific date argument in detriment to range date arguments")
    void whenGetStatsAtDateAndRange_thenPrioritizeAtDate() throws Exception {
        LocalDate dateAt = LocalDate.of(2022, 1, 1);
        LocalDate dateBefore = LocalDate.of(2022, 2, 2);
        LocalDate dateAfter = LocalDate.of(2022, 1, 31);

        Stats responseAtDate = new Stats(
                100, 10,
                20, 5,
                200, 17,
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                20
        );
        List<Covid19WorldStats> externalResponseAtDate = List.of(
                new Covid19WorldStats(90, 15, 183, LocalDateTime.of(dateAt.minusDays(1L), LocalTime.MIN)),
                new Covid19WorldStats(100, 20, 200, LocalDateTime.of(dateAt, LocalTime.MIN))
        );
        Stats responseAtRange = new Stats(
                550, 42,
                55, 10,
                221, 54,
                Stats.UNSUPPORTED_FIELD, Stats.UNSUPPORTED_FIELD,
                10
        );
        List<Covid19WorldStats> externalResponseAtRange = List.of(
                new Covid19WorldStats(508, 45, 167, LocalDateTime.of(dateAfter, LocalTime.MIN)),
                new Covid19WorldStats(525, 50, 200, LocalDateTime.of(dateAfter.plusDays(1L), LocalTime.MIN)),
                new Covid19WorldStats(550, 55, 221, LocalDateTime.of(dateBefore, LocalTime.MIN))
        );

        ApiQuery queryAtDate = ApiQuery.builder()
                .atDate(dateAt)
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(externalResponseAtDate) )
                .addHeader("Content-Type", "application/json"));

        ApiQuery queryBoth = ApiQuery.builder()
                .atDate(dateAt)
                .before(dateBefore)
                .after(dateAfter)
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(externalResponseAtDate) )
                .addHeader("Content-Type", "application/json"));

        ApiQuery queryAtRange = ApiQuery.builder()
                .after(dateAfter)
                .before(dateBefore)
                .build();
        mockWebServer.enqueue(new MockResponse()
                .setBody( gson.toJson(externalResponseAtRange) )
                .addHeader("Content-Type", "application/json"));

        checkCorrectRequestWithDateParameters(queryAtDate, responseAtDate, dateAt.minusDays(1L), dateAt, mockWebServer);

        checkCorrectRequestWithDateParameters(queryBoth, responseAtDate, dateAt.minusDays(1L), dateAt, mockWebServer);

        checkCorrectRequestWithDateParameters(queryAtRange, responseAtRange, dateAfter, dateBefore, mockWebServer);
    }



    private void checkCorrectRequestWithDateParameters(ApiQuery query, Stats response,
                                                       LocalDate requestFromDate, LocalDate requestToDate,
                                                       MockWebServer mockWebServer) throws InterruptedException {
        assertEquals(response, covid19FastestUpdateApi.getStats(query));
        RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
        assertNotNull(request);
        HttpUrl httpUrl = request.getRequestUrl();
        assertNotNull(httpUrl);
        assertThat(httpUrl.queryParameterNames()).contains("from", "to");
        assertEquals(requestFromDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                httpUrl.queryParameter("from"));
        assertEquals(requestToDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                httpUrl.queryParameter("to"));
    }

}
