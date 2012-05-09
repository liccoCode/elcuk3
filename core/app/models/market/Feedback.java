package models.market;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import helper.GTs;
import helper.HTTP;
import helper.Webs;
import notifiers.Mails;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.data.validation.Email;
import play.db.jpa.GenericModel;
import play.utils.FastRuntimeException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 3/14/12
 * Time: 5:15 PM
 */
@Entity
public class Feedback extends GenericModel {

    public enum S {
        HANDLING,
        SLOVED,
        END,
        /**
         * 没有办法处理了, 只能留着
         */
        LEFT
    }

    @OneToOne
    public Orderr orderr;

    @OneToOne
    public Account account;

    /**
     * Feedback 每一个订单只有一个, 所以直接使用 OrderId 作为 feedback Id
     */
    @Id
    public String orderId;

    public Date createDate;

    public Float score;

    @Email
    public String email;

    @Lob
    public String comment;

    @Lob
    public String memo;

    @Enumerated(EnumType.STRING)
    public Account.M market;

    /**
     * 发送邮件警告的次数, 最多 3 次;
     */
    public Integer mailedTimes = 0;

    /**
     * 关联的 OsTicket 系统中的 Id, 如果没有则需要向 OsTicket 系统指定的 URL 创建 Ticket.
     */
    public String osTicketId;

    /**
     * 是否解决了,等等状态
     */
    public S state;

    public Feedback() {
    }

    public Feedback(String orderId, Date createDate, Float score, String email, String comment) {
        this.orderId = orderId;
        this.createDate = createDate;
        this.score = score;
        this.email = email;
        this.comment = comment;
        this.state = S.END;
    }

    /**
     * 检查这个 Feedback, 如果 <= 3 则发送警告邮件, 并且没有创建 OsTicket 则去创建 OsTicket;
     *
     * @return true: 不需要进行邮件警告与 OsTicket 创建.  false: 需要进行邮件警告与 OsTicket 创建
     */
    public boolean checkMailAndTicket() {
        /**
         * 1. 判断是否需要发送警告邮件;
         * 2. 判断是否需要去 OsTicket 系统中创建 Ticket.
         */
        if(this.score > 3 || this.state == S.SLOVED || this.state == S.END || this.state == S.LEFT) return true;

        if(StringUtils.isBlank(this.osTicketId)) this.openOsTicket(null);

        if(this.mailedTimes == null || this.mailedTimes <= 3) Mails.feedbackWarnning(this);

        this.save();

        return false;
    }

    public Feedback updateAttr(Feedback newFeedback) {
        if(!this.orderId.equalsIgnoreCase(newFeedback.orderId))
            throw new FastRuntimeException("Feedback OrderId is not the same!");
        if(newFeedback.score != null && newFeedback.score >= 1) {
            if(!newFeedback.score.equals(this.score)) { // score 不一样了, 需要记录
                this.memo += String.format("\r\nScore from %s to %s on %s", this.score, newFeedback.score, DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
            } else {
                this.score = newFeedback.score;
            }
        }
        if(StringUtils.isNotBlank(newFeedback.comment)) this.comment = newFeedback.comment;
        if(newFeedback.state != null && this.state == S.HANDLING) this.state = newFeedback.state;
        if(StringUtils.isNotBlank(newFeedback.email)) this.email = newFeedback.email;

        return this.save();
    }

    public Feedback checkSaveOrUpdate() {
        Feedback manager = Feedback.findById(this.orderId);
        if(manager != null && manager.isPersistent()) { // 系统中存在过的
            return manager.updateAttr(this);
        } else {
            return this.save();
        }
    }

    /**
     * 向 OsTicket 系统开启一个新的 Ticket
     *
     * @param title 可以调整的在 OsTicket 中创建的 Ticket 的 title, 回复给客户的邮件 Title 也是如此.
     */
    public void openOsTicket(String title) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String name = this.orderId;
        if(this.orderr != null)
            name = String.format("%s - %s", this.orderr.buyer, this.market.toString());

        String subject = title;
        if(StringUtils.isBlank(subject))
            subject = "You left a negative feedback, Please give us a chance to make up!"; //TODO  默认的 Title 很需要

        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("email", this.email));
        params.add(new BasicNameValuePair("phone", ""));
        params.add(new BasicNameValuePair("phone_ext", ""));
        params.add(new BasicNameValuePair("topicId", "1")); // 固定这个 TopicId 为 1; OsTicket 系统里面为 Support
        params.add(new BasicNameValuePair("submit_x", "Submit Ticket"));
        params.add(new BasicNameValuePair("subject", subject));
        params.add(new BasicNameValuePair("message", GTs.render("OsTicketWarn", GTs.newMap("f", this).build())));

