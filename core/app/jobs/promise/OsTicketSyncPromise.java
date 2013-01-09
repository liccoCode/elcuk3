package jobs.promise;

import com.alibaba.fastjson.TypeReference;
import com.trendrr.beanstalk.BeanstalkJob;
import helper.J;
import jobs.loop.OsTicketBeanstalkdCheck;
import models.support.Ticket;
import play.Logger;
import play.jobs.Job;

import java.util.Map;

/**
 * 用于同步系统与 OsTicket 的 Ticket
 * User: wyatt
 * Date: 1/8/13
 * Time: 2:06 PM
 */
public class OsTicketSyncPromise extends Job<Ticket> {
    private String tube;
    private BeanstalkJob job;

    public OsTicketSyncPromise(String tube, BeanstalkJob job) {
        this.tube = tube;
        this.job = job;
    }

    @Override
    public Ticket doJobWithResult() throws Exception {
        Ticket ticket = null;
        try {
            String body = new String(job.getData());
            Map<String, String> t = J.from(body, new TypeReference<Map<String, String>>() {});
            ticket = Ticket.find("osTicketId=?", t.get("ticketId")).first();
            if(ticket != null) {
                ticket.syncFromOsticket();
                Logger.info("Synchronize OsTicket #%s(%s) [%s].", ticket.osTicketId, ticket.id,
                        ticket.fid);
            } else {
                // 如果是新创建的 Ticket, 那么很可能 Save 与 Sync Job 一起触发, 而这一次的 Sync 忽略无大碍
                Logger.info("OsTicket %s is not exist, can not be synchronize.", t.get("ticketId"));
            }
            job.getClient().deleteJob(job);
        } catch(Exception e) {
            // 延迟 10s
            job.getClient().release(job, OsTicketBeanstalkdCheck.DEFAULT_PRI, 10);
        }
        return ticket;
    }
}
