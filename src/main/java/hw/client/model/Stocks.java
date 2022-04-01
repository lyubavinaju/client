package hw.client.model;

import java.util.Objects;

public class Stocks {
    private final String companyName;
    private long count;

    public String getCompanyName() {
        return companyName;
    }

    public long getCount() {
        return count;
    }

    public Stocks(String companyName, long count) {
        this.companyName = companyName;
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stocks stocks = (Stocks) o;
        return companyName.equals(stocks.companyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyName);
    }

    public void setCount(long count) {
        this.count = count;
    }
}
