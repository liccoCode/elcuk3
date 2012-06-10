package controllers;

import helper.Constant;
import helper.Webs;
import models.Ret;
import models.finance.SaleFee;
import models.market.Account;
import org.apache.commons.io.FileUtils;
import play.Logger;
import play.data.validation.Error;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 与财务有关的操作
 * User: wyattpan
 * Date: 3/20/12
 * Time: 10:11 AM
 */
@With({Secure.class, GzipFilter.class})
public class Finances extends Controller {

    /**
     * 用来修复手动更新 Amazon 的定时 Payment 报表
     *
     * @param n
     */
    @Check("root")
    public static void fix(String n, String m, Account a) {
        try {
            if(!a.isPersistent()) renderText("Account is not Persistent!");
            List<SaleFee> fees = SaleFee.flat2FinanceParse(new File(Constant.D_FINANCE + "/fix/" + n + ".txt"), a, Account.M.val(m));
            SaleFee.clearOldSaleFee(fees);
            SaleFee.batchSaveWithJDBC(fees);
            renderText("Saved: " + fees.size() + " fees");
        } catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Logger.error(sw.toString());
            renderText(Webs.E(e) + "\r\n<br/><br/>" + sw.toString());
        }
    }

    public static void index() {
        render();
    }

    /**
     * 上传文件解析用的文件
     *
     * @param f
     */
    public static void upload(File f) {
        String path = Constant.D_FINANCE + "/fix/" + f.getName().split("\\.")[0] + ".txt";
        try {
            FileUtils.copyFile(f, new File(path));
        } catch(IOException e) {
            renderJSON(new Error("Exception", Webs.E(e), new String[]{}));
        }
        Map<String, String> rt = new HashMap<String, String>();
        rt.put("flag", "true");
        rt.put("path", path);
        rt.put("size", f.getTotalSpace() + "");
        renderJSON(rt);
    }

    /**
     * @param rid
     * @param acc
     * @param m
     */
    @Check("root")
    public static void flatV2(String rid, Account acc, String m) {
        if(!acc.isPersistent()) renderJSON(new Ret("Account 不存在, 错误!"));
        Account.M market = Account.M.val(m);
        if(market == null) renderJSON(new Ret("Account is invalid!"));
        try {
            List<SaleFee> fees = SaleFee.flat2FinanceParse(acc.briefFlatV2Finance(market, rid), acc, market);
            SaleFee.clearOldSaleFee(fees);
            SaleFee.batchSaveWithJDBC(fees);
            renderJSON(new Ret(true, "Saved: " + fees.size() + " fees"));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.S(e)));
        }
    }
}
