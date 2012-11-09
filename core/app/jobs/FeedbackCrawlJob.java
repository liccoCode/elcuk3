package jobs;

import helper.*;
import models.Jobex;
import models.market.Account;
import models.market.Feedback;
import models.market.M;
import org.apache.commons.io.FileUtils;
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
import play.jobs.Every;
import play.jobs.Job;

import java.io.File;
import java.util.ArrayList;
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
 */
@Every("30mn")
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
        Currency.updateCRY();// 每一次的轮训, 都更新一次 CRY

        if(!Jobex.findByClassName(FeedbackCrawlJob.class.getName()).isExcute()) return;

        List<Account> accs = Account.openedSaleAcc();
        for(Account acc : accs) {
            switch(acc.type) {
                case AMAZON_DE:
                case AMAZON_US:
                case AMAZON_UK:
                    FeedbackCrawlJob.fetchAccountFeedback(acc, acc.type, 5);
                    break;
                //  现在 FR 是 UK 的账号, 不处理 FR 的.
                case AMAZON_FR:
                case AMAZON_ES:
                case AMAZON_IT:
                    Logger.warn("Not Support Right Now!");
                    break;
            }
        }

        // 最后检查一次创建了 Ticket 但是没有创建成功的(额外开一个 Job, 保证这个 Job 的数据进入 DB)
        new Job() {
            @Override
            public void doJob() {
                List<Feedback> feedbacks = Feedback.find("score<4 and (osTicketId is null OR osTicketId='') and createDate>=?",
                        DateTime.now().minusMonths(1).toDate()).fetch();
                for(Feedback f : feedbacks)
                    f.checkMailAndTicket();
            }
        }.now();
    }

    /**
     * 抓取某一个市场的 N(现在为 5) 页的 feedback 并且更新或者保存到数据库
     *
     * @param acc
     * @param market
     * @param pages
     */
    public static void fetchAccountFeedback(Account acc, M market, int pages) {
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
            Logger.warn(String.format("Account %s Market %s fetch feedback have some error![%s]", acc.username, market, e.getMessage()));
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
        List<Feedback> feedbacks = FeedbackCrawlJob.fetchFeedback(acc, i);
        if(feedbacks.size() == 0) {
            Logger.info(String.format("Fetch %s %s, page %s has no more feedbacks.", acc.username, market, i));
        } else {
            Logger.info(String.format("Fetch %s %s, page %s, total %s.", acc.username, market, i, feedbacks.size()));
        }

        for(Feedback f : feedbacks) {
            Feedback managed = Feedback.findById(f.orderId);
            if(managed == null) {
                try {
                    f.checkAndSave(acc);
                    f.checkMailAndTicket();
                    Logger.info("Save Feedback %s, score: %s", f.orderId, f.score);
                } catch(Exception e) {
                    Logger.warn(Webs.E(e) + "|" + J.json(Validation.errors()));
                }
            } else {
                managed.updateAttr(f);
                managed.checkMailAndTicket();
                Logger.info("Update Feedback %s, score: %s", f.orderId, f.score);
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
    public static List<Feedback> fetchFeedback(Account acc, int page) {
        switch(acc.type) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_US:
                try {
                    String body = HTTP.get(acc.cookieStore(), acc.type.feedbackPage(page));
                    if(Play.mode.isDev())
                        FileUtils.writeStringToFile(new File(Constant.HOME + "/elcuk2-logs/" + acc.type.name() + ".id_" + acc.id + "feedback_p" + page + ".html"), body);
                    return FeedbackCrawlJob.parseFeedBackFromHTML(body);
                } catch(Exception e) {
                    Logger.warn("[" + acc.type + "] Feedback page can not found Or the session is invalid!");
                }
                break;
            default:
                Logger.warn("Not support fetch [" + acc.type + "] Feedback.");
        }
        return new ArrayList<Feedback>();
    }

    /**
     * 从 Amazon 的网页上解析出需要的 Feedback 信息
     *
     * @param html
     * @return
     */
    public static List<Feedback> parseFeedBackFromHTML(String html) {
        Document doc = Jsoup.parse(html);

        Element marketEl = doc.select("#merchant-website").first();
        M market = null;
        if(marketEl == null) {
            Webs.systemMail("Feedback Market parse Error!", html);
            return new ArrayList<Feedback>();
        } else {
            // us/ de
            if(StringUtils.contains(marketEl.select(".merch-site-span").text(), "amazon.com"))
                market = M.AMAZON_US;
            else {
                market = M.val(doc.select("#marketplaceSelect option[selected]").first().text().trim());
            }
        }
        Elements feedbacks = doc.select("td[valign=center][align=middle] tr[valign=center]");
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        for(Element feb : feedbacks) {
            if("#ffffff".equals(feb.attr("bgcolor"))) continue;
            Feedback feedback = new Feedback();
            Elements tds = feb.select("td");
            //time
            if(market == M.AMAZON_US)
                feedback.createDate = DateTime.parse(tds.get(0).text(), DateTimeFormat.forPattern("MM/dd/yy")).toDate();
            else
                feedback.createDate = DateTime.parse(tds.get(0).text(), DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
            //score
            feedback.score = NumberUtils.toFloat(tds.get(1).text());
            //comments
            feedback.feedback = tds.get(2).childNode(0).toString();
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
}
