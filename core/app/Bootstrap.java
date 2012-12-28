import helper.Currency;
import helper.HTTP;
import helper.S3;
import jobs.ListingSchedulJob;
import jobs.loop.OsTicketCreateCheck;
import models.Privilege;
import models.User;
import models.finance.FeeType;
import models.market.Account;
import play.Play;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

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
            OsTicketCreateCheck.begin();
        }

        if(Play.mode.isProd()) {
            Currency.updateCRY();// 系统刚刚启动以后进行一次 Currency 的更新.
            Account.init();
            new ListingSchedulJob().now();
        }
    }
}
