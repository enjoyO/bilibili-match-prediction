package top.misec;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.log4j.Log4j2;
import top.misec.config.Config;
import top.misec.login.ServerVerify;
import top.misec.login.Verify;
import top.misec.task.DailyTask;
import top.misec.task.ServerPush;
import top.misec.utils.VersionInfo;

/**
 * @author junzhou
 */
@Log4j2
public class Main {
    public static void main(String[] args) {
        if (args.length < 3) {
            log.info("任务启动失败");
            log.warn("Cookies参数缺失，请检查是否在Github Secrets中配置Cookies参数");
        }
        VersionInfo.printVersionInfo();
        //读取环境变量
        Verify.verifyInit(args[0], args[1], args[2]);
        if (args.length > 4) {
            ServerVerify.verifyInit(args[3], args[4]);
        } else if (args.length > 3) {
            ServerVerify.verifyInit(args[3]);
        }

        Config.getInstance().configInit();

        if (Config.getInstance().isSkipDailyTask()) {
            log.info("已配置跳过本日任务，本次执行将不会发起任何网络请求");
            ServerPush.doServerPush();
        } else {
            DailyTask dailyTask = new DailyTask();
            dailyTask.doDailyTask();
        }
    }

    /**
     * 用于腾讯云函数触发
     */
    public String mainHandler(KeyValueClass ignored) {

        String config = System.getProperty("config");
        if (null == config) {
            System.out.println("取config配置为空！！！");
            return "error config";
        }
        KeyValueClass kv;
        try {
            kv = new Gson().fromJson(config, KeyValueClass.class);
        } catch (JsonSyntaxException e) {
            System.out.println("JSON配置反序列化失败，请检查");
            e.printStackTrace();
            return "error json config";
        }
        /**
         *   读取环境变量
         */
        Verify.verifyInit(kv.getDedeuserid(), kv.getSessdata(), kv.getBiliJct());

        if (null != kv.getTelegrambottoken() && null != kv.getTelegramchatid()) {
            ServerVerify.verifyInit(kv.getTelegrambottoken(), kv.getTelegramchatid());
        } else if (null != kv.getServerpushkey()) {
            ServerVerify.verifyInit(kv.getServerpushkey());
        }


        VersionInfo.printVersionInfo();
        //每日任务65经验
        Config.getInstance().configInit(new Gson().toJson(kv));
        if (!Boolean.TRUE.equals(Config.getInstance().isSkipDailyTask())) {
            DailyTask dailyTask = new DailyTask();
            dailyTask.doDailyTask();
        } else {
            log.info("已开启了跳过本日任务，本日任务跳过（不会发起任何网络请求），如果需要取消跳过，请将skipDailyTask值改为false");
            ServerPush.doServerPush();
        }
        return "success";
    }
}
