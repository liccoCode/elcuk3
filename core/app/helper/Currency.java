package helper;

import com.google.gson.JsonObject;
import org.apache.commons.lang.math.NumberUtils;
import play.Logger;

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
        public Float toUSD(Float value) {
            return value * GBP_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * GBP_CNY;
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
        public Float toUSD(Float value) {
            return value * EUR_USD;
        }

        @Override
        public Float toCNY(Float value) {
            return value * EUR_CNY;
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
        public Float toUSD(Float value) {
            return value;
        }

        @Override
        public Float toCNY(Float value) {
            return value * USD_CNY;
        }
    };

    /**
     * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
     *
     * @return
     */
    public abstract Float toGBP(Float value);

    public abstract Float toEUR(Float value);

    public abstract Float toUSD(Float value);

    public abstract Float toCNY(Float value);

    //GBP
    private static float GBP_EUR = 1.20175971f;
    private static float GBP_CNY = 10.008345f;
    private static float GBP_USD = 1.5831f;

    //EUR
    private static float EUR_GBP = 0.831975238f;
    private static float EUR_CNY = 8.32669524f;
    private static float EUR_USD = 1.3171f;

    //CNY
    private static float CNY_GBP = 0.0999166193f;
    private static float CNY_EUR = 0.120095665f;
    private static float CNY_USD = 0.158165f;

    //USD
    private static float USD_GBP = 0.631672036f;
    private static float USD_CNY = 6.3225113f;
    private static float USD_EUR = 0.758495146f;

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

            // EUR
            tmp = ratio("EUR", "GBP");
            EUR_GBP = tmp > 0 ? tmp : EUR_GBP;
            tmp = ratio("EUR", "CNY");
            EUR_CNY = tmp > 0 ? tmp : EUR_CNY;
            tmp = ratio("EUR", "USD");
            EUR_USD = tmp > 0 ? tmp : EUR_USD;

            // CNY
            tmp = ratio("CNY", "GBP");
            CNY_GBP = tmp > 0 ? tmp : CNY_GBP;
            tmp = ratio("CNY", "EUR");
            CNY_EUR = tmp > 0 ? tmp : CNY_EUR;
            tmp = ratio("CNY", "USD");
            CNY_USD = tmp > 0 ? tmp : CNY_USD;

            // USD
            tmp = ratio("USD", "GBP");
            USD_GBP = tmp > 0 ? tmp : USD_GBP;
            tmp = ratio("USD", "CNY");
            USD_CNY = tmp > 0 ? tmp : USD_CNY;
            tmp = ratio("USD", "EUR");
            USD_EUR = tmp > 0 ? tmp : USD_EUR;
        }
    }

    @SuppressWarnings("unchecked")
    private static Float ratio(String from, String to) {
        if(from.equalsIgnoreCase(to)) return 1f;
        try {
            JsonObject json = HTTP.json(("http://www.google.com/ig/calculator?hl=en&q=1" + from + "=?" + to)).getAsJsonObject();
            Logger.info("[" + json.get("lhs").getAsString() + " TO " + json.get("rhs").getAsString() + "]");
            return NumberUtils.toFloat(json.get("rhs").getAsString().split(" ")[0], -1);
        } catch(Exception e) {
            Logger.warn(Webs.E(e));
        }
        return -1f;
    }
}

//http://www1.jctrans.com/tool/sjhb.htm

