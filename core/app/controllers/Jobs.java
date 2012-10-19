package controllers;

import helper.Webs;
import jobs.AmazonFBAQtySyncJob;
import jobs.AmazonOrderUpdateJob;
import jobs.SellingRecordCheckJob;
import jobs.works.ListingReviewsWork;
import models.Jobex;
import models.market.*;
import models.view.Ret;
import notifiers.Mails;
import org.joda.time.DateTime;
import play.data.validation.Validation;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 12/29/11
 * Time: 12:43 AM
 */
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
@Check("root")
public class Jobs extends Controller {

    @Before(only = {"index", "update", "create"})
    public static void indexPage() {
        renderArgs.put("jobs", Jobex.all().<Jobex>fetch());
        renderArgs.put("jobReqs", JobRequest.find("ORDER BY requestDate DESC").<Jobex>fetch(30));
    }

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
        List<Selling> sellings = Selling.all().fetch();
        DateTime dt = new DateTime(begin);
        SellingRecordCheckJob srcj = new SellingRecordCheckJob();
        for(int i = 0; i < days; i++) {
            srcj.checkOneDaySellingRecord(sellings, dt.plusDays(i).toDate());
            srcj.fixTime = dt.plusDays(i);
            srcj.amazonNewestRecords();
        }
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

    /**
     * 根据最新从 Amazon 同步回来的 MANAGE_FBA_INVENTORY_ARCHIVED .csv 文件对 fnSku 进行同步更新
     */
    public static void fbaFnSkuFix() {
        List<Account> accs = Account.openedSaleAcc();

        List<F.T4<String, String, String, String>> unfindSelling = new ArrayList<F.T4<String, String, String, String>>();
        for(Account acc : accs) {
            JobRequest job = JobRequest.newEstJobRequest(JobRequest.T.MANAGE_FBA_INVENTORY_ARCHIVED, acc, JobRequest.S.CLOSE);
            List<F.T3<String, String, String>> mskuAndFnSkuAndAsins = AmazonFBAQtySyncJob.fbaCSVParseFnSku(new File(job.path));
            for(F.T3<String, String, String> mfa : mskuAndFnSkuAndAsins) {
                Selling selling = Selling.find("merchantSKU=? AND account=? AND asin=?", mfa._1, acc, mfa._3).first();
                if(selling == null)
                    unfindSelling.add(new F.T4<String, String, String, String>(mfa._1, mfa._2, mfa._3, acc.prettyName()));
                else {
                    selling.fnSku = mfa._2;
                    selling.save();
                }
            }
        }
        if(unfindSelling.size() > 0) Mails.fnSkuCheckWarn(unfindSelling);
        renderJSON(new Ret(true, "处理完毕"));
    }

    public static void reviewFix(String asin, String m) {
        M market = M.val(m);
        try {
            new ListingReviewsWork(Listing.lid(asin, market)).now().get(20, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new FastRuntimeException(Webs.S(e));
        }
        renderJSON(new Ret("成功运行."));
    }
}