        try {
            JsonElement jsonel = HTTP.postJson("http://t.easyacceu.com/open_api.php", params);
            JsonObject obj = jsonel.getAsJsonObject();
            if(obj == null) {
                Logger.error("Feedback.openOsTicket fetch content Error!");
                return;
            }
            if(obj.get("flag").getAsBoolean()) { // 成功创建
                this.osTicketId = obj.get("tid").getAsString();
            } else {
                Logger.warn("Order[%s] Feedback post to OsTicket failed because of [%s]",
                        this.orderId, obj.get("message").getAsString());
            }
        } catch(Exception e) {
            Logger.error("Feedback.openOsTicket fetch IO Error!");
        }
    }

    /**
     * 从 Amazon 的网页上解析出需要的 Feedback 信息
     *
     * @param html
     * @return
     */
    public static List<Feedback> parseFeedBackFromHTML(String html) {
        Document doc = Jsoup.parse(html);

        Element marketEl = doc.select("#marketplaceSelect option[selected]").first();
        Account.M market = null;
        if(marketEl == null) {
            Webs.systemMail("Feedback Market parse Error!", html);
        } else {
            market = Account.M.val(marketEl.text().trim());
        }
        Elements feedbacks = doc.select("td[valign=center][align=middle] tr[valign=center]");
        List<Feedback> feedbackList = new ArrayList<Feedback>();
        for(Element feb : feedbacks) {
            if("#ffffff".equals(feb.attr("bgcolor"))) continue;
            Feedback feedback = new Feedback();
            Elements tds = feb.select("td");
            //time
            feedback.createDate = DateTime.parse(tds.get(0).text(), DateTimeFormat.forPattern("dd/MM/yyyy")).toDate();
            //score
            feedback.score = NumberUtils.toFloat(tds.get(1).text());
            if(feedback.score <= 3) feedback.state = S.HANDLING;
            else feedback.state = S.END;
            //comments
            feedback.comment = tds.get(2).childNode(0).toString();
            Element b = tds.get(2).select("b").first();
            if(b != null) {
                feedback.memo = b.nextSibling().toString();
                feedback.state = S.SLOVED;
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
     * 查找 N 天以前到现在的某一个 Account 的得分小于指定值的 Feedback 的数量
     *
     * @param beforeDays 一般为附属, 传入 0, 那么则表示查找所有的
     * @param acc
     * @param score
     * @return
     */
    public static Long feedbackCount(int beforeDays, Account acc, Account.M market, float score, S state) {
        if(score <= 0) score = 1;
        if(beforeDays > 0) beforeDays = -beforeDays;
        if(beforeDays == 0) {
            if(state == null) return Feedback.count("account=? AND score<=? AND market=?", acc, score, market);
            else return Feedback.count("account=? AND score<=? AND state=? AND market=?", acc, score, state, market);
        } else {
            DateTime dt = DateTime.parse(DateTime.now().toString("yyyy-MM-dd"));
            Date bdt = dt.plusDays(beforeDays).toDate();
            if(state == null)
                return Feedback.count("createDate>=? AND createDate<=? AND account=? AND score<=? AND market=?", bdt, dt.toDate(), acc, score, market);
            else
                return Feedback.count("createDate>=? AND createDate<=? AND account=? AND score<=? AND state=? AND market=?", bdt, dt.toDate(), acc, score, state, market);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Feedback");
        sb.append("{orderId='").append(orderId).append('\'');
        sb.append(", createDate=").append(createDate);
        sb.append(", score=").append(score);
        sb.append(", email='").append(email).append('\'');
        sb.append(", comment='").append(comment).append('\'');
        sb.append(", memo='").append(memo).append('\'');
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        Feedback feedback = (Feedback) o;

        if(orderId != null ? !orderId.equals(feedback.orderId) : feedback.orderId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (orderId != null ? orderId.hashCode() : 0);
        return result;
    }
}
