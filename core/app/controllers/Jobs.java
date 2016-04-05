package controllers;

import controllers.api.SystemOperation;
import helper.Webs;
import jobs.AmazonOrderUpdateJob;
import jobs.promise.FeedbackFixPromise;
import jobs.promise.SellingRecordFixPromise;
import models.Jobex;
import models.market.JobRequest;
import models.view.Ret;
import play.data.validation.Validation;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 * @deprecated 任务被放弃
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
public class Jobs extends Controller {

    @Before(only = {"index", "update", "create"})
    public static void indexPage() {
        renderArgs.put("jobs", Jobex.all().<Jobex>fetch());
        renderArgs.put("jobReqs", JobRequest.find("ORDER BY requestDate DESC").<Jobex>fetch(30));
    }

    @Check("jobs.index")
    public static void index() {
        render();
    }

    public static void create(Jobex job) {
        validation.valid(job);
        job.validate();
        if(Validation.hasErrors()) render("Jobs/index.html");
        flash.success("Job %s 添加成功", job.className);
        job.save();
        redirect("/Jobs/index");
    }

    public static void update(Jobex job) {
        validation.required(job.id);
        validation.valid(job);
        job.validate();
        if(Validation.hasErrors()) render("Jobs/index.html");
        flash.success("Job %s 更新成功", job.className);
        job.save();
        redirect("/Jobs/index");
    }

    public static void now(long id) {
        validation.required(id);
        Jobex job = Jobex.findById(id);
        if(Validation.hasErrors() || job == null) {
            flash.error("JobId %s 不存在!", id);
            redirect("/Jobs/index");
        }
        try {
            job.now();
        } catch(Exception e) {
            flash.error("Job %s 因 [%s] 执行失败.", job.className, Webs.E(e));
        }
        flash.success("Job %s 执行成功", job.className);
        redirect("/Jobs/index");
    }

    // -------------------- Small Fix Method --------------------

    /**
     * 对 SellingRecord 进行修补处理, 从 begin 开始, 抓取 days 天的 Amazon SellingReord 数据
     *
     * @param begin
     * @param days
     */
    public static void sellingRecordFix(Date begin, int days) {
        new SellingRecordFixPromise(begin, days).now();
        renderText("任务已提交, 完成后有 Notification 提醒");
    }

    /**
     * 修复某一个市场上的 Feedback 数据
     *
     * @param id
     * @param page
     */
    public static void feedbackFix(final long id, final int page) {
        new FeedbackFixPromise(id, page).now();
        renderText("任务已提交, 完成后有 Notification 提醒");
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
