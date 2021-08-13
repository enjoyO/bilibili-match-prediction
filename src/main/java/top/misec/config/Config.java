package top.misec.config;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import top.misec.utils.LoadFileResource;

/**
 * @author junzhou
 */
@Data
@Log4j2
public class Config {
    private static Config CONFIG = new Config();
    private boolean skipDailyTask;
    private int predictNumberOfCoins;
    private int minimumNumberOfCoins;
    //1 买赔率大的, 0-买赔率小的
    private Integer odds;
    //赔率相等是否购买 false 不购买, true 购买
    private boolean equalsBuy = false;
    public static Config getInstance() {
        return CONFIG;
    }

    public int getPredictNumberOfCoins() {
        if (predictNumberOfCoins > 10) {
            predictNumberOfCoins = 10;
        }
        return predictNumberOfCoins;
    }

    @Override
    public String toString() {
        return "Config{" +
                "skipDailyTask=" + skipDailyTask +
                ", predictNumberOfCoins=" + predictNumberOfCoins +
                ", minimumNumberOfCoins=" + minimumNumberOfCoins +
                ", odds=" + odds +
                ", equalsBuy=" + equalsBuy +
                '}';
    }

    public void configInit(String configJson) {
        Config.CONFIG = new Gson().fromJson(configJson, Config.class);
        log.info(Config.getInstance().toString());
    }

    public void configInit() {
        String configJson = null;
        String outConfig = LoadFileResource.loadConfigJsonFromFile();
        if (outConfig != null) {
            configJson = outConfig;
            log.info("读取外部配置文件成功");
        } else {
            String temp = LoadFileResource.loadJsonFromAsset("config.json");
            configJson = temp;
            log.info("读取配置文件成功");
        }

        Config.CONFIG = new Gson().fromJson(configJson, Config.class);
        log.info(Config.getInstance().toString());
    }
}
