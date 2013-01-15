package jobs.promise;

import com.alibaba.fastjson.TypeReference;
import com.trendrr.beanstalk.BeanstalkJob;
import helper.J;
import helper.Webs;
import jobs.loop.OsTicketBeanstalkdCheck;
import models.support.Ticket;
import org.apache.commons.lang.StringUtils;
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
    private String tube;

    /**
     * 将解析 OsTicket 与保存的任务交给这个 Promise (因为需要 JPA Content)
     *
     * @param job
     */
    public OsTicketSavePromise(String tube, BeanstalkJob job) {
        this.job = job;
        this.tube = tube;
    }

    @Override
    public Ticket doJobWithResult() throws Exception {
        Ticket ticket = null;
        try {

            String body = new String(job.getData());
            Map<String, String> t = J.from(body, new TypeReference<Map<String, String>>() {});

            String title = t.get("title");
            if(t.get("title").contains("日报")) {
                Logger.info("Skip 日报 Ticket create.");
                // 日报不进入系统
                OsTicketBeanstalkdCheck.deleteJob(job);
                return null;
            }
            // TODO 需要调整 OsTicket,还需要返回 Ticket 类型
            if(title.contains("einen negativen Testbericht") ||
                    title.contains("negative product review") ||
                    title.contains("negative feedback")) {
                Logger.info("Skip Feedback/Review Ticket create.");
                OsTicketBeanstalkdCheck.deleteJob(job);
                return null;
            }

            String topic = t.get("topic");
            if(StringUtils.isNotBlank(topic)) {
                topic = topic.toLowerCase();
                if(topic.contains("feedback") || topic.contains("review")) {
                    Logger.info("Skip [%s] Ticket create.", topic);
                    OsTicketBeanstalkdCheck.deleteJob(job);
                    return null;
                }
            }

            ticket = Ticket.find("osTicketId=?", t.get("ticketId")).first();
            if(ticket == null) {
                //TODO 检查为什么没有存储进来
                ticket = new Ticket(t.get("ticketId"), DateTime.parse(t.get("createAt"),
                        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")).toDate(), t.get("title"));
                ticket.save();
                Logger.info("Saved OsTicket #%s(%s) [%s].", ticket.osTicketId, ticket.id,
                        ticket.fid);
            } else {
                Logger.info("OsTicket #%s(%s) [%s] is exist.", ticket.osTicketId, ticket.id,
                        ticket.fid);
            }
            OsTicketBeanstalkdCheck.deleteJob(job);
        } catch(Exception e) {
            // 延迟 10s
            Logger.warn("Why`s wrong? %s", Webs.E(e));
            OsTicketBeanstalkdCheck.releaseJob(job);
        }
        return ticket;
    }
}
