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
}
