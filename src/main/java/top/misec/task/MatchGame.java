package top.misec.task;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.log4j.Log4j2;
import top.misec.BuyRateEnum;
import top.misec.apiquery.ApiList;
import top.misec.apiquery.OftenAPI;
import top.misec.config.Config;
import top.misec.login.Verify;
import top.misec.utils.HttpUtil;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @author junzhou
 */
@Log4j2
public class MatchGame implements Task {

    @Override
    public void run() throws InterruptedException {

        if (OftenAPI.getCoinBalance() < Config.getInstance().getMinimumNumberOfCoins()) {
            log.info("{}个硬币都没有，参加什么预测呢？任务结束", Config.getInstance().getMinimumNumberOfCoins());
            return;
        }
        JsonObject resultJson = queryContestQuestion(getTime(),1,50);
        JsonObject jsonObject = resultJson.get("data").getAsJsonObject();
        if (resultJson.get("code").getAsInt() == 0) {
            JsonArray list = jsonObject.get("list").getAsJsonArray();
            JsonObject pageinfo = jsonObject.get("page").getAsJsonObject();
            if (pageinfo.get("total").getAsInt() == 0) {
                log.info("今日无赛事或者本日赛事已经截止预测");
                return;
            }
            if (list != null) {
                int coinNumber = Config.getInstance().getPredictNumberOfCoins();
                int contestId;
                String contestName;
                int questionId;
                String questionTitle;
                int teamId=0;
                String teamName=null;
                int seasonId;
                String seasonName;

                for (JsonElement listinfo : list) {
                    log.info("-----预测开始-----");
                    JsonObject contestJson = listinfo.getAsJsonObject().getAsJsonObject("contest");
                    JsonObject questionJson = listinfo.getAsJsonObject().getAsJsonArray("questions")
                            .get(0).getAsJsonObject();
                    contestId = contestJson.get("id").getAsInt();
                    contestName = contestJson.get("game_stage").getAsString();
                    questionId = questionJson.get("id").getAsInt();
                    questionTitle = questionJson.get("title").getAsString();
                    seasonId = contestJson.get("season").getAsJsonObject()
                            .get("id").getAsInt();
                    seasonName = contestJson.get("season").getAsJsonObject()
                            .get("title").getAsString();

                    log.info(seasonName + " " + contestName + ":" + questionTitle);

                    if (questionJson.get("is_guess").getAsInt() == 1) {
                        log.info("此问题已经参与过预测了，无需再次预测");
                        continue;
                    }

                    JsonObject teamA = questionJson.get("details").getAsJsonArray().get(0).getAsJsonObject();
                    JsonObject teamB = questionJson.get("details").getAsJsonArray().get(1).getAsJsonObject();

                    log.info("当前赔率为:  {}:{}", teamA.get("odds").getAsDouble(), teamB.get("odds").getAsDouble());
                    BuyRateEnum rate = BuyRateEnum.getDescByCode(Config.getInstance().getOdds());
                    log.info("当前配置竞猜赔率买:{}",rate.getDesc());
                    boolean big = rate==BuyRateEnum.BIG;
                    boolean equalsBuy = Config.getInstance().isEqualsBuy();
                    if (teamA.get("odds").getAsDouble() == teamB.get("odds").getAsDouble()  ) {
                        if (equalsBuy) {
                            log.info("赔率相等也买");
                            teamId =   teamA.get("detail_id").getAsInt() ;
                            teamName =  teamA.get("option").getAsString();
                        }else{
                            log.info("赔率相等不买");
                        }
                    } else  {
                        boolean greater = teamA.get("odds").getAsDouble() > teamB.get("odds").getAsDouble();
                        if (big) {
                            teamId =greater ?  teamA.get("detail_id").getAsInt() : teamB.get("detail_id").getAsInt() ;
                            teamName = greater ? teamA.get("option").getAsString():teamB.get("option").getAsString();
                        }else{
                            teamId =  greater?  teamB.get("detail_id").getAsInt() : teamA.get("detail_id").getAsInt() ;
                            teamName =  greater? teamB.get("option").getAsString():teamA.get("option").getAsString();
                        }
                    }
                    if (teamId>0) {
                        log.info("拟预测的队伍是:{},预测硬币数为:{}", teamName, coinNumber);
                        doPrediction(contestId, questionId, teamId, coinNumber);
                        taskSuspend();
                    }
                }
            }
        } else {
            log.info("获取赛事信息失败");
        }

    }

    private void taskSuspend() throws InterruptedException {
        Random random = new Random();
        int sleepTime = (int) ((random.nextDouble() + 0.5) * 3000);
        log.info("-----随机暂停{}ms-----\n", sleepTime);
        Thread.sleep(sleepTime);
    }
    private JsonObject queryContestQuestion(String today,int pn,int ps){

        String gid = "";
        String sids = "";
        String urlParam = "?pn=" + pn +
                "&ps=" + ps
                + "&gid=" + gid
                + "&sids=" + sids
                + "&stime=" + today + URLEncoder.encode(" 00:00:00")
                + "&etime=" + today + URLEncoder.encode(" 23:59:59")
                + "&pn=" + pn
                + "&ps=" + ps
                + "&stime=" + today + "+00:00:00"
                + "&etime=" + today + "+23:59:59";
        return HttpUtil.doGet(ApiList.queryQuestions + urlParam);
    }

    private void doPrediction(int oid, int main_id, int detail_id, int count) {
        String requestbody = "oid=" + oid +
                "&main_id=" + main_id
                + "&detail_id=" + detail_id
                + "&count=" + count
                + "&is_fav=0"
                + "&csrf=" + Verify.getInstance().getBiliJct();

        JsonObject result = HttpUtil.doPost(ApiList.doAdd, requestbody);

        if (result.get("code").getAsInt() != 0) {
            log.info(result.get("message").getAsString());
        } else {
            log.info("预测成功");
        }

    }

    private String getTime() {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(d);
    }

    @Override
    public String getName() {
        return "赛事预测";
    }

    public static void test( ) {
        JsonObject teamA = new JsonObject();
        teamA.addProperty("odds",2.33);
        teamA.addProperty("detail_id",1);
        teamA.addProperty("option","A");
        JsonObject teamB =new JsonObject();
        teamB.addProperty("odds",2.34);
        teamB.addProperty("detail_id",2);
        teamB.addProperty("option","B");
        int teamId = 0;
        String teamName = "";
        BuyRateEnum rate = BuyRateEnum.getDescByCode(0);
        log.info("当前配置竞猜赔率买:{}",rate.getDesc());
        boolean big = rate==BuyRateEnum.BIG;
        boolean equalsBuy = true;
        if (teamA.get("odds").getAsDouble() == teamB.get("odds").getAsDouble()  ) {
            if (equalsBuy) {
                log.info("赔率相等也买");
                teamId =   teamA.get("detail_id").getAsInt() ;
                teamName =  teamA.get("option").getAsString();
            }else{
                log.info("赔率相等不买");
            }
        } else  {
            boolean greater = teamA.get("odds").getAsDouble() > teamB.get("odds").getAsDouble();
            if (big) {
                teamId =greater ?  teamA.get("detail_id").getAsInt() : teamB.get("detail_id").getAsInt() ;
                teamName = greater ? teamA.get("option").getAsString():teamB.get("option").getAsString();
            }else{
                teamId =  greater?  teamB.get("detail_id").getAsInt() : teamA.get("detail_id").getAsInt() ;
                teamName =  greater? teamB.get("option").getAsString():teamA.get("option").getAsString();
            }
        }
        System.out.println("teamId:"+teamId);
        System.out.println("teamName:"+teamName);
    }

}
