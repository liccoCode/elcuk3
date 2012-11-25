package controllers;

import jobs.promise.FinanceCrawlPromise;
import models.finance.SaleFee;
import models.market.Account;
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


    public static void index() {
        render();
    }

    //TODO 需要权限
    public static void fixFinance(File file, long accId) {
        Account acc = Account.findById(accId);
        if(acc == null)
            error("Error Account Id");

        FinanceCrawlPromise worker = new FinanceCrawlPromise(acc, new Date());
        Map<String, List<SaleFee>> feeMap = SaleFee.flatFileFinanceParse(file, acc);
        worker.parseToDB(feeMap);
        renderText("Deals %s Orders.", feeMap.keySet().size());
    }
}
