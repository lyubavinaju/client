package hw.client.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hw.client.model.Stocks;
import hw.client.response.UserMoneyResponse;
import hw.client.response.UserStocksResponse;
import hw.client.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private ObjectMapper mapper;
    public static final String HTTP_LOCALHOST_8080 = "http://localhost:8080";

    @RequestMapping("/add/{name}")
    public String addUser(@PathVariable String name) {
        Storage.addUser(name);
        return "User added";
    }

    @RequestMapping("/addMoney/{name}/{amount}")
    public String addMoney(@PathVariable String name, @PathVariable Long amount) {
        if (Storage.addMoney(name, amount)) {
            return "Money added";
        } else {
            return "Something wrong";
        }
    }

    @RequestMapping("/stocks/{name}")
    public String stocks(@PathVariable String name) throws URISyntaxException, IOException, InterruptedException {
        var result = new UserStocksResponse(name);
        Set<Stocks> stocks = Storage.getStocks(name);
        for (Stocks companyStocks : stocks) {
            String companyName = companyStocks.getCompanyName();
            long count = companyStocks.getCount();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/getPrice/" + companyName))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            long price = Long.parseLong(response.body());
            result.addStockResponse(companyName, count, price);
        }
        return mapper.writeValueAsString(result);
    }

    @RequestMapping("/buy/{name}/{company}/{count}")
    public String buy(@PathVariable String name,
                      @PathVariable String company,
                      @PathVariable Long count) throws URISyntaxException, IOException, InterruptedException {
        var requestPrice = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/getPrice/" + company))
                .GET()
                .build();
        long price = Long.parseLong(
                HttpClient.newHttpClient().send(requestPrice, HttpResponse.BodyHandlers.ofString()).body());
        var user = Storage.getUser(name);

        if (user == null || count * price > user.getMoney()) {
            return "Something wrong. Try again";
        }
        var requestBuyStocks = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/pop/" + company + "/" + count + "/" + price))
                .GET()
                .build();
        String body = HttpClient.newHttpClient().send(requestBuyStocks, HttpResponse.BodyHandlers.ofString()).body();
        if (body.equals("Stocks removed")) {
            Storage.addStocks(name, company, count);
            Storage.removeMoney(name, count * price);
            return "OK";
        }
        return body;
    }

    @RequestMapping("/sell/{name}/{company}/{count}")
    public String sell(@PathVariable String name,
                       @PathVariable String company,
                       @PathVariable Long count) throws URISyntaxException, IOException, InterruptedException {

        try {
            Storage.removeStocks(name, company, count);
        } catch (Exception e) {
            return e.getMessage();
        }
        var request = HttpRequest.newBuilder()
                .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/put/" + company + "/" + count))
                .GET()
                .build();
        String body = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).body();
        char[] chars = body.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        Storage.addMoney(name, Long.parseLong(sb.toString()));
        return body;
    }

    @RequestMapping("/money/{name}")
    public String money(@PathVariable String name) throws IOException, InterruptedException, URISyntaxException {
        var user = Storage.getUser(name);
        if (user == null) {
            return "Something wrong. Try again";
        }
        long allMoney = user.getMoney();
        for (Stocks stocks : user.getStocks()) {
            long count = stocks.getCount();
            String companyName = stocks.getCompanyName();
            var requestPrice = HttpRequest.newBuilder()
                    .uri(new URI(HTTP_LOCALHOST_8080 + "/stocks/getPrice/" + companyName))
                    .GET()
                    .build();
            long price = Long.parseLong(
                    HttpClient.newHttpClient().send(requestPrice, HttpResponse.BodyHandlers.ofString()).body());
            allMoney += count * price;
        }
        var result = new UserMoneyResponse(name, user.getMoney(), allMoney);
        return mapper.writeValueAsString(result);
    }
}
