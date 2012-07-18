package controllers;

import helper.Webs;
import jobs.AmazonOrderUpdateJob;
import jobs.SellingRecordCheckJob;
import models.Jobex;
import models.market.JobRequest;
import models.market.Selling;
import models.view.Ret;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.libs.Time;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 */
@With({Secure.class, GzipFilter.class})
@Check("root")
public class Jobs extends Controller {

    public static void index() {
        List<Jobex> jobs = Jobex.all().fetch();
        List<JobRequest> jobReqs = JobRequest.find("ORDER BY requestDate DESC").fetch(30);

        render(jobs, jobReqs);
    }


    public static void c(Jobex j) {
        validation.required(j.className);
        validation.required(j.duration);
        if(Validation.hasErrors()) renderJSON(new Ret(validation.errorsMap()));
        try {
            // 先解析 Cron, 如果 Cron 表达式错误再解析 Duration 如果还错误,则报错
            if(!Time.CronExpression.isValidExpression(j.duration))
                Time.parseDuration(j.duration);
        } catch(IllegalArgumentException e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        j.save();
        renderJSON(j);
    }

    public static void u(Jobex j) {
        validation.required(j.id);
        if(Validation.hasErrors()) renderJSON(new Ret(validation.errorsMap()));
        Jobex job = Jobex.findById(j.id);
        try {
            job.updateJobAttrs(j);
        } catch(Exception e) {
            render(new Ret(Webs.E(e)));
        }
        renderJSON(job);
    }

    public static void now(long id) {
        validation.required(id);
        if(Validation.hasErrors()) renderJSON(validation.errorsMap());
        Jobex job = Jobex.findById(id);
        if(job == null) renderJSON(new Ret("Job is not exist!"));
        try {
            job.now();
        } catch(Exception e) {
            throw new FastRuntimeException(e.getMessage());
        }
        renderJSON(new Ret());
    }

    public static void close(long id) {
        Jobex job = Jobex.findById(id);
        job.close = true;
        job.save();
        renderJSON(job);
    }

    /**
     * 对 SellingRecord 进行修补处理, 从 begin 开始, 抓取 days 天的 Amazon SellingReord 数据
     *
     * @param begin
     * @param days
     */
    public static void sellingRecordFix(Date begin, int days) {
        List<Selling> sellings = Selling.all().fetch();
        DateTime dt = new DateTime(begin);
        SellingRecordCheckJob srcj = new SellingRecordCheckJob();
        for(int i = 0; i < days; i++)
            srcj.checkOneDaySellingRecord(sellings, dt.plusDays(i).toDate());
        renderJSON(new Ret());
    }

    /**
     * 修复 Amazon Shipped Order 修复方法
     *
     * @param path 指定修复文件所在的绝对地址
     */
    public static void shippedOrderFix(String path) {
        long begin = System.currentTimeMillis();
        JobRequest job = new JobRequest();
        job.path = path;
        new AmazonOrderUpdateJob().callBack(job);
        renderJSON(new Ret(true, "耗时 " + (System.currentTimeMillis() - begin) + "ms 更新成功."));
    }
}
