package hw.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hw.client.response.UserMoneyResponse;
import hw.client.response.UserStocksResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestPropertySource(properties = "server.port=8090")
@Testcontainers
class ClientApplicationTests {
    public static final String COMPANY_NAME = "sber";
    public static final long STOCKS_COUNT = 5L;
    public static final long PRICE = 300;
    public static final String USER_NAME_1 = "sasha";
    public static final String USER_NAME_2 = "masha";
    public static final String USER_NAME_3 = "ivan";
    @Autowired
    private ObjectMapper mapper;
    public static final String HTTP_LOCALHOST_8090 = "http://localhost:8090";
    public static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";
    @Container
    private GenericContainer server = new FixedHostPortGenericContainer("springio/gs-spring-boot-docker")
            .withFixedExposedPort(8080, 8080)
            .withExposedPorts(8080);

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        var addCompanyRequest = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/company/add/" + COMPANY_NAME))
                .GET()
                .build();
        var addCompanyResponse = client.send(addCompanyRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(addCompanyResponse.body());
        var addStocksRequest = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/put/" + COMPANY_NAME + "/" + STOCKS_COUNT))
                .GET()
                .build();
        var addStocksResponse = client.send(addStocksRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(addStocksResponse.body());
        var changePriceRequest = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/changePrice/" + COMPANY_NAME + "/" + PRICE))
                .GET()
                .build();
        var changePriceResponse = client.send(changePriceRequest, HttpResponse.BodyHandlers.ofString());
        System.out.println(changePriceResponse.body());

    }

    @Test
    void testPriceNotChanged() throws IOException {
        int moneyToAdd = 5000;
        int stocksCountToBuy = 1;
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/add/" + USER_NAME_1, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/addMoney/" + USER_NAME_1 + "/" + moneyToAdd, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/buy/" + USER_NAME_1 + "/" + COMPANY_NAME + "/" + stocksCountToBuy, String.class);

        var stocks = mapper.readValue(restTemplate.getForEntity(
                HTTP_LOCALHOST_8090 + "/user/stocks/" + USER_NAME_1, String.class).getBody(), UserStocksResponse.class);
        var money = mapper.readValue(restTemplate.getForEntity(
                HTTP_LOCALHOST_8090 + "/user/money/" + USER_NAME_1, String.class).getBody(), UserMoneyResponse.class);

        Assertions.assertEquals(1, stocks.getStocks().size());
        Assertions.assertEquals(stocksCountToBuy, stocks.getStocks().get(0).getCount());
        Assertions.assertEquals(moneyToAdd - PRICE, money.getFreeMoney());
        Assertions.assertEquals(moneyToAdd, money.getAllMoney());
    }

    @Test
    void testPriceChanged() throws URISyntaxException, IOException, InterruptedException {
        int moneyToAdd = 5000;
        int stocksCountToBuy = 1;
        int newPrice = 2000;
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/add/" + USER_NAME_2, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/addMoney/" + USER_NAME_2 + "/" + moneyToAdd, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/buy/" + USER_NAME_2 + "/" + COMPANY_NAME + "/" + stocksCountToBuy, String.class);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/changePrice/" + COMPANY_NAME + "/" + newPrice))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(response.body());
        var stocks = mapper.readValue(restTemplate.getForEntity(
                HTTP_LOCALHOST_8090 + "/user/stocks/" + USER_NAME_2, String.class).getBody(), UserStocksResponse.class);
        var money = mapper.readValue(restTemplate.getForEntity(
                HTTP_LOCALHOST_8090 + "/user/money/" + USER_NAME_2, String.class).getBody(), UserMoneyResponse.class);

        Assertions.assertEquals(1, stocks.getStocks().size());
        Assertions.assertEquals(stocksCountToBuy, stocks.getStocks().get(0).getCount());
        Assertions.assertEquals(moneyToAdd - PRICE + newPrice, money.getAllMoney());
    }

    @Test
    void testSell() throws IOException {
        int moneyToAdd = 5000;
        int stocksCountToBuy = 1;
        int stocksCountToSell = 1;
        RestTemplate restTemplate = restTemplateBuilder.build();
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/add/" + USER_NAME_3, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/addMoney/" + USER_NAME_3 + "/" + moneyToAdd, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/buy/" + USER_NAME_3 + "/" + COMPANY_NAME + "/" + stocksCountToBuy, String.class);
        restTemplate.getForEntity(HTTP_LOCALHOST_8090 + "/user/sell/" + USER_NAME_3 + "/" + COMPANY_NAME + "/" + stocksCountToSell, String.class);

        var stocks = mapper.readValue(restTemplate.getForEntity(
                HTTP_LOCALHOST_8090 + "/user/stocks/" + USER_NAME_3, String.class).getBody(), UserStocksResponse.class);
        var money = mapper.readValue(restTemplate.getForEntity(
                HTTP_LOCALHOST_8090 + "/user/money/" + USER_NAME_3, String.class).getBody(), UserMoneyResponse.class);
        System.out.println(mapper.writeValueAsString(stocks));
        System.out.println(mapper.writeValueAsString(money));
        Assertions.assertEquals(1, stocks.getStocks().size());
        Assertions.assertEquals(0, stocks.getStocks().get(0).getCount());
        Assertions.assertEquals(moneyToAdd, money.getFreeMoney());
        Assertions.assertEquals(moneyToAdd, money.getAllMoney());
    }


}
