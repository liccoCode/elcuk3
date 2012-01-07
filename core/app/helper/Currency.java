package helper;

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
            return value * 1;
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
            return (float) (value * 0.8592); // 1 EUR = 0.8592 GBP
        }
    },
    /**
     * 人民币
     */
    CNY {
        /**
         * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
         */
        @Override
        public Float toGBP(Float value) {
            return (float) (value * 0.1004); // 1 CNY = 0.1004 GBP
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
            return (float) (value * 0.6400); // 1 USD = 0.6400 GBP
        }
    };

    /**
     * 每一个枚举都有这个, 用来将自己的值转换成 GBP 的值
     *
     * @return
     */
    public abstract Float toGBP(Float value);
}

//http://www1.jctrans.com/tool/sjhb.htm

