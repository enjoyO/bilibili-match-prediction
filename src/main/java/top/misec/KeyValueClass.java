package top.misec;

import lombok.Data;

@Data
public class KeyValueClass {

    private Boolean skipDailyTask;
    private Integer predictNumberOfCoins ;
    private Integer   minimumNumberOfCoins;
    private Boolean reverse;
    private String dedeuserid;
    private String sessdata;
    private String biliJct;
    private String serverpushkey;
    private String telegrambottoken;
    private String telegramchatid;
    //1 买赔率大的, 0-买赔率小的
    private Integer odds;
    //赔率相等是否购买 false 不购买, true 购买
    private boolean equalsBuy = false;
}
