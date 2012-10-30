package models.procure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.HTTP;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 国际快递商
 * TODO 在 Model 中的 enum 没有办法在 VIEW 中使用?
 * User: wyattpan
 * Date: 6/26/12
 * Time: 4:31 PM
 */
public enum iExpress {
    DHL {
        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            if(StringUtils.isBlank(iExpressHTML)) return new F.T2<Boolean, DateTime>(false, DateTime.now());
            Document doc = Jsoup.parse(iExpressHTML);
            Element thead = doc.select("thead:eq(1)").first();
            Element tbody = doc.select("tbody").first();

            // parse date
            DateTime dt = DateTime.parse(thead.select("th:eq(0)").text(), DateTimeFormat.forPattern("E, MMM dd, yyyy").withLocale(Locale.CHINESE));

            // is delivered
            String text = tbody.select("td:eq(1)").text();
            boolean isDelivered = StringUtils.contains(text, "已派送并签收") || StringUtils.contains(text, "已经签收");

            return new F.T2<Boolean, DateTime>(isDelivered, dt);
        }

        @Override
        public boolean isContainsClearance(String content) {
            return StringUtils.contains(content, "已完成清关");
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            Document doc = Jsoup.parse(html);
            Element table = doc.select(String.format("#table%s", trackNo)).first();
            Elements articles = table.select(".article_list");
            for(Element article : articles) {
                int boxSize = article.select(".ArticleTitleContent > div").size();
                article.html(boxSize + " 箱");
            }
            return table.html();
        }

        @Override
        public String trackUrl(String tracNo) {
            return String.format("http://www.cn.dhl.com/content/cn/zh/express/tracking.shtml?brand=DHL&AWB=%s", tracNo.trim());
        }
    },



    FEDEX {
        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            if(StringUtils.isBlank(iExpressHTML)) return new F.T2<Boolean, DateTime>(false, DateTime.now());
            Document doc = Jsoup.parse(iExpressHTML);
            Element newestTr = doc.select("tr:eq(1)").first();

            DateTime dt = DateTime.parse(newestTr.select("td:eq(0)").text(), DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a"));
            String text = newestTr.select("td:eq(1)").text();
            boolean isDelivered = StringUtils.contains(text, "已送达");

            return new F.T2<Boolean, DateTime>(isDelivered, dt);
        }

        @Override
        public boolean isContainsClearance(String content) {
            return StringUtils.contains(content, "可以向有关国家机构申报本货件");
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            String jsonObj = StringUtils.substringBetween(html, "detailInfoObject =", "var associatedShipmentsTab =").trim();
            JSONObject infos = JSON.parseObject(jsonObj.substring(0, jsonObj.length() - 1));
            JSONArray scans = infos.getJSONArray("scans");
            /**
             *{"scanStatus":"Delivered",
             * "scanLocation":"PETERBOROUGH GB",
             * "scanTime":"8:11 AM",
             * "GMTOffset":"+01:00",
             * "showReturnToShipper":false,
             * "scanDate":"Jun 25, 2012"}
             */
            StringBuilder sbd = new StringBuilder("<table><tr>");
            // header
            sbd.append("<td>").append("日期/时间").append("</td>");
            sbd.append("<td>").append("活动").append("</td>");
            sbd.append("<td>").append("地点").append("</td>");
            sbd.append("<td>").append("详细信息").append("</td>");
            sbd.append("</tr>");
            for(JSONObject info : scans.toArray(new JSONObject[scans.size()])) {
                sbd.append("<tr>");
                sbd.append("<td>").append(getStr(info, "scanDate")).append(" ").append(getStr(info, "scanTime")).append("</td>");
                sbd.append("<td>").append(getStr(info, "scanStatus")).append("</td>");
                sbd.append("<td>").append(getStr(info, "scanLocation")).append("</td>");
                sbd.append("<td>").append(getStr(info, "scanComments")).append("</td>");
                sbd.append("</tr>");
            }
            return sbd.append("</table>").toString();
        }

        private String getStr(JSONObject json, String key) {
            String val = json.getString(key);
            if(val == null) return "";
            else return val;
        }

        @Override
        public String trackUrl(String tracNo) {
            return String.format("http://www.fedex.com/Tracking?tracknumbers=%s&cntry_code=cn", tracNo.trim());
        }
    },



    UPS {
        @Override
        public String trackUrl(String tracNo) {
            return String.format("http://wwwapps.ups.com/WebTracking/processInputRequest?AgreeToTermsAndConditions=yes&tracknum=%s&HTMLVersion=5.0&loc=zh_CN&Requester=UPSHome", tracNo.trim());
        }

        @Override
        public String fetchStateHTML(String tracNo) {
            String html = HTTP.get(this.trackUrl(tracNo));
            Document doc = Jsoup.parse(html);
            Element form = doc.select("#detailFormid").first();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for(Element input : form.select("input"))
                params.add(new BasicNameValuePair(input.attr("name"), input.val()));
            return HTTP.post(form.attr("action"), params);
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            Document doc = Jsoup.parse(html);
            return doc.select(".secBody > .dataTable").outerHtml();
        }

        @Override
        public boolean isContainsClearance(String content) {
            return StringUtils.contains(content, "清关机构");
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            Document doc = Jsoup.parse(iExpressHTML);
            for(Element tr : doc.select("tr")) {
                if(!StringUtils.contains(tr.outerHtml(), "已递送")) continue;
                String dateStr = String.format("%s %s", tr.select("td:eq(1)").text(), tr.select("td:eq(2)").text());
                return new F.T2<Boolean, DateTime>(true, DateTime.parse(dateStr, DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")));
            }
            return new F.T2<Boolean, DateTime>(false, new DateTime());
        }
    };

    /**
     * 抓取快递单的地址
     *
     * @param tracNo
     * @return
     */
    public abstract String trackUrl(String tracNo);

    /**
     * 解析出需要的部分 HTML
     *
     * @param html
     * @return
     */
    public abstract String parseExpress(String html, String trackNo);

    public abstract boolean isContainsClearance(String content);

    /**
     * 检查是否已经签收, 如果签收了,时间是什么时候
     * @return
     */
    public abstract F.T2<Boolean, DateTime> isDelivered(String iExpressHTML);


    /**
     * 直接返回抓取的 HTML 代码
     *
     * @param tracNo
     * @return
     */
    public String fetchStateHTML(String tracNo) {
        return HTTP.get(this.trackUrl(tracNo));
    }
}
