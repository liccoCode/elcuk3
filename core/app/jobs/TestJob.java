package jobs;

import models.Jobex;
import play.jobs.Every;
import play.jobs.Job;

/**
 * 例子代码
 * User: wyattpan
 * Date: 9/21/12
 * Time: 5:43 PM
 */
//@Every("1s")
public class TestJob extends Job {

    @Override
    public void doJob() {
        if(Jobex.findByClassName(TestJob.class.getName()).isExcute())
            System.out.println("Execute!");
        else
            System.out.println("No!!!!!!!");

    }
}
