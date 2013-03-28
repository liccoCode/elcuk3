import helper.Currency;
import helper.Dates;
import helper.HTTP;
import helper.S3;
import jobs.ListingSchedulJob;
import jobs.loop.OsTicketBeanstalkdCheck;
import models.Privilege;
import models.User;
import models.finance.FeeType;
import models.market.Account;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

import java.util.TimeZone;

/**
 * 项目启动的时候的数据初始化.
 * User: wyattpan
 * Date: 1/13/12
 * Time: 11:10 AM
 */
@OnApplicationStart
public class Bootstrap extends Job {
    @Override
    public void doJob() throws Exception {
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


        HTTP.init();
        Privilege.init();
        S3.init();

        if(Play.mode.isProd() || (Play.mode.isDev() &&
                "true".equalsIgnoreCase(Play.configuration.getProperty("beanstalkd.dev")))) {
            OsTicketBeanstalkdCheck.begin();
        }

        if(Play.mode.isProd()) {
            Currency.updateCRY();// 系统刚刚启动以后进行一次 Currency 的更新.
            Account.init();
            new ListingSchedulJob().now();
        }
    }
}
