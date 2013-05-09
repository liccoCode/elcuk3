package models.procure;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import helper.HTTP;
import helper.J;
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

import java.util.*;

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
        public String trackUrl(String tracNo) {
            return String
                    .format("http://www.cn.dhl.com/content/cn/zh/express/tracking.shtml?brand=DHL&AWB=%s",
                            tracNo.trim());
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
        public boolean isClearance(String content) {
            // 如果都完成运输了, 那清关也一定完成了.
            return this.deliverText(content) || StringUtils.contains(content, "已完成清关");
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            if(StringUtils.isBlank(iExpressHTML))
                return new F.T2<Boolean, DateTime>(false, DateTime.now());
            /**
             * 只会检查最新的信息, 最上面的记录, 根据最新的信息判断是否已经发货
             */
            Document doc = Jsoup.parse(iExpressHTML);
            Element thead = doc.select("thead:eq(1)").first();
            Element tbody = doc.select("tbody").first();

            // parse date
            DateTime dt = DateTime.parse(String.format("%s %s", thead.select("th:eq(0)").text(),
                    tbody.select("td:eq(3)").text()),
                    DateTimeFormat.forPattern("E, MMM dd, yyyy HH:mm").withLocale(Locale.CHINESE));

            // is delivered
            String text = tbody.select("td:eq(1)").text();
            boolean isDelivered = this.deliverText(text);

            return new F.T2<Boolean, DateTime>(isDelivered, dt);
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return null;
        }

        private boolean deliverText(String text) {
            return StringUtils.contains(text, "已派送并签收") ||
                    (StringUtils.contains(text, "已经签收") && !StringUtils.contains(text, "部分快件已经签收"));

        }
    },


    FEDEX {
        @Override
        public String trackUrl(String tracNo) {
            return String.format("https://www.fedex.com/trackingCal/track");
        }

        @Override
        public String fetchStateHTML(String tracNo) {
            Map<String, Map> data = new HashMap<String, Map>();
            Map<String, Object> trackpackgeRequest = new HashMap<String, Object>();
            List<Map<String, Object>> trackingInfoList = new ArrayList<Map<String, Object>>();
            Map<String, Object> trackNumberInfo = new HashMap<String, Object>();
            Map<String, String> trackNumber = new HashMap<String, String>();
            Map<String, String> processingParameters = new HashMap<String, String>();

            data.put("TrackPackagesRequest", trackpackgeRequest);
            trackpackgeRequest.put("processingParameters", processingParameters);
            trackpackgeRequest.put("trackingInfoList", trackingInfoList);
            trackpackgeRequest.put("appType", "wtrk");

            processingParameters.put("anonymousTransaction", "true");
            processingParameters.put("clientId", "WTRK");
            processingParameters.put("returnDetailedErrors", "true");
            processingParameters.put("returnLocalizedDateTime", "false");

            trackingInfoList.add(trackNumberInfo);
            trackNumberInfo.put("trackNumberInfo", trackNumber);
            trackNumber.put("trackingNumber", tracNo);

            return HTTP.post(this.trackUrl(tracNo), Arrays.asList(
                    new BasicNameValuePair("data", J.json(data)),
                    new BasicNameValuePair("action", "trackpackages"),
                    new BasicNameValuePair("locale", "zh_CN"),
                    new BasicNameValuePair("format", "json"),
                    new BasicNameValuePair("version", "99")
            ));
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            html = html.replaceAll("x2d", "-").replaceAll("x3a", ":");
            JsonElement infos = new JsonParser().parse(html);
            JsonArray scanInfos = infos.getAsJsonObject().get("TrackPackagesResponse")
                    .getAsJsonObject().get("packageList")
                    .getAsJsonArray().get(0).getAsJsonObject().get("scanEventList")
                    .getAsJsonArray();

            StringBuilder sbd = new StringBuilder("<table><tr>");
            // header
            sbd.append("<td>").append("日期/时间").append("</td>");
            sbd.append("<td>").append("活动").append("</td>");
            sbd.append("<td>").append("地点").append("</td>");
            sbd.append("<td>").append("详细信息").append("</td>");
            sbd.append("</tr>");
            for(JsonElement je : scanInfos) {
                JsonObject info = je.getAsJsonObject();
                sbd.append("<tr>");
                sbd.append("<td>").append(getStr(info, "date")).append(" ")
                        .append(getStr(info, "time")).append("</td>")
                        .append("<td>").append(getStr(info, "status")).append("</td>")
                        .append("<td>").append(getStr(info, "scanLocation")).append("</td>")
                        .append("<td>").append(getStr(info, "scanDetails")).append("</td>")
                        .append("</tr>");
            }
            return sbd.append("</table>").toString();
        }

        @Override
        public boolean isClearance(String content) {
            return this.deliverText(content) || StringUtils.contains(content, "可以向有关国家机构申报本货件");
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            if(StringUtils.isBlank(iExpressHTML))
                return new F.T2<Boolean, DateTime>(false, DateTime.now());
            Document doc = Jsoup.parse(iExpressHTML);
            Element newestTr = doc.select("tr:eq(1)").first();

            DateTime dt = DateTime.parse(newestTr.select("td:eq(0)").text(),
                    DateTimeFormat.forPattern("MMM dd, yyyy hh:mm a"));
            String text = newestTr.select("td:eq(1)").text();
            boolean isDelivered = this.deliverText(text);

            return new F.T2<Boolean, DateTime>(isDelivered, dt);
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return null;
        }

        private String getStr(JsonObject json, String key) {
            String val = json.get(key).getAsString();
            if(val == null) return "";
            else return val;
        }

        private boolean deliverText(String text) {
            return StringUtils.contains(text, "已送达");
        }

    },


    UPS {
        @Override
        public String trackUrl(String tracNo) {
            return String.format(
                    "http://wwwapps.ups.com/WebTracking/processInputRequest?AgreeToTermsAndConditions=yes&tracknum=%s&HTMLVersion=5.0&loc=zh_CN&Requester=UPSHome",
                    tracNo.trim());
        }

        @Override
        public String fetchStateHTML(String tracNo) {
            String html = HTTP.get(this.trackUrl(tracNo));
            Document doc = Jsoup.parse(html);
            Element form = doc.select("#detailFormid").first();
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            for(Element input : form.select("input")) {
                params.add(new BasicNameValuePair(input.attr("name"), input.val()));
            }
            return HTTP.post(form.attr("action"), params);
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            Document doc = Jsoup.parse(html);
            return doc.select(".secBody > .dataTable").outerHtml();
        }

        @Override
        public boolean isClearance(String content) {
            return StringUtils.contains(content, "清关机构");
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            Document doc = Jsoup.parse(iExpressHTML);
            for(Element tr : doc.select("tr")) {
                if(!StringUtils.contains(tr.outerHtml(), "已递送")) continue;
                String dateStr = String.format("%s %s", tr.select("td:eq(1)").text(),
                        tr.select("td:eq(2)").text());
                return new F.T2<Boolean, DateTime>(true,
                        DateTime.parse(dateStr, DateTimeFormat.forPattern("yyyy/MM/dd HH:mm")));
            }
            return new F.T2<Boolean, DateTime>(false, new DateTime());
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return null;
        }
    };

    /**
     * 0. 抓取快递单的地址
     *
     * @param tracNo
     * @return
     */
    public abstract String trackUrl(String tracNo);

    /**
     * 1. 直接返回抓取的 HTML 代码
     *
     * @param tracNo
     * @return
     */
    public String fetchStateHTML(String tracNo) {
        return HTTP.get(this.trackUrl(tracNo));
    }

    /**
     * 2. 解析出需要的部分 HTML
     *
     * @param html
     * @return
     */
    public abstract String parseExpress(String html, String trackNo);

    /**
     * 检查快递单是否到满足 清关中 状态
     *
     * @param content
     * @return
     */
    public abstract boolean isClearance(String content);

    /**
     * 检查运输单是否 派送中 状态
     *
     * @return
     */
    public abstract F.T2<Boolean, DateTime> isDelivered(String iExpressHTML);

    /**
     * 检查运输单是否 已经签收 状态
     *
     * @param iExpressHTML
     * @return
     */
    public abstract F.T2<Boolean, DateTime> isReceipt(String iExpressHTML);
}
