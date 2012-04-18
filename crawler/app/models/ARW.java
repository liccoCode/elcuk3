package models;

/**
 * Amazon Review 的地址
 * User: wyattpan
 * Date: 4/18/12
 * Time: 1:45 PM
 */
public enum ARW {
    /**
     * Amazon.co.uk 的 Review 链接
     */
    UK {
        @Override
        public String listing(String asin) {
            return String.format("http://www.amazon.co.uk/dp/%s", asin);
        }

        @Override
        public String review(String asin, int page) {
            //http://www.amazon.co.uk/product-reviews/B006GPS9D8/ref=dp_top_cm_cr_acr_txt?ie=UTF8&showViewpoints=1
            return String.format("http://www.amazon.co.uk/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
        }
    },
    DE {
        @Override
        public String listing(String asin) {
            return String.format("http://www.amazon.de/dp/%s", asin);
        }

        @Override
        public String review(String asin, int page) {
            return String.format("http://www.amazon.de/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
        }
    },
    FR {
        @Override
        public String listing(String asin) {
            return String.format("http://www.amazon.fr/dp/%s", asin);
        }

        @Override
        public String review(String asin, int page) {
            return String.format("http://www.amazon.fr/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
        }
    },
    ES {
        @Override
        public String review(String asin, int page) {
            return String.format("http://www.amazon.es/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
        }

        @Override
        public String listing(String asin) {
            return String.format("http://www.amazon.es/dp/%s", asin);
        }
    },
    IT {
        @Override
        public String review(String asin, int page) {
            return String.format("http://www.amazon.it/product-reviews/%s?pageNumber=%s&sortBy=bySubmissionDateDescending", asin, page);
        }

        @Override
        public String listing(String asin) {
            return String.format("http://www.amazon.it/dp/%s", asin);
        }
    };

    public abstract String review(String asin, int page);

    public abstract String listing(String asin);

    /**
     * 根据 MT(market) 返回抓取 Review 的地址
     *
     * @param m
     * @param asin
     * @param page
     * @return
     */
    public static String review(MT m, String asin, int page) {
        switch(m) {
            case AUK:
                return UK.review(asin, page);
            case ADE:
                return DE.review(asin, page);
            case AFR:
                return FR.review(asin, page);
            case AES:
                return ES.review(asin, page);
            case AIT:
                return IT.review(asin, page);
            default:
                return "";
        }
    }

    public static String listing(MT m, String asin) {
        switch(m) {
            case AUK:
                return UK.listing(asin);
            case ADE:
                return DE.listing(asin);
            case AFR:
                return FR.listing(asin);
            case AES:
                return ES.listing(asin);
            case AIT:
                return IT.listing(asin);
            default:
                return "";
        }
    }
}
