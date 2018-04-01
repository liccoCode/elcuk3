package models.procure;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import helper.Dates;
import helper.HTTP;
import helper.J;
import models.market.M;
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
//BEGIN GENERATED CODE
public enum iExpress {
    DHL {
        @Override
        public String trackUrl(String trackNo) {
            return String.format("http://www.cn.dhl.com/content/cn/zh/express/tracking.shtml?brand=DHL&AWB=%s",
                    trackNo.trim());
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
        public F.T2<Boolean, DateTime> isClearance(String iExpressHTML) {
            return isAnyState(iExpressHTML, "已完成清关");
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            return isAnyState(iExpressHTML, "正在派送");
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return isAnyState(iExpressHTML, "签收");
        }

        @Override
        public String carrierName(M m) {
            return Arrays.asList(M.AMAZON_US, M.AMAZON_CA).contains(m) ? "DHL_EXPRESS_USA_INC" : "DHL_UK";
        }

        @Override
        public String fcNum() {
            return "100001";
        }

        public F.T2<Boolean, DateTime> isAnyState(String iExpressHTML, String text) {
            if(StringUtils.isNotBlank(iExpressHTML)) {
                Document doc = Jsoup.parse(iExpressHTML);
                for(Element element : doc.select("tbody > tr")) {
                    if(element.text().contains(text)) {
                        Element thead = element.parent().previousElementSibling();
                        while(!thead.tagName().equals("thead")) {
                            thead = thead.previousElementSibling();
                        }
                        String date = thead.select("tr:eq(1) th:eq(0)").text() + " "
                                + element.select("td:eq(3)").text();
                        return new F.T2<>(true, dateStringParse(date));
                    }
                }
            }
            return new F.T2<>(false, DateTime.now());
        }

        private DateTime dateStringParse(String dateString) {
            return DateTime.parse(dateString,
                    DateTimeFormat.forPattern("E, MMM dd, yyyy HH:mm").withLocale(Locale.CHINESE));
        }
    },

    FEDEX {
        @Override
        public String trackUrl(String trackNo) {
            return String
                    .format("https://www.fedex.com/apps/fedextrack/index.html?tracknumbers=%s&locale=zh_CN&cntry_code=cn",
                            trackNo);
        }

        @Override
        public String fetchStateHTML(String trackNo) {
            Map<String, Map> data = new HashMap<>();
            Map<String, Object> trackpackgeRequest = new HashMap<>();
            List<Map<String, Object>> trackingInfoList = new ArrayList<>();
            Map<String, Object> trackNumberInfo = new HashMap<>();
            Map<String, String> trackNumber = new HashMap<>();
            Map<String, String> processingParameters = new HashMap<>();

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
            trackNumber.put("trackingNumber", trackNo);

            // Fedex 的 url 不一样
            return HTTP.post("https://www.fedex.com/trackingCal/track",
                    Arrays.asList(
                            new BasicNameValuePair("data", J.json(data)),
                            new BasicNameValuePair("action", "trackpackages"),
                            new BasicNameValuePair("locale", "zh_CN"),
                            new BasicNameValuePair("format", "json"),
                            new BasicNameValuePair("version", "99")
                    ));
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            JSONObject infos = JSON.parseObject(html);
            StringBuilder sbd = new StringBuilder("<table>")
                    // header
                    .append("<tr>")
                    .append("<td>").append("日期/时间").append("</td>")
                    .append("<td>").append("活动").append("</td>")
                    .append("<td>").append("地点").append("</td>")
                    .append("<td>").append("详细信息").append("</td>")
                    .append("</tr>");
            JSONArray events = infos.getJSONObject("TrackPackagesResponse")
                    .getJSONArray("packageList").getJSONObject(0).getJSONArray("scanEventList");
            for(Object obj : events) {
                JSONObject info = (JSONObject) obj;
                sbd.append("<tr>");
                sbd.append("<td>").append(info.getString("date")).append(" ")
                        .append(info.getString("time")).append("</td>")
                        .append("<td>").append(info.getString("status")).append("</td>")
                        .append("<td>").append(info.getString("scanLocation")).append("</td>")
                        .append("<td>").append(info.getString("scanDetails")).append("</td>")
                        .append("</tr>");
            }
            return sbd.append("</table>").toString();
        }

        @Override
        public F.T2<Boolean, DateTime> isClearance(String iExpressHTML) {
            F.T2<Boolean, DateTime> t2 = isAnyState(iExpressHTML, "可以向有关国家机构申报本货件");
            if(t2._1) return t2;
            return isAnyState(iExpressHTML, "进口");
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            F.T2<Boolean, DateTime> state = isAnyState(iExpressHTML, "派送途中");
            if(state._1) return state;
            return isAnyState(iExpressHTML, "递送");
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return isAnyState(iExpressHTML, "已送达");
        }

        @Override
        public String carrierName(M m) {
            return Arrays.asList(M.AMAZON_US, M.AMAZON_CA).contains(m) ? "FEDERAL_EXPRESS_CORP" : "OTHER";
        }

        @Override
        public String fcNum() {
            return "100003";
        }

        private F.T2<Boolean, DateTime> isAnyState(String iExpressHTML, String state) {
            if(StringUtils.isNotBlank(iExpressHTML)) {
                Document doc = Jsoup.parse(iExpressHTML);
                for(Element element : doc.select("tr")) {
                    if(element.text().contains(state)) {
                        String date = element.select("td:eq(0)").text();
                        return new F.T2<>(true, Dates.cn(date));
                    }
                }
            }
            return new F.T2<>(false, DateTime.now());
        }
    },

    UPS {
        @Override
        public String trackUrl(String trackNo) {
            return String.format("http://wwwapps.ups.com/WebTracking/processInputRequest?AgreeToTermsAndConditions=yes"
                    + "&tracknum=%s&HTMLVersion=5.0&loc=zh_CN&Requester=UPSHome", trackNo.trim());
        }

        @Override
        public String fetchStateHTML(String tracNo) {
            String html = HTTP.get(this.trackUrl(tracNo));
            Document doc = Jsoup.parse(html);
            Element form = doc.select("#detailFormid").first();
            List<NameValuePair> params = new ArrayList<>();
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
        public F.T2<Boolean, DateTime> isClearance(String iExpressHTML) {
            Document doc = Jsoup.parse(iExpressHTML);
            Elements elements = doc.select("tr:contains(清关机构)");
            if(elements.size() > 0) {
                // 由于 UPS 的重复信息太多了, 所以只能按照 "清关机构" 这四个字进行处理
                return new F.T2<>(true, trToDate(elements.last()));
            }
            return new F.T2<>(false, DateTime.now());
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            // UPS 先检查是否有外出递送, 没有则检查已递送
            Document doc = Jsoup.parse(iExpressHTML);
            Elements elements = doc.select("tr:contains(外出递送)");
            if(elements.size() > 0) {
                return new F.T2<>(true, trToDate(elements.last()));
            }
            return isReceipt(iExpressHTML);
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            Document doc = Jsoup.parse(iExpressHTML);
            Elements elements = doc.select("tr:contains(已递送)");
            if(elements.size() > 0) {
                return new F.T2<>(true, trToDate(elements.last()));
            }
            return new F.T2<>(false, DateTime.now());
        }

        @Override
        public String carrierName(M m) {
            return "UNITED_PARCEL_SERVICE_INC";
        }

        @Override
        public String fcNum() {
            return "100002";
        }

        private DateTime trToDate(Element trElement) {
            return DateTime.parse(String.format("%s %s", trElement.select("td:eq(1)").text(),
                    trElement.select("td:eq(2)").text()), DateTimeFormat.forPattern("yyyy/MM/dd HH:mm"));
        }
    },
    DPD {
        @Override
        public String trackUrl(String trackNo) {
            return null;
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            return null;
        }

        @Override
        public F.T2<Boolean, DateTime> isClearance(String content) {
            return null;
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            return null;
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return null;
        }

        @Override
        public String carrierName(M m) {
            return m == M.AMAZON_UK ? "DPD" : "OTHER";
        }

        @Override
        public String fcNum() {
            return "100010";
        }
    },
    TNT {
        @Override
        public String trackUrl(String trackNo) {
            return null;
        }

        @Override
        public String parseExpress(String html, String trackNo) {
            return null;
        }

        @Override
        public F.T2<Boolean, DateTime> isClearance(String content) {
            return null;
        }

        @Override
        public F.T2<Boolean, DateTime> isDelivered(String iExpressHTML) {
            return null;
        }

        @Override
        public F.T2<Boolean, DateTime> isReceipt(String iExpressHTML) {
            return null;
        }

        @Override
        public String carrierName(M m) {
            return m == M.AMAZON_UK ? "TNT" : "OTHER";
        }

        @Override
        public String fcNum() {
            return "100004";
        }
    };

    /**
     * 0. 抓取快递单的地址
     *
     * @param trackNo
     * @return
     */
    public abstract String trackUrl(String trackNo);

    /**
     * 1. 直接返回抓取的 HTML 代码
     *
     * @param trackNo
     * @return
     */
    public String fetchStateHTML(String trackNo) {
        return HTTP.get(this.trackUrl(trackNo));
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
    public abstract F.T2<Boolean, DateTime> isClearance(String content);

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

    public abstract String carrierName(M m);

    public abstract String fcNum();

    public String oneSevenTrackUrl(String trackNo) {
        return String.format("https://t.17track.net/zh-cn#nums=%s&fc=%s", trackNo.trim(), this.fcNum());
    }
}
