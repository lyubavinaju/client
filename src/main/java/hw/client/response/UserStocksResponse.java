package hw.client.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserStocksResponse implements Serializable {
    private String userName;
    private List<StocksResponse> stocks = new ArrayList<>();

    public String getUserName() {
        return userName;
    }

    public List<StocksResponse> getStocks() {
        return stocks;
    }

    public static class StocksResponse implements Serializable {
        private String companyName;
        private long count;
        private long price;

        public StocksResponse(String companyName, long count, long price) {
            this.companyName = companyName;
            this.count = count;
            this.price = price;
        }

        public String getCompanyName() {
            return companyName;
        }

        public long getCount() {
            return count;
        }

        public long getPrice() {
            return price;
        }
    }

    public UserStocksResponse(String userName) {
        this.userName = userName;
    }

    public UserStocksResponse addStockResponse(String companyName,
                                               long count,
                                               long price) {
        stocks.add(new StocksResponse(companyName, count, price));
        return this;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStocks(List<StocksResponse> stocks) {
        this.stocks = stocks;
    }

    public UserStocksResponse(String userName, List<StocksResponse> stocks) {
        this.userName = userName;
        this.stocks = stocks;
    }
}
