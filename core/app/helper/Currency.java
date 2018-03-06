package helper;

import models.market.M;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Logger;
import play.jobs.Job;

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
        @Override
        public Float toUSD(Float value) {
            return value * GBP_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * GBP_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return GBP_CNY;
                case USD:
                    return GBP_USD;
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
        @Override
        public Float toUSD(Float value) {
            return value * EUR_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * EUR_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return EUR_CNY;
                case USD:
                    return EUR_USD;
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
        public Float toUSD(Float value) {
            return value * CNY_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value;
        }


        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case USD:
                    return CNY_USD;
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
        @Override
        public Float toUSD(Float value) {
            return value;
        }

        @Override
        public Float toCNY(Float value) {
            return value * USD_CNY;
        }


        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return USD_CNY;
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
        public Float toUSD(Float value) {
            return value * HKD_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * HKD_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return HKD_CNY;
                case USD:
                    return HKD_USD;
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
        public Float toUSD(Float value) {
            return value;
        }

        @Override
        public Float toCNY(Float value) {
            return value * CAD_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return CAD_CNY;
                case USD:
                    return CAD_USD;
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
        public Float toUSD(Float value) {
            return value * JPY_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * JPY_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return JPY_CNY;
                case USD:
                    return JPY_USD;
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

    },

    MXN {
        @Override
        public Float toUSD(Float value) {
            return value * MXN_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * MXN_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return MXN_CNY;
                case USD:
                    return MXN_USD;
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "Mex$";
        }

        @Override
        public String label() {
            return "墨西哥元";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            return NumberUtils.toFloat(doc.select("tr:contains(墨西哥元) td:eq(1)").text()) / 100;
        }
    },

    AUD {
        @Override
        public Float toUSD(Float value) {
            return value * AUD_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * AUD_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return AUD_CNY;
                case USD:
                    return AUD_USD;
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "A$";
        }

        @Override
        public String label() {
            return "澳大利亚元";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            return NumberUtils.toFloat(doc.select("tr:contains(澳大利亚元) td:eq(1)").text()) / 100;
        }
    },

    INR {
        @Override
        public Float toUSD(Float value) {
            return value * INR_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * INR_CNY;
        }

        @Override
        public float ratio(Currency currency) {
            switch(currency) {
                case CNY:
                    return INR_CNY;
                case USD:
                    return INR_USD;
                default:
                    return 1;
            }
        }

        @Override
        public String symbol() {
            return "₹";
        }

        @Override
        public String label() {
            return "印度卢比";
        }

        @Override
        public Float rate(String html) {
            Document doc = Jsoup.parse(html);
            return NumberUtils.toFloat(doc.select("tr:contains(印度卢比) td:eq(1)").text()) / 100;
        }
    };

    // ------------------------------------------------------- Currency 所有枚举的通用方法 ----------------------------------------

    /**
     * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
     *
     * @return
     */
    public abstract Float toUSD(Float value);

    public abstract Float toCNY(Float value);

    public abstract float ratio(Currency currency);

    public abstract String symbol();

    public abstract String label();

    //CNY
    private static float CNY_USD = 0.15f;

    //EUR
    private static float EUR_CNY = 7.37f;
    private static float EUR_USD = 1.07f;

    //GBP
    private static float GBP_CNY = 8.59f;
    private static float GBP_USD = 1.25f;

    //HKD
    private static float HKD_CNY = 0.89f;
    private static float HKD_USD = 0.13f;

    //USD
    private static float USD_CNY = 6.89f;

    //JPY
    private static float JPY_CNY = 0.062f;
    private static float JPY_USD = 0.0089f;

    //CAD
    private static float CAD_CNY = 5.17f;
    private static float CAD_USD = 0.75f;

    private static float MXN_CNY = 0.3481f;
    private static float MXN_USD = 0.0524f;

    private static float AUD_CNY = 4.9340f;
    private static float AUD_USD = 0.7781f;

    private static float INR_CNY = 0.0976f;
    private static float INR_USD = 0.0154f;


    public static void updateCRY() {
        synchronized(Currency.class) { // 与 Google 同步汇率的时候阻塞所有与 Currency 有关的操作.
            // 1. https://openexchangerates.org/account (6*7=42 次一组, 1000 / 42 = 23.8 组)
            CNY_USD = ratio("CNY", "USD");
            EUR_USD = ratio("EUR", "USD");
            GBP_USD = ratio("GBP", "USD");
            HKD_USD = ratio("HKD", "USD");
            JPY_USD = ratio("JPY", "USD");
            CAD_USD = ratio("CAD", "USD");

            EUR_CNY = ratio("EUR", "CNY");
            GBP_CNY = ratio("GBP", "CNY");
            HKD_CNY = ratio("HKD", "CNY");
            USD_CNY = ratio("USD", "CNY");
            JPY_CNY = ratio("JPY", "CNY");
            CAD_CNY = ratio("CAD", "CNY");
        }
    }

    @SuppressWarnings("unchecked")
    private static Float ratio(String from, String to) {
        // 使用三方的 API 服务,  一个月 1000 次请求:
        // 1. https://openexchangerates.org/account (6*2=12 次一组, 1000 / 12 = 83 组)
        // 2. http://api.fixer.io/latest?base=USD (**** 免费, 只支持部分从银行获取)
        if(from.equalsIgnoreCase(to)) return 1f;
        try {
            String body = HTTP.get(String.format("https://www.exchangerate-api.com/%s/%s?k=%s", from, to,
                    System.getenv(Constant.EXCHANGERATE_TOKEN)));
            Logger.info("[1 %s TO %s %s]", from, body, to);
            return NumberUtils.toFloat(body.trim());
        } catch(Exception e) {
            Logger.warn(Webs.e(e));
        }
        return -1f;
    }

    /**
     * 返回 Market 对应的 Currency
     *
     * @param market
     * @return
     */
    public static Currency m(M market) {
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
            case AMAZON_AU:
                return AUD;
            case AMAZON_IN:
                return INR;
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
        String[] currencies = new String[]{"英镑", "港币", "美元", "欧元", "日元", "澳大利亚元", "印度卢比"};
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
     * 返回 Xe.com 市场的汇率 Table
     *
     * @return
     */
    public static String xeRatesHtml(Currency from) {
        String html = HTTP.get("http://www.xe.com/zh-CN/currencytables/?from=" + from.name());
        Document doc = Jsoup.parse(html);
        Elements trs = doc.select("#historicalRateTbl tbody tr");
        if(trs.isEmpty()) return StringUtils.EMPTY;

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

    public static void initCurrency() {
        Job job = new Job<Currency>() {
            @Override
            public void doJob() throws Exception {
                Currency.updateCRY();// 系统刚刚启动以后进行一次 Currency 的更新.
            }
        };
        job.every("8h");
        job.now();
    }
}

//http://www1.jctrans.com/tool/sjhb.htm

