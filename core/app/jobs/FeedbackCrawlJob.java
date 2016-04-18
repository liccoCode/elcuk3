package jobs;

import helper.*;
import models.Jobex;
import models.market.Account;
import models.market.Feedback;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.Play;
import play.data.validation.Validation;
import play.db.DB;
import play.jobs.Job;
import play.libs.IO;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *
 * 用来抓取 Feedback 的线程, 7 小时抓取一次
 * 周期:
 * - 轮询周期: 30mn
 * - Duration: 7h
 * </pre>
 * User: wyattpan
 * Date: 3/15/12
 * Time: 9:34 AM
 * @deprecated
 */
public class FeedbackCrawlJob extends Job {
    /**
     * <pre>
     * 如果是 Europe 的 Account 则进入一个流程
     *   a. 首先抓取 uk 的 feedback
     *   b. 然后抓取 de 的 feedback
     *   c. 最后抓取 fr 的 feedback
     * 否则仅仅抓取自己一个的即可.
     * </pre>
     */
    @Override
    public void doJob() {
        long begin = System.currentTimeMillis();
        Currency.updateCRY();// 每一次的轮训, 都更新一次 CRY
        if(!Jobex.findByClassName(FeedbackCrawlJob.class.getName()).isExcute()) return;

        List<Account> accs = Account.openedSaleAcc();
        for(Account acc : accs) {
            if(acc.type == M.AMAZON_DE) {
                for(M market : Arrays.asList(M.AMAZON_DE, M.AMAZON_ES, M.AMAZON_FR)) {
                    FeedbackCrawlJob.fetchAccountFeedback(acc, market, 5);
                }
            } else {
                FeedbackCrawlJob.fetchAccountFeedback(acc, acc.type, 5);
            }
        }
        if(LogUtils.isslow(System.currentTimeMillis() - begin, "FeedbackCrawlJob")) {
            LogUtils.JOBLOG
                    .info(String.format("FeedbackCrawlJob calculate.... [%sms]", System.currentTimeMillis() - begin));
        }

    }

    /**
     * 抓取某一个市场的 N(现在为 5) 页的 feedback 并且更新或者保存到数据库
     *
     * @param acc
     * @param market
     * @param pages
     */
    public static void fetchAccountFeedback(Account acc, M market, int pages) {
        if(market == null || Arrays.asList(M.EBAY_UK).contains(market)) {
            Logger.warn("不支持市场 [%s] 的 Feedback 抓取", market);
            return;
        }
        if(pages <= 0) pages = 5;
        try {
            synchronized(acc.cookieStore()) { // 将 CookieStore 锁住, 防止更改了 Region 以后有其他的地方进行操作.
                acc.changeRegion(market);
                for(int i = 1; i <= pages; i++) {
                    FeedbackCrawlJob.fetchAccountFeedbackOnePage(acc, market, i);
                }
                // 还原到原来的 Market
                acc.changeRegion(acc.type);
            }
        } catch(Exception e) {
            Logger.warn(String.format("Account %s Market %s fetch feedback have some error![%s]",
                    acc.username, market, e.getMessage()));
        } finally {
            //防止feedback重复发
            try {
                DB.getConnection().commit();
            } catch(Exception e) {
                Logger.warn("db connection [%s].", e.getMessage());
            }
        }
    }

    /**
     * 抓取市场上某一页的 Feedbacks, 并且保存到数据库
     *
     * @param acc
     * @param market
     * @param i
     * @return
     */
    public static List<Feedback> fetchAccountFeedbackOnePage(Account acc, M market, int i) {
        String feedbackHtml = FeedbackCrawlJob.fetchFeedback(acc, i);
        List<Feedback> feedbacks = FeedbackCrawlJob.parseFeedBackFromHTML(feedbackHtml);
        if(feedbacks.size() == 0) {
            Logger.info(String.format("Fetch %s %s, page %s has no more feedbacks.", acc.username, market, i));
        } else {
            Logger.info(String.format("Fetch %s %s, page %s, total %s.", acc.username, market, i, feedbacks.size()));
        }

        for(Feedback feedback : feedbacks) {
            Feedback managed = Feedback.findById(feedback.orderId);
            if(managed == null) {
                try {
                    feedback.checkAndSave(acc);
                    feedback.checkMailAndTicket();
                    Logger.info("Save Feedback %s, score: %s", feedback.orderId, feedback.score);
                } catch(Exception e) {
                    Logger.warn(Webs.E(e) + "|" + J.json(Validation.errors()));
                }
            } else {
                managed.updateAttr(feedback);
                managed.checkMailAndTicket();
                Logger.info("Update Feedback %s, score: %s", feedback.orderId, feedback.score);
            }
        }
        return feedbacks;
    }

    /**
     * 抓取 Account 对应网站的 FeedBack
     *
     * @param page amazon 网站上第 N 页的 Feedback
     * @return
     */
    public static String fetchFeedback(Account acc, int page) {
        String body = HTTP.get(acc.cookieStore(), acc.type.feedbackPage(page));
        String filePath = Constant.E_LOGS + "/" + acc.type.name() + ".id_" + acc.id + "feedback_p" + page + ".html";
        if(Play.mode.isDev()) {
            IO.writeContent(body, new File(filePath));
        }
        return body;
    }

    /**
     * 从 Amazon 的网页上解析出需要的 Feedback 信息
     *
     * @param html
     * @return
     */
    public static List<Feedback> parseFeedBackFromHTML(String html) {
        Document doc = Jsoup.parse(html);
        M market = selectMarket(doc);

        Elements feedbacks = doc.select("td[valign=center][align=middle] tr[valign=center]");
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        for(Element feb : feedbacks) {
            if("#ffffff".equals(feb.attr("bgcolor"))) continue;
            Feedback feedback = new Feedback();
            Elements tds = feb.select("td");
            //time
            if(market == M.AMAZON_US) {
                feedback.createDate = DateTime.parse(tds.get(0).text(), DateTimeFormat.forPattern("MM/dd/yy")).toDate();
            } else {
                feedback.createDate = DateTime.parse(tds.get(0).text(), DateTimeFormat.forPattern("dd/MM/yyyy"))
                        .toDate();
            }
            //score
            feedback.score = NumberUtils.toFloat(StringUtils.replace(tds.get(1).select("b").html(), "&nbsp;", ""));

            // isremove
            Element span = tds.get(1).select("span").first();
            if(span != null && "text-decoration:line-through".equals(span.attr("style"))) {
                feedback.isRemove = true;
            } else {
                feedback.isRemove = false;
            }

            //comments
            feedback.feedback = StringUtils.trim(tds.get(2).childNode(0).toString());
            Element b = tds.get(2).select("b").first();
            if(b != null) {
                // de Dieser Artikel wurde durch Amazon versendet. Amazon übernimmt die Verantwortung für den geleisteten Service
                // uk This item was fulfilled by Amazon, and we take responsibility for this fulfilment experience
                feedback.memo = b.nextSibling().toString();
            }

            //orderid
            feedback.orderId = tds.get(6).text();
            //email
            feedback.email = tds.get(7).text();
            if(market != null) feedback.market = market;
            feedbackList.add(feedback);
        }
        return feedbackList;
    }

    /**
     * 通过 Document 检查出是那一个市场
     *
     * @param doc
     * @return
     */
    public static M selectMarket(Document doc) {
        Element multiMarketSelect = doc.select("#sc-mkt-switcher-select").first();
        if(multiMarketSelect == null) return M.AMAZON_US;
        return M.val(multiMarketSelect.select("option[selected]").text());
    }
}
