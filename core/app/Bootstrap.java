import helper.*;
import jobs.JobsSetup;
import jobs.ListingSchedulJob;
import models.ElcukConfig;
import models.OperatorConfig;
import models.Privilege;
import models.User;
import models.finance.FeeType;
import models.market.Account;
import models.market.ListingStateRecord;
import org.apache.commons.lang.StringUtils;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * 项目启动的时候的数据初始化.
 * User: wyattpan
 * Date: 1/13/12
 * Time: 11:10 AM
 */
@OnApplicationStart
public class Bootstrap extends Job {
    private static Map<String, String> ENV_MSG = new HashMap<>();

    static {
        ENV_MSG.put("DB_HOST", "无法连接数据库");
        ENV_MSG.put("DB_NAME", "不知道数据库名称");
        ENV_MSG.put("DB_PASS", "不知道数据库密码");
        ENV_MSG.put("REDIS_HOST", "无法连接 Redis 实例");

        // ex: http://10.129.129.23:4567
        ENV_MSG.put(Constant.ROCKEND_HOST, "无法连接后端任务 Rockend 实例");
        // ex: http://47.88.6.96:9000
        ENV_MSG.put(Constant.ROOT_URL, "没有指明系统的 HOST URL");
        // ex: http://47.88.6.96:8080
        ENV_MSG.put(Constant.KOD_HOST, "没有指明系统的附件服务器 Kod 实例 Host");
        ENV_MSG.put(Constant.ETRACKER_HOST, "没有指定所使用的 Etracker 服务器");
        ENV_MSG.put(Constant.ES_INDEX, "没有指明系统在 ElasticSearch 中所使用的索引名字(Index)");
        ENV_MSG.put(Constant.ES_HOST, "没有指明系统在 ElasticSearch 实例 Host");
    }

    @Override
    public void doJob() throws Exception {
        validElcuk2ENV();
        // 设置为 北京 时间;整个系统内的时间, 以北京时间为准
        TimeZone.setDefault(Dates.timeZone(null).toTimeZone());

        if(Play.id.equalsIgnoreCase("test")) {
            return;
        }
        // 1. 初始化系统内的用户
        long users = User.count();
        if(users == 0) {
            Fixtures.delete(User.class);
            Fixtures.loadModels("users.yml");
        }

        long feeTypes = FeeType.count();
        if(feeTypes == 0) {
            Fixtures.delete(FeeType.class);
            Fixtures.loadModels("feetypes.yml");
        }


        /**
         * 初始化日志类
         */
        OperatorConfig.init();
        LogUtils.initLog();
        HTTP.init();
        Privilege.init();
        JobsSetup.init();
        Account.initOfferIds();
        ElcukConfig.init();
        Caches.clearRedisRunningKeys();

        /**
         * 流程activiti的初始化
         */
        ActivitiEngine.initEngine();

        /**
         * 为所有 Listing 做一个状态的变化过程记录的初始化
         */
        ListingStateRecord.initAllListingRecords();

        if(Play.mode.isProd()) {
            Currency.updateCRY();// 系统刚刚启动以后进行一次 Currency 的更新.
            Account.initLogin();
            new ListingSchedulJob().now();
        }
    }

    public void validElcuk2ENV() throws Exception {
        validENV("DB_HOST");
        validENV("DB_NAME");
        validENV("REDIS_HOST");
        validENV("DB_PASS");
        validENV(Constant.ROCKEND_HOST);
        validENV(Constant.ROOT_URL);
        validENV(Constant.ES_INDEX);
        validENV(Constant.KOD_HOST);
        validENV(Constant.ETRACKER_HOST);
    }

    public void validENV(String env) throws Exception {
        if(StringUtils.isBlank(System.getenv(env))) {
            throw new Exception("环境变量 " + env + " 没有初始化, " + ENV_MSG.get(env) + ".");
        }
    }
}
