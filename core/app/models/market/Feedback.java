package models.market;

import helper.Webs;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.data.validation.Email;
import play.db.jpa.GenericModel;

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

    public String memo;

    @Enumerated(EnumType.STRING)
    public Account.M market;

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
}
