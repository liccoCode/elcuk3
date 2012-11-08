package beanstalk;

import com.trendrr.beanstalk.BeanstalkClient;
import com.trendrr.beanstalk.BeanstalkException;
import com.trendrr.beanstalk.BeanstalkJob;
import helper.Webs;
import org.junit.Test;
import play.test.UnitTest;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 11/8/12
 * Time: 11:42 AM
 */
public class BeanstalkTest extends UnitTest {
    @Test
    public void see() {
        new Thread() {
            @Override
            public void run() {
                BeanstalkClient bk = new BeanstalkClient("r.easya.cc", 11300, "osticket");
                try {
                    while(true) {
                        BeanstalkJob job = bk.reserve(null);
                        System.out.println(new String(job.getData()));
                        bk.deleteJob(job);
                        System.out.println(bk.tubeStats("osticket"));
                    }
                } catch(BeanstalkException e) {
                    System.out.println(Webs.E(e));
                }
            }
        }.start();
    }
}
