package hw.client.response;

import java.io.Serializable;

public class UserMoneyResponse implements Serializable {
    private String userName;
    private long freeMoney;
    private long allMoney;

    public UserMoneyResponse(String userName, long freeMoney, long allMoney) {
        this.userName = userName;
        this.freeMoney = freeMoney;
        this.allMoney = allMoney;
    }

    public String getUserName() {
        return userName;
    }

    public long getFreeMoney() {
        return freeMoney;
    }

    public long getAllMoney() {
        return allMoney;
    }
}
