package controllers;

import exception.NotLoginFastException;
import helper.Webs;
import models.PageInfo;
import models.market.Account;
import models.market.Feedback;
import models.market.Orderr;
import notifiers.Mails;
import org.joda.time.DateTime;
import play.data.validation.Error;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理 Feedbacks 的功能
 * User: wyattpan
 * Date: 3/15/12
 * Time: 1:41 PM
 */
@With({Secure.class, GzipFilter.class})
public class Feedbacks extends Controller {
    public static void index(Integer p, Integer s) {
        Webs.fixPage(p, s);
        List<Feedback> feds = Feedback.find("ORDER BY state ASC, score ASC, createDate DESC").fetch(p, s);
        List<Account> accs = Account.all().fetch();
        Map<String, Long> accFeds = new HashMap<String, Long>();


        long count = Feedback.count();
        for(Account ac : accs) {
            for(Account.M m : Account.M.values()) {
                accFeds.put(ac.id + "d7" + m.name(), Feedback.feedbackCount(7, ac, m, 3f, null));
                accFeds.put(ac.id + "d30" + m.name(), Feedback.feedbackCount(30, ac, m, 3f, null));
                accFeds.put(ac.id + "dall" + m.name(), Feedback.feedbackCount(0, ac, m, 3f, null));
                accFeds.put(ac.id + "d7h" + m.name(), Feedback.feedbackCount(7, ac, m, 3f, Feedback.S.HANDLING));
                accFeds.put(ac.id + "d30h" + m.name(), Feedback.feedbackCount(30, ac, m, 3f, Feedback.S.HANDLING));
                accFeds.put(ac.id + "dallh" + m.name(), Feedback.feedbackCount(0, ac, m, 3f, Feedback.S.HANDLING));
            }
        }

        PageInfo<Feedback> pi = new PageInfo<Feedback>(s, count, p, feds);
        render(feds, pi, accs, accFeds);
    }

    /**
     * 展现出[指定状态],[指定离现在 N 天前] 的 feedback
     *
     * @param s
     * @param d
     */
    public static void list(Feedback.S s, Integer d, Account.M m) {
        List<Feedback> feds = null;
        if(d == null || d == 0)
            feds = Feedback.find("state=? AND score<=3 AND market=? ORDER BY state ASC, score ASC, createDate DESC", s, m).fetch();
        else {
            DateTime dt = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
            Date bdt = dt.plusDays(d).toDate();
            feds = Feedback.find("state=? AND market=? AND score<=3 AND createDate>=? AND createDate<=? ORDER BY state ASC, score ASC, createDate DESC", s, m, bdt, dt.toDate()).fetch();
        }
        render(feds);
    }

    /**
     * 手动抓取 Feedback
     *
     * @param market
     * @param acc
     * @param page
     */
    public static void feedback(String market, Account acc, int page) {
        if(!acc.isPersistent()) renderJSON(new Error("Account", "Account is not persistent!", new String[]{}));
        try {
            acc.changeRegion(Account.M.val(market));
        } catch(NotLoginFastException e) {
            acc.loginWebSite(); // 没有登陆, 那么则登陆一次.
            acc.changeRegion(Account.M.val(market));
        }
        List<Feedback> feedbacks = acc.fetchFeedback(page);

        // 这段代码在  FeedbackCrawlJob 也使用了, 但不好将其抽取出来
        for(Feedback f : feedbacks) {
            f.orderr = Orderr.findById(f.orderId);
            f.account = acc;
            f.merge()._save(); // 系统中有则更新, 没有则创建
            if(f.score <= 3 && f.state == Feedback.S.HANDLING) Mails.feedbackWarnning(f);
        }
        Map<String, String> rt = new HashMap<String, String>();
        rt.put("flag", "true");
        rt.put("count", feedbacks.size() + "");
        renderJSON(rt);
    }

    public static void update(Feedback f) {
        if(!f.isPersistent()) renderJSON(new Error("Feedback", "Feedback is not persistent!", new String[]{}));
        try {
            f.save();
        } catch(Exception e) {
            renderJSON(new Error("Exception", Webs.E(e), new String[]{}));
        }
        renderJSON("{\"flag\":\"true\"}");
    }
}
