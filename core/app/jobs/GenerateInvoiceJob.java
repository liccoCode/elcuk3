package jobs;

import models.market.OrderInvoice;
import play.Logger;
import play.jobs.Job;

import java.util.Calendar;
import java.util.Date;

/**
 * 后台自动生成发票功能, 预计每月1W张
 * Created by licco on 16/6/2.
 */
public class GenerateInvoiceJob extends Job {

    @Override
    public void doJob() throws Exception {
        Logger.info("启动自动生成发票Job...");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        OrderInvoice.createInvoicePdf(0, calendar.getTime(), null);
    }

}
