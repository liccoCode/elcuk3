package models.market;

import com.alibaba.fastjson.JSONObject;
import helper.AmazonSQS;
import helper.Dates;
import helper.J;
import play.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Even
 * Date: 17/12/6
 * Time: AM9:42
 */
public class Jrockend {

    public static void orderProcess(Map<String, Object> jobMap, Map<String, Object> jobParameters,
                                    Date from, Date to, Integer splitDate, String market) {
        //如果未传入分割天数,那么系统根据 每日市场的销量确定抓取的时间段
        if (splitDate == null) {
            splitDate = getSplitDate(M.valueOf(market));
        }
        if (splitDate == 0) {
            return;
        }

        int day = (int) ((to.getTime() - from.getTime()) / (1000 * 3600 * 24));
        if (day > splitDate) {
            //开始结束时间大于分割日期,那么需要按照分割日期分割,按端执行
            int i = 1;
            Map<String, String> map = Dates.splitDayForDate(from, to, splitDate);
            for (String key : map.keySet()) {
                sendSqs(jobMap, jobParameters, key, map.get(key), i * 60, market);
                i++;
            }
        } else {
            //开始结束时间小于分割日期,那么直接执行
            sendSqs(jobMap, jobParameters, new SimpleDateFormat("yyyy-MM-dd").format(from),
                    new SimpleDateFormat("yyyy-MM-dd").format(to), 60, market);
        }

    }


    public static void sendSqs(Map<String, Object> jobMap, Map<String, Object> jobParameters,
                               String from, String to, Integer delaySeconds, String market) {
        jobParameters.put("beginDate", from);
        jobParameters.put("endDate", to);
        jobParameters.put("marketErp", market);
        jobMap.put("args", jobParameters);
        Logger.info("订单抓取sqs：[" + J.json(jobParameters) + "],延迟[" + delaySeconds + "]秒后发送");
        AmazonSQS.sendMessage(JSONObject.toJSONString(jobMap), delaySeconds);

    }


    /**
     * 根据每日市场的销量确定抓取的时间段
     *
     * @param market
     * @return
     */
    public static int getSplitDate(M market) {
        //加拿大 40     30天分割
        //墨西哥  20    30天分割
        //日本   60     30天分割
        //美国   800    15天分割
        //欧洲五国 同一个账号跑一次德国账号即可  2天分割
        switch (market) {
            case AMAZON_CA:
            case AMAZON_JP:
            case AMAZON_MX:
                return 30;
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_ES:
            case AMAZON_DE:
            case AMAZON_UK:
                return 2;
            case AMAZON_US:
                return 16;
            default:
                return 0;
        }
    }

}
