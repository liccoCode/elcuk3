package jobs;

import helper.HTTP;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

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
        HTTP.init();
    }
}
