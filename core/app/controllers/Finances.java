package controllers;

import jobs.AmazonFinancePatchJob;
import jobs.AmazonOrderFetchJob;
import jobs.AmazonOrderUpdateJob;
import models.finance.SaleFee;
import models.market.Account;
import models.market.JobRequest;
import models.view.Ret;
import play.data.validation.Error;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 与财务有关的操作
 * User: wyattpan
 * Date: 3/20/12
 * Time: 10:11 AM
 */
@With({GlobalExceptionHandler.class, Secure.class})
@Check("finances")
public class Finances extends Controller {


    @Check("finances.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        render(accs);
    }

    @Check("finances.fixfinance")
    public static void fixFinance(File file, long accId) {
        Account acc = Account.findById(accId);
        if(acc == null)
            error("Error Account Id");

        AmazonFinancePatchJob worker = new AmazonFinancePatchJob(acc, new Date());
        Map<String, List<SaleFee>> feeMap = SaleFee.flatFileFinanceParse(file, acc);
        worker.parseToDB(feeMap);
        renderText("Deals %s Orders.", feeMap.keySet().size());
    }

    public static void settlement(long accId) {
        Account acc = Account.findById(accId);
        AmazonFinancePatchJob worker = new AmazonFinancePatchJob(acc, new Date());
        List<Error> errors = await(worker.now());
        if(errors.size() > 0) {
            renderJSON(new Ret(false, errors.toString()));
        } else {
            renderJSON(new Ret(true, "成功处理"));
        }
    }

    @Check("finances.reparseorder")
    public static void reParseOrder(File file, Account acc) {
        String name = file.getName();
        JobRequest req = new JobRequest();
        req.account = acc;
        req.path = file.getAbsolutePath();
        if(name.contains("txt")) {
            new AmazonOrderUpdateJob().callBack(req);
        } else if(name.contains("xml")) {
            new AmazonOrderFetchJob().callBack(req);
        }
        renderText("已经处理.");
    }

}
