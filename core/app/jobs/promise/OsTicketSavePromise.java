package jobs.promise;

import com.alibaba.fastjson.TypeReference;
import com.trendrr.beanstalk.BeanstalkJob;
import helper.J;
import models.support.Ticket;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import play.Logger;
import play.jobs.Job;

import java.util.Map;

/**
 * 将解析 OsTicket 与保存的任务交给这个 Promise (因为需要 JPA Content)
 * User: wyattpan
 * Date: 11/8/12
 * Time: 2:05 PM
 */
public class OsTicketSavePromise extends Job<Ticket> {
    private BeanstalkJob job;

    /**
     * 将解析 OsTicket 与保存的任务交给这个 Promise (因为需要 JPA Content)
     *
     * @param job
     */
    public OsTicketSavePromise(BeanstalkJob job) {
        this.job = job;
    }

    @Override
    public Ticket doJobWithResult() {
        String body = new String(job.getData());
        Map<String, String> t = J.from(body, new TypeReference<Map<String, String>>() {});

        Ticket ticket = Ticket.find("osTicketId=?", t.get("ticketId")).first();
        if(ticket == null) {
            ticket = new Ticket(t.get("ticketId"), DateTime.parse(t.get("createAt"), DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate(), t.get("title"));
            ticket.save();
            Logger.info("OsTicket #%s(%s) [%s] is exist.", ticket.osTicketId, ticket.id, ticket.fid);
        } else {
            Logger.info("OsTicket #%s(%s) [%s] is exist.", ticket.osTicketId, ticket.id, ticket.fid);
        }
        return ticket;
    }
}
