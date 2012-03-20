package jobs;

import helper.Currency;
import helper.HTTP;
import models.User;
import models.finance.FeeType;
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
public class OnStartUp extends Job {
    @Override
    public void doJob() throws Exception {
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
        Currency.updateCRY();// 系统刚刚启动以后进行一次 Currency 的更新.
    }
}
