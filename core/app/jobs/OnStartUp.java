package jobs;

import models.User;
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
            User.deleteAll();
            Fixtures.loadModels("users.yml");
        }
    }
}
