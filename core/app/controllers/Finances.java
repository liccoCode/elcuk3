package controllers;

import helper.Constant;
import models.finance.SaleFee;
import models.market.Account;
import play.db.jpa.JPA;
import play.libs.Files;
import play.mvc.Controller;
import play.mvc.With;

import java.io.File;
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
     * @param name
     */
    public static void fix(String name) {
        try {
            List<SaleFee> fees = SaleFee.flat2FinanceParse(new File(Constant.E_FINANCE + "/fix/" + name + ".txt"), Account.<Account>findById(1l));
            SaleFee.clearOldSaleFee(fees);
            for(int i = 0; i < fees.size(); i++) {
                if(i % 100 == 0) JPA.em().flush();
                fees.get(i).save();
            }
            renderText("Saved: " + fees.size() + " fees");
        } catch(Exception e) {
            renderText(e.getClass().getSimpleName() + "|" + e.getMessage());
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
        String path = Constant.E_FINANCE + "/fix/" + f.getName().split("\\.")[0] + ".txt";
        Files.copy(f, new File(path));
        Map<String, String> rt = new HashMap<String, String>();
        rt.put("flag", "true");
        rt.put("path", path);
        rt.put("size", f.getTotalSpace() + "");
        renderJSON(rt);
    }
}
