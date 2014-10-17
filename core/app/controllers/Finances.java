package controllers;

import controllers.api.SystemOperation;
import jobs.AmazonOrderFetchJob;
import jobs.AmazonOrderUpdateJob;
import jobs.promise.FinanceShippedPromise;
import models.finance.SaleFee;
import models.market.Account;
import models.market.JobRequest;
import models.market.M;
import org.apache.commons.lang3.StringUtils;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 与财务有关的操作
 * User: wyattpan
 * Date: 3/20/12
 * Time: 10:11 AM
 */
@With({GlobalExceptionHandler.class, Secure.class,SystemOperation.class})
@Check("finances")
public class Finances extends Controller {


    @Check("finances.index")
    public static void index() {
        List<Account> accs = Account.openedSaleAcc();
        render(accs);
    }

//    @Check("finances.fixfinance")

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

    public static void promotion(String orderId, long aid, String m) {
        List<String> orderIds = Arrays.asList(StringUtils.split(orderId, ","));
        Account acc = Account.findById(aid);
        M market = M.val(m);
        try {
            List<SaleFee> fees = new FinanceShippedPromise(acc, market, orderIds, 10).now().get();
            renderText(fees);
        } catch(Exception e) {
            renderText(e.getMessage());
        }
    }

}
