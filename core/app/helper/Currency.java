package helper;

import models.market.M;
import org.apache.commons.lang.math.NumberUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * 不同货币单位的枚举类
 * User: Wyatt
 * Date: 12-1-6
 * Time: 上午11:58
 */
public enum Currency {
    /**
     * 英镑
     */
    GBP {
        /**
         * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
         */
        @Override
        public Float toGBP(Float value) {
            return value;
        }

        @Override
        public Float toEUR(Float value) {
            return value * GBP_EUR;
        }

        @Override
        public Float toHKD(Float value) {
            return value * GBP_HKD;
        }

        @Override
        public Float toUSD(Float value) {
            return value * GBP_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * GBP_CNY;
        }

        @Override
        public Float toJPY(Float value) {
            return value * GBP_JPY;
        }

        @Override
        public Float toCAD(Float value) {
            return value * GBP_CAD;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return GBP_CNY;
                case EUR:
                    return GBP_EUR;
                case USD:
                    return GBP_USD;
                case HKD:
                    return GBP_HKD;
                case JPY:
                    return GBP_JPY;
                case CAD:
                    return GBP_CAD;
                default:
                    return 1f;
            }
        }

        @Override
        public String symbol() {
            return "£";
        }

        @Override
        public String label() {
            return "英镑";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            // 619.71 -> 6.1971
            return NumberUtils.toFloat(doc.select("tr:contains(英镑) td:eq(1)").text()) / 100;
        }
    },
    /**
     * 欧元
     */
    EUR {
        /**
         * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
         */
        @Override
        public Float toGBP(Float value) {
            return value * EUR_GBP;
        }

        @Override
        public Float toEUR(Float value) {
            return value;
        }

        @Override
        public Float toHKD(Float value) {
            return value * EUR_HKD;
        }

        @Override
        public Float toUSD(Float value) {
            return value * EUR_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * EUR_CNY;
        }

        @Override
        public Float toJPY(Float value) {
            return value * EUR_JPY;
        }

        @Override
        public Float toCAD(Float value) {
            return value * EUR_CAD;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return EUR_CNY;
                case GBP:
                    return EUR_GBP;
                case HKD:
                    return EUR_HKD;
                case USD:
                    return EUR_USD;
                case JPY:
                    return EUR_JPY;
                case CAD:
                    return EUR_CAD;
                default:
                    return 1f;
            }
        }

        @Override
        public String symbol() {
            return "€";
        }

        @Override
        public String label() {
            return "欧元";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            // 619.71 -> 6.1971
            return NumberUtils.toFloat(doc.select("tr:contains(欧元) td:eq(1)").text()) / 100;
        }
    },
    /**
     * 人民币
     */
    CNY {
        @Override
        public Float toEUR(Float value) {
            return value * CNY_EUR;
        }

        @Override
        public Float toHKD(Float value) {
            return value * CNY_HKD;
        }

        @Override
        public Float toUSD(Float value) {
            return value * CNY_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value;
        }

        /**
         * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
         */
        @Override
        public Float toGBP(Float value) {
            return value * CNY_GBP; // 1 CNY = 0.1004 GBP
        }

        @Override
        public Float toJPY(Float value) {
            return value * CNY_JPY; // 1 CNY = 0.1004 GBP
        }

        @Override
        public Float toCAD(Float value) {
            return value * CNY_CAD; // 1 CNY = 0.1004 GBP
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case GBP:
                    return CNY_GBP;
                case EUR:
                    return CNY_EUR;
                case HKD:
                    return CNY_HKD;
                case USD:
                    return CNY_USD;
                case JPY:
                    return CNY_JPY;
                case CAD:
                    return CNY_CAD;
                default:
                    return 1f;
            }
        }

        @Override
        public String symbol() {
            return "¥";
        }

        @Override
        public String label() {
            return "人民币";
        }

        @Override
        public Float rate(String html) {
            return 1.0f;
        }
    },
    /**
     * 美元
     */
    USD {
        /**
         * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
         */
        @Override
        public Float toGBP(Float value) {
            return value * USD_GBP;
        }

        @Override
        public Float toEUR(Float value) {
            return value * USD_EUR;
        }

        @Override
        public Float toHKD(Float value) {
            return value * USD_HKD;
        }

        @Override
        public Float toUSD(Float value) {
            return value;
        }

        @Override
        public Float toCNY(Float value) {
            return value * USD_CNY;
        }

        @Override
        public Float toJPY(Float value) {
            return value * USD_JPY;
        }

        @Override
        public Float toCAD(Float value) {
            return value * USD_CAD;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return USD_CNY;
                case GBP:
                    return USD_GBP;
                case EUR:
                    return USD_EUR;
                case HKD:
                    return USD_HKD;
                case JPY:
                    return USD_JPY;
                case CAD:
                    return USD_CAD;
                case USD:
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "$";
        }

        @Override
        public String label() {
            return "美元";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            // 619.71 -> 6.1971
            return NumberUtils.toFloat(doc.select("tr:contains(美元) td:eq(1)").text()) / 100;
        }
    },
    HKD {
        @Override
        public Float toGBP(Float value) {
            return value * HKD_GBP;
        }

        @Override
        public Float toEUR(Float value) {
            return value * HKD_EUR;
        }

        @Override
        public Float toHKD(Float value) {
            return value;
        }

        @Override
        public Float toUSD(Float value) {
            return value * HKD_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * HKD_CNY;
        }

        @Override
        public Float toJPY(Float value) {
            return value * HKD_JPY;
        }

        @Override
        public Float toCAD(Float value) {
            return value * HKD_CAD;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return HKD_CNY;
                case GBP:
                    return HKD_GBP;
                case EUR:
                    return HKD_EUR;
                case USD:
                    return HKD_USD;
                case JPY:
                    return HKD_JPY;
                case CAD:
                    return HKD_CAD;
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "HK$";
        }

        @Override
        public String label() {
            return "港币";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            // 78.309 -> 0.78309
            return NumberUtils.toFloat(doc.select("tr:contains(港币) td:eq(1)").text()) / 100;
        }
    },

    /**
     * 加拿大
     */
    CAD {
        /**
         * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
         */
        @Override
        public Float toGBP(Float value) {
            return value * USD_GBP;
        }

        @Override
        public Float toEUR(Float value) {
            return value * USD_EUR;
        }

        @Override
        public Float toHKD(Float value) {
            return value * USD_HKD;
        }

        @Override
        public Float toUSD(Float value) {
            return value;
        }

        @Override
        public Float toCNY(Float value) {
            return value * USD_CNY;
        }

        @Override
        public Float toJPY(Float value) {
            return value * USD_JPY;
        }

        @Override
        public Float toCAD(Float value) {
            return value * USD_CAD;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return USD_CNY;
                case GBP:
                    return USD_GBP;
                case EUR:
                    return USD_EUR;
                case HKD:
                    return USD_HKD;
                case JPY:
                    return USD_JPY;
                case CAD:
                    return USD_CAD;
                case USD:
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "$";
        }

        @Override
        public String label() {
            return "美元";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            // 619.71 -> 6.1971
            return NumberUtils.toFloat(doc.select("tr:contains(美元) td:eq(1)").text()) / 100;
        }
    },

    JPY {
        @Override
        public Float toGBP(Float value) {
            return value * JPY_GBP;
        }

        @Override
        public Float toEUR(Float value) {
            return value * JPY_EUR;
        }

        @Override
        public Float toHKD(Float value) {
            return value * JPY_HKD;
        }

        @Override
        public Float toUSD(Float value) {
            return value * JPY_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * JPY_CNY;
        }

        @Override
        public Float toJPY(Float value) {
            return value;
        }

        @Override
        public Float toCAD(Float value) {
            return value;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return JPY_CNY;
                case GBP:
                    return JPY_GBP;
                case EUR:
                    return JPY_EUR;
                case USD:
                    return JPY_USD;
                case HKD:
                    return JPY_HKD;
                case CAD:
                    return JPY_CAD;
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "JPY￥";
        }

        @Override
        public String label() {
            return "日元";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            // 78.309 -> 0.78309
            return NumberUtils.toFloat(doc.select("tr:contains(港币) td:eq(1)").text()) / 100;
        }
    };

    /**
     * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
     *
     * @return
     */
    public abstract Float toGBP(Float value);

    public abstract Float toEUR(Float value);

    public abstract Float toHKD(Float value);

    public abstract Float toUSD(Float value);

    public abstract Float toCNY(Float value);

    public abstract Float toJPY(Float value);

    public abstract Float toCAD(Float value);

    public abstract float ratio(Currency currency);

    public abstract String symbol();

    public abstract String label();

    //CNY
    private static float CNY_EUR = 0.124664933f;
    private static float CNY_GBP = 0.107829021f;
    private static float CNY_HKD = 1.26433964f;
    private static float CNY_USD = 0.162962f;
    private static float CNY_JPY = 16.2930f;
    private static float CNY_CAD = 0.1778f;

    //EUR
    private static float EUR_CNY = 7.99469721f;
    private static float EUR_GBP = 0.864950705f;
    private static float EUR_HKD = 10.1419028f;
    private static float EUR_USD = 1.3072f;
    private static float EUR_JPY = 141.5580f;
    private static float EUR_CAD = 1.4630f;

    //GBP
    private static float GBP_CNY = 9.27394116f;
    private static float GBP_EUR = 1.15613525f;
    private static float GBP_HKD = 11.7254114f;
    private static float GBP_USD = 1.5113f;
    private static float GBP_JPY = 172.0010f;
    private static float GBP_CAD = 1.8347f;

    //HKD
    private static float HKD_CNY = 0.790926719f;
    private static float HKD_EUR = 0.0986008262f;
    private static float HKD_GBP = 0.0852848541f;
    private static float HKD_USD = 0.128891f;
    private static float HKD_JPY = 13.1423f;
    private static float HKD_CAD = 0.1412f;

    //USD
    private static float USD_CNY = 6.1363999f;
    private static float USD_EUR = 0.76499388f;
    private static float USD_GBP = 0.661681996f;
    private static float USD_HKD = 7.75849361f;
    private static float USD_JPY = 102.0900f;
    private static float USD_CAD = 1.0945f;

    //JPY
    private static float JPY_CNY = 0.0613f;
    private static float JPY_EUR = 0.0071f;
    private static float JPY_GBP = 0.0058f;
    private static float JPY_HKD = 0.0759f;
    private static float JPY_USD = 0.0098f;
    private static float JPY_CAD = 0.0107f;

    //CAD
    private static float CAD_CNY = 5.6276f;
    private static float CAD_EUR = 0.6835f;
    private static float CAD_GBP = 0.5450f;
    private static float CAD_HKD = 7.0816f;
    private static float CAD_JPY = 93.4558f;
    private static float CAD_USD = 0.9137f;


    public static void updateCRY() {
        synchronized(Currency.class) { // 与 Google 同步汇率的时候阻塞所有与 Currency 有关的操作.
            //http://www.google.com/ig/calculator?hl=en&q=1USD=?EUR

            // GBP
            float tmp = ratio("GBP", "EUR");
            GBP_EUR = tmp > 0 ? tmp : GBP_EUR;
            tmp = ratio("GBP", "CNY");
            GBP_CNY = tmp > 0 ? tmp : GBP_CNY;
            tmp = ratio("GBP", "USD");
            GBP_USD = tmp > 0 ? tmp : GBP_USD;
            tmp = ratio("GBP", "HKD");
            GBP_HKD = tmp > 0 ? tmp : GBP_HKD;
            tmp = ratio("GBP", "JPY");
            GBP_JPY = tmp > 0 ? tmp : GBP_JPY;
            tmp = ratio("GBP", "CAD");
            GBP_CAD = tmp > 0 ? tmp : GBP_CAD;

            // EUR
            tmp = ratio("EUR", "GBP");
            EUR_GBP = tmp > 0 ? tmp : EUR_GBP;
            tmp = ratio("EUR", "CNY");
            EUR_CNY = tmp > 0 ? tmp : EUR_CNY;
            tmp = ratio("EUR", "USD");
            EUR_USD = tmp > 0 ? tmp : EUR_USD;
            tmp = ratio("EUR", "HKD");
            EUR_HKD = tmp > 0 ? tmp : EUR_HKD;
            tmp = ratio("EUR", "JPY");
            EUR_JPY = tmp > 0 ? tmp : EUR_JPY;
            tmp = ratio("EUR", "CAD");
            EUR_CAD = tmp > 0 ? tmp : EUR_CAD;

            // CNY
            tmp = ratio("CNY", "GBP");
            CNY_GBP = tmp > 0 ? tmp : CNY_GBP;
            tmp = ratio("CNY", "EUR");
            CNY_EUR = tmp > 0 ? tmp : CNY_EUR;
            tmp = ratio("CNY", "USD");
            CNY_USD = tmp > 0 ? tmp : CNY_USD;
            tmp = ratio("CNY", "HKD");
            CNY_HKD = tmp > 0 ? tmp : CNY_HKD;
            tmp = ratio("CNY", "JPY");
            CNY_JPY = tmp > 0 ? tmp : CNY_JPY;
            tmp = ratio("CNY", "CAD");
            CNY_CAD = tmp > 0 ? tmp : CNY_CAD;

            // USD
            tmp = ratio("USD", "GBP");
            USD_GBP = tmp > 0 ? tmp : USD_GBP;
            tmp = ratio("USD", "CNY");
            USD_CNY = tmp > 0 ? tmp : USD_CNY;
            tmp = ratio("USD", "EUR");
            USD_EUR = tmp > 0 ? tmp : USD_EUR;
            tmp = ratio("USD", "HKD");
            USD_HKD = tmp > 0 ? tmp : USD_HKD;
            tmp = ratio("USD", "JPY");
            USD_JPY = tmp > 0 ? tmp : USD_JPY;
            tmp = ratio("USD", "CAD");
            USD_CAD = tmp > 0 ? tmp : USD_CAD;


            // HKD
            tmp = ratio("HKD", "CNY");
            HKD_CNY = tmp > 0 ? tmp : HKD_CNY;
            tmp = ratio("HKD", "EUR");
            HKD_EUR = tmp > 0 ? tmp : HKD_EUR;
            tmp = ratio("HKD", "GBP");
            HKD_GBP = tmp > 0 ? tmp : HKD_GBP;
            tmp = ratio("HKD", "USD");
            HKD_USD = tmp > 0 ? tmp : HKD_USD;
            tmp = ratio("HKD", "JPY");
            HKD_JPY = tmp > 0 ? tmp : HKD_JPY;
            tmp = ratio("HKD", "CAD");
            HKD_CAD = tmp > 0 ? tmp : HKD_CAD;

            // JPY
            tmp = ratio("JPY", "CNY");
            JPY_CNY = tmp > 0 ? tmp : JPY_CNY;
            tmp = ratio("JPY", "EUR");
            JPY_EUR = tmp > 0 ? tmp : JPY_EUR;
            tmp = ratio("JPY", "GBP");
            JPY_GBP = tmp > 0 ? tmp : JPY_GBP;
            tmp = ratio("JPY", "USD");
            JPY_USD = tmp > 0 ? tmp : JPY_USD;
            tmp = ratio("JPY", "HKD");
            JPY_HKD = tmp > 0 ? tmp : JPY_HKD;
            tmp = ratio("JPY", "CAD");
            JPY_CAD = tmp > 0 ? tmp : JPY_CAD;


            // CAD
            tmp = ratio("CAD", "CNY");
            CAD_CNY = tmp > 0 ? tmp : CAD_CNY;
            tmp = ratio("CAD", "EUR");
            CAD_EUR = tmp > 0 ? tmp : CAD_EUR;
            tmp = ratio("CAD", "GBP");
            CAD_GBP = tmp > 0 ? tmp : CAD_GBP;
            tmp = ratio("CAD", "USD");
            CAD_USD = tmp > 0 ? tmp : CAD_USD;
            tmp = ratio("CAD", "HKD");
            CAD_HKD = tmp > 0 ? tmp : CAD_HKD;
            tmp = ratio("CAD", "JPY");
            CAD_JPY = tmp > 0 ? tmp : CAD_JPY;
        }
    }

    @SuppressWarnings("unchecked")
    private static Float ratio(String from, String to) {
        if(from.equalsIgnoreCase(to)) return 1f;
        try {

            String html = "";
            String proxyhost = models.OperatorConfig.getVal("proxyhost");
            if(!proxyhost.equals("")) {
                html = HTTP.getProxy(proxyhost, "https://www.google.com/finance/converter?a=1&from=" + from + "&to=" +
                        to);
            } else {
                html = HTTP.get("https://www.google.com/finance/converter?a=1&from=" + from + "&to=" + to);
            }
            Document doc = Jsoup.parse(html);
            String toStr = doc.select("#currency_converter_result .bld").text();
            Logger.info("[1 %s TO %s]", from, toStr);
            return NumberUtils.toFloat(toStr.split(" ")[0].trim());
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
        }
        return -1f;
    }

    /**
     * 将数字的小数点上下调整到 0.49 与 0.99, 以 0.5 为界限
     *
     * @param price
     * @return
     */
    public static Float upDown(Float price) {
        int intPart = new BigDecimal(price).setScale(2, RoundingMode.DOWN).intValue();
        float floatPart = price - intPart;
        if(floatPart >= 0.5) floatPart = 0.99f;
        else floatPart = 0.49f;
        return intPart + floatPart;
    }

    /**
     * 返回 Market 对应的 Currency
     *
     * @param market
     * @return
     */
    public static Currency M(M market) {
        switch(market) {
            case AMAZON_CA:
                return CAD;
            case AMAZON_UK:
                return GBP;
            case AMAZON_DE:
                return EUR;
            case AMAZON_ES:
                return EUR;
            case AMAZON_FR:
                return EUR;
            case AMAZON_JP:
                return JPY;
            case AMAZON_IT:
                return EUR;
            case AMAZON_US:
                return USD;
            default:
                return USD;
        }
    }

    /**
     * 返回中国银行的最新汇率表
     *
     * @return
     */
    public static String bocRatesHtml() {
        Document doc = Jsoup.parse(HTTP.get("http://www.boc.cn/sourcedb/whpj/"));
        Element table = doc.select(".BOC_main .publish table").last();
        String[] currencies = new String[]{"英镑", "港币", "美元", "欧元", "日元"};
        for(Element tr : table.select("tr")) {
            boolean find = false;
            for(String c : currencies) {
                if(tr.text().contains(c)) {
                    find = true;
                    break;
                }
            }
            if(!find) tr.remove();
        }
        return table.outerHtml();
    }

    /**
     * 从 Boc 的 汇率 HTML 中解析 Rate
     *
     * @param html
     * @return
     */
    public abstract Float rate(String html);

    /**
     * 解析出当前汇率的时间是什么
     *
     * @param html
     * @return
     */
    public static Date rateDateTime(String html) {
        Document doc = Jsoup.parse(html);
        return Dates.cn(String.format("%s %s",
                doc.select("tr:eq(3) td:eq(6)").text(),
                doc.select("tr:eq(3) td:eq(7)").text()
        )).toDate();
    }

    /**
     * 返回 Xe.com 市场的汇率 Table
     *
     * @return
     */
    public static String xeRatesHtml(Currency from) {
        String html = HTTP.get("http://www.xe.com/zh-CN/currencytables/?from=" + from.name());
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("#historicalRateTbl tr");
        for(Element tr : trs.subList(1, trs.size() - 1)) {
            boolean find = false;
            for(Currency c : Currency.values()) {
                if(!tr.select("td:eq(0):contains(" + c.name() + ")").isEmpty())
                    find = true;
            }
            if(!find) tr.remove();
        }
        return doc.select("#historicalRateTbl").outerHtml();
    }
}

//http://www1.jctrans.com/tool/sjhb.htm

