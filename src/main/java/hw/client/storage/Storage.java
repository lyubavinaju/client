package hw.client.storage;

import hw.client.model.Stocks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Storage {
    public static class User {
        private final String name;
        private Long money = 0L;
        private Set<Stocks> stocks = new HashSet<>();

        public User(String name) {
            this.name = name;
        }

        public Long getMoney() {
            return money;
        }

        public Set<Stocks> getStocks() {
            return stocks;
        }
    }

    private static final Map<String, User> users = new HashMap<>();

    public static void addUser(String name) {
        users.putIfAbsent(name, new User(name));
    }

    public static boolean addMoney(String name, long count) {
        User user = users.getOrDefault(name, null);
        if (user == null) return false;
        user.money += count;
        return true;
    }

    public static boolean removeMoney(String name, long count) {
        User user = users.getOrDefault(name, null);
        if (user == null || user.money < count) return false;
        user.money -= count;
        return true;
    }

    public static Set<Stocks> getStocks(String name) {
        User user = users.getOrDefault(name, null);
        if (user == null) return Set.of();
        return user.stocks;
    }

    public static User getUser(String name) {
        return users.getOrDefault(name, null);
    }

    public static void addStocks(String name, String company, long count) {
        Stocks s = users.get(name).stocks
                .stream()
                .filter(stocks -> stocks.getCompanyName().equals(company))
                .findAny()
                .orElse(new Stocks(company, 0));
        s.setCount(s.getCount() + count);
        users.get(name).stocks.add(s);

    }

    public static void removeStocks(String name, String company, long count) throws Exception {
        Stocks s = users.get(name).stocks
                .stream()
                .filter(stocks -> stocks.getCompanyName().equals(company))
                .findAny().orElseThrow();
        if (s.getCount() < count) {
            throw new Exception("Need more stocks");
        }
        s.setCount(s.getCount() - count);
        users.get(name).stocks.add(s);
    }
}
