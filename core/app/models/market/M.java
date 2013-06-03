package models.market;


import exception.NotSupportChangeRegionFastException;
import helper.Dates;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.Date;

/**
 * 不同的 Market place
 * ps:
 * 添加一个新市场需要:
 * 1. M 中的 Market Type
 * 2. FeedbackCrawlJob Feedback 抓取
 * 3. KeepSessionJob 保持网站登陆的任务
 * 4. 订单抓取
 */
public enum M {
    AMAZON_UK {
        @Override
        public String label() {
            return "英国亚马逊";
        }
    },
    AMAZON_DE {
        @Override
        public String label() {
            return "德国亚马逊";
        }
    },
    AMAZON_FR {
        @Override
        public String label() {
            return "法国亚马逊";
        }
    },
    AMAZON_IT {
        @Override
        public String label() {
            return "意大利亚马逊";
        }
    },
    AMAZON_ES {
        @Override
        public String label() {
            return "西班牙亚马逊";
        }
    },
    AMAZON_US {
        @Override
        public String label() {
            return "美国亚马逊";
        }
    },
    EBAY_UK {
        @Override
        public String label() {
            return "英国 Ebay";
        }
    };

    public abstract String label();


    /**
     * 为 Amazon 不同市场的 Id, 与 Market 对应
     */
    public enum MID {
        /**
         * UK
         */
        A1F83G8C2ARO7P,
        /**
         * DE
         */
        A1PA6795UKMFR9,
        /**
         * FR
         */
        A13V1IB3VIYZZH,
        /**
         * US
         */
        ATVPDKIKX0DER,
        /**
         * IT
         */
        APJ6JRA9NG5V4,
        /**
         * ES
         */
        A1RKKUPIHCS9HS,
        EBAY_UK;

        public M market() {
            switch(this) {
                case A1F83G8C2ARO7P:
                    return AMAZON_UK;
                case A1PA6795UKMFR9:
                    return AMAZON_DE;
                case A13V1IB3VIYZZH:
                    return AMAZON_FR;
                case ATVPDKIKX0DER:
                    return AMAZON_US;
                case APJ6JRA9NG5V4:
                    return AMAZON_IT;
                case A1RKKUPIHCS9HS:
                    return AMAZON_ES;
                case EBAY_UK:
                default:
                    return M.EBAY_UK;
            }

        }
    }


    /**
     * Amazon MarketPlaceId, 在 Amazon 上用来区分 uk/de/us 等等市场的 ID
     *
     * @return
     */
    public MID amid() {
        switch(this) {
            case AMAZON_UK:
                return MID.A1F83G8C2ARO7P;
            case AMAZON_DE:
                return MID.A1PA6795UKMFR9;
            case AMAZON_FR:
                return MID.A13V1IB3VIYZZH;
            case AMAZON_IT:
                return MID.APJ6JRA9NG5V4;
            case AMAZON_ES:
                return MID.A1RKKUPIHCS9HS;
            case AMAZON_US:
                return MID.ATVPDKIKX0DER;
            case EBAY_UK:
            default:
                return MID.EBAY_UK;
        }
    }

    public boolean isAmazon() {
        return (StringUtils.startsWith(this.name(), "AMAZON"));
    }

    public boolean isEbay() {
        return (StringUtils.startsWith(this.name(), "EBAY"));
    }

    /**
     * 返回 html 页面中单位的格式
     *
     * @return
     */
    public String htmlCurrency() {
        switch(this) {
            case AMAZON_US:
                return "$";
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return "&euro;";
            case AMAZON_UK:
            case EBAY_UK:
            default:
                return "&pound;";
        }
    }

    public String toString() {
        switch(this) {
            case AMAZON_UK:
                return "amazon.co.uk";
            case AMAZON_DE:
                return "amazon.de";
            case AMAZON_FR:
                return "amazon.fr";
            case AMAZON_IT:
                return "amazon.it";
            case AMAZON_ES:
                return "amazon.es";
            case AMAZON_US:
                return "amazon.com";
            case EBAY_UK:
                return "ebay.co.uk";
            default:
                return "amazon.co.uk";
        }
    }

    public String nickName() {
        switch(this) {
            case AMAZON_UK:
                return "A_UK";
            case AMAZON_DE:
                return "A_DE";
            case AMAZON_FR:
                return "A_FR";
            case AMAZON_IT:
                return "A_IT";
            case AMAZON_ES:
                return "A_ES";
            case AMAZON_US:
                return "A_US";
            case EBAY_UK:
                return "E_UK";
            default:
                return "A_UK";
        }
    }

    /**
     * 账户对应的网站的后台首页
     *
     * @return
     */
    public String sellerCentralHomePage() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return "https://sellercentral." + this.toString();
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    /**
     * 账户对应的网站的后台登陆 URL
     *
     * @return
     */
    public String sellerCentralLogIn() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return "https://sellercentral." + this.toString() +
                        "/gp/sign-in/sign-in.html/ref=xx_login_lgin_home";
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    /**
     * 访问 Amazon 的普通账户的登陆页面(需要使用 openId)
     * PS: 普通账户的登陆地址 Amazon 会自动生成, 需要通过抓取获得
     *
     * @return
     */
    public String amazonSiteLogin() {
        /**
         * https://www.amazon.co.uk/ap/signin?_encoding=UTF8
         *
         * &openid.assoc_handle=***gb***flex
         *
         * &openid.mode=checkid_setup
         * &openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select
         * &openid.ns=http://specs.openid.net/auth/2.0
         * &openid.pape.max_auth_age=0
         * &openid.ns.pape=http://specs.openid.net/extensions/pape/1.0
         * &openid.identity=http://specs.openid.net/auth/2.0/identifier_select
         */
        String baseUrl = "https://www.%s/ap/signin?_encoding=UTF8" +//一个是域名
                "&openid.assoc_handle=%sflex" + //一个是区域
                "&openid.mode=checkid_setup" +
                "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select" +
                "&openid.ns=http://specs.openid.net/auth/2.0" +
                "&openid.pape.max_auth_age=0" +
                "&openid.ns.pape=http://specs.openid.net/extensions/pape/1.0" +
                "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select";
        switch(this) {
            case AMAZON_UK:
                return String.format(baseUrl, this.toString(), "gb");
            case AMAZON_DE:
                return String.format(baseUrl, this.toString(), "de");
            case AMAZON_ES:
                return String.format(baseUrl, this.toString(), "es");
            case AMAZON_FR:
                return String.format(baseUrl, this.toString(), "fr");
            case AMAZON_IT:
                return String.format(baseUrl, this.toString(), "it");
            case AMAZON_US:
                return String.format(baseUrl, this.toString(), "us");
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    public String amazonSiteHome() {
        //https://www.amazon.de/gp/yourstore/home
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String.format("https://www.%s/gp/yourstore/home", this.toString());
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    public String amazonLikeLink() {
        //http://www.amazon.de/gp/like/external/submit.html/ref=pd_like_submit_like_dp?_cachebust=0.7498981582466513
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                /*最后的 _cachebust 为随即生成的值*/
                return String
                        .format("http://www.%s/gp/like/external/submit.html/ref=pd_like_submit_like_dp?_cachebust=0.7498981582466513",
                                this.toString());
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    /**
     * 账户 Wishlist的网站抓取地址
     *
     * @return
     */
    public String amazonWishList() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("http://www.%s/gp/registry/wishlist", this.toString());
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    public String amazonNewWishList() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("https://www.%s/gp/registry/wishlist/ref=cm_wl_rl-create-pub-list", this.toString());
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    public String amazonAsinLink(String asin) {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("http://www.%s/dp/%s", this.toString(), asin);
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    /**
     * 账户对应的网站的 feedback 抓取的地址
     *
     * @param page
     * @return
     */
    public String feedbackPage(int page) {
        //https://sellercentral.amazon.co.uk/gp/feedback-manager/view-all-feedback.html?ie=UTF8&sortType=sortByDate&pageSize=50&dateRange=&descendingOrder=1&currentPage=1
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return "https://sellercentral." + this.toString() +
                        "/gp/feedback-manager/view-all-feedback.html?ie=UTF8&sortType=sortByDate&pageSize=50&dateRange=&descendingOrder=1&currentPage=" +
                        page;
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    /**
     * 仅仅只有 Amazon Europe 才有的区域转换
     *
     * @param marketplaceID
     * @return
     */
    public String changeRegion(String marketplaceID) {
        //https://sellercentral.amazon.de/gp/utilities/set-rainier-prefs.html?ie=UTF8&marketplaceID=A1PA6795UKMFR9
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return "https://sellercentral." + this.toString() +
                        "/gp/utilities/set-rainier-prefs.html?ie=UTF8&marketplaceID=" +
                        marketplaceID;
            case AMAZON_US:
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * Amazon 后台的订单页面
     *
     * @param oid
     * @return
     */
    public String orderDetail(String oid) {
        //https://sellercentral.amazon.co.uk/gp/orders-v2/details?orderID=203-5364157-2572327
        switch(this) {
            case AMAZON_UK:
            case AMAZON_US:
                return "https://sellercentral." + this.toString() +
                        "/gp/orders-v2/details?orderID=" + oid;
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return "https://sellercentral.amazon.de" +
                        "/gp/orders-v2/details?orderID=" + oid;
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * <pre>
     * 在详细订单页面会自行通过 Ajax 去获取此订单关联的 Feedback 的链接;
     * 因为订单详细页面的 feedback 数据也是 ajax 获取的.
     * 需要如下参数(POST):
     * action:show-feedback
     * orderID:303-8171136-0010717
     * applicationPath:/gp/orders-v2
     * </pre>
     *
     * @return
     */
    public String feedbackLink() {
        //https://sellercentral.amazon.de/gp/orders-v2/remote-actions/action.html
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return "https://sellercentral." + this.toString() +
                        "/gp/orders-v2/remote-actions/action.html";
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 模拟人工上架使用的链接.
     * - Amazon: 由于 Amazon 上架方式, 其需要访问两次这个页面, 一次 CLASSIFY 一次 IDENTIFY
     *
     * @return
     */
    public String saleSellingLink() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/Classify/SelectCategory",
                        this.toString());
            case AMAZON_US:
                return String
                        .format("https://catalog.%s/abis/Classify/SelectCategory", this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 模拟人工上架使用的链接.
     * - Amazon: 创建 Listing 的提交地址
     *
     * @return
     */
    public String saleSellingPostLink() {
        //https://catalog-sc.amazon.co.uk/abis/product/ProcessCreateProduct
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/product/ProcessCreateProduct",
                        this.toString());
            case AMAZON_US:
                return String.format("https://catalog.%s/abis/product/ProcessCreateProduct",
                        this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }


    /**
     * 模拟人工上架使用的链接.
     * - Amazon: 创建全新的 Listing 的时候, 最后需要回掉方法寻找 New UPC 对应的 ASIN
     *
     * @return
     */
    public String productCreateStatusLink() {
        // https://sellercentral.amazon.de/myi/search/ajax/ProductCreateStatus
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String.format("https://sellercentral.%s/myi/search/ajax/ProductCreateStatus",
                        this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    public String matchAsinAjaxLink() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/product/ajax/Match.ajax",
                        this.toString());
            case AMAZON_US:
                return String
                        .format("https://catalog.%s/abis/product/ajax/Match.ajax", this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    public String saleSellingStateLink() {
        //https://sellercentral.amazon.de/gp/ezdpc-gui/inventory-status/status.html
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/ezdpc-gui/inventory-status/status.html",
                                this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    public String uploadImageLink() {
        //https://catalog-sc.amazon.co.uk/abis/image/AddImage.ajax
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return String
                        .format("https://catalog-sc.%s/abis/image/AddImage.ajax", this.toString());
            case AMAZON_US:
                return String
                        .format("https://catalog.%s/abis/image/AddImage.ajax", this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    public String removeImageLink() {
        //https://catalog-sc.amazon.co.uk/abis/image/RemoveImage.ajax
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/image/RemoveImage.ajax",
                        this.toString());
            case AMAZON_US:
                return String
                        .format("https://catalog.%s/abis/image/RemoveImage.ajax", this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 这个是根据 Account 所在地来确定, 不同的市场需要先进行 region 切换
     * Amazon 不同 Listing 的 Session, PageView 数据
     *
     * @param from
     * @param to
     * @param currentPage
     * @return
     */
    public String salesAndTrafficByAsinLink(Date from, Date to, int currentPage) {
        /**
         * https://sellercentral.amazon.co.uk/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?
         * fromDate=12/05/2012&
         * toDate=12/05/2012&
         * reportID=102:DetailSalesTrafficBySKU&
         * currentPage=0
         */
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                // 在 DE 使用了 英文页面以后与 UK 一样了. 可 US 得另外设置日期字符串格式 - -||
                return String
                        .format("https://sellercentral.%s/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?" +
                                "fromDate=%s&toDate=%s&reportID=102:DetailSalesTrafficBySKU&currentPage=%s",
                                this.toString(),
                                Dates.listingUpdateFmt(AMAZON_UK, from),
                                Dates.listingUpdateFmt(AMAZON_UK, to), currentPage);
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?" +
                                "fromDate=%s&toDate=%s&reportID=102:DetailSalesTrafficBySKU&currentPage=%s",
                                this.toString(),
                                Dates.listingUpdateFmt(AMAZON_US, from),
                                Dates.listingUpdateFmt(AMAZON_US, to), currentPage);
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 下载 FBA 最新的 Label 的链接
     *
     * @return
     */
    public String fnSkuDownloadLink() {
        /**
         * https://sellercentral.amazon.de/gp/ssof/product-label.pdf/ref=ag_xx_cont_fbaprntlab?ie=UTF8&ascending=1&sortAttribute=MerchantSKU
         * 是 0(零) 不是 O(字母)
         * qty.0:27
         * fnSku.0:X0003U9PEH
         * mSku.0:10HTCG14-1900S
         * labelType:ItemLabel_A4_27
         */
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/ssof/product-label.pdf/ref=ag_xx_cont_fbaprntlab?ie=UTF8&ascending=1&sortAttribute=MerchantSKU",
                                this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 查看 FBA 容量的 FBA Inventory 页面
     *
     * @return
     */
    public String fbaCapacityPage() {
        //https://sellercentral.amazon.de/gp/ssof/knights/items-list.html/ref=ag_fbalist_cont_fbamnginv
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/ssof/knights/items-list.html/ref=ag_fbalist_cont_fbamnginv",
                                this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 获取某一个订单的 Transaction 信息的页面
     *
     * @param orderId
     * @return
     */
    public String oneTransactionFees(String orderId) {
        //https://sellercentral.amazon.com/gp/payments-account/view-transactions.html?orderId=110-6815187-8483453&view=search&range=all
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/payments-account/view-transactions.html?orderId=%s&view=search&range=all",
                                this.toString(), orderId);
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 下载 14days Payments 数据的链接
     *
     * @return
     */
    public String pastSettlementsUrl() {
        //https://sellercentral.amazon.de/gp/payments-account/past-settlements.html/ref=ag_xx_cont_payments
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/payments-account/past-settlements.html/ref=ag_xx_cont_payments",
                                this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * 将时间转换程对应市场的时间;
     * 例如: 2012-03-04 12:00:00 -> 2012-03-04 12:00:00 GMT-8 (US)
     *
     * @param from
     * @param to
     * @return
     */
    public F.T2<DateTime, DateTime> withTimeZone(Date from, Date to) {
        return new F.T2<DateTime, DateTime>(withTimeZone(from), withTimeZone(to));
    }


    /**
     * 将时间转换程对应市场的时间;
     * 例如: 2012-03-04 12:00:00 -> 2012-03-04 12:00:00 GMT-8 (US)
     *
     * @param time
     * @return
     */
    public DateTime withTimeZone(String time) {
        //yyyy-MM-dd HH:mm:ss
        if(time.contains(":")) {
            return Dates.fromDatetime(time, this);
        } else {
            return Dates.fromDate(time, this);
        }
    }

    /**
     * 将时间转换程对应市场的时间;
     * 例如: 2012-03-04 12:00:00 (?) -> 2012-03-04 12:00:00 GMT-8 (US)
     *
     * @param time
     * @return
     */
    public DateTime withTimeZone(Date time) {
        return withTimeZone(Dates.date2DateTime(time));
    }

    /**
     * 为时间根据市场增加市场时区调整
     *
     * @param time
     * @return
     */
    public DateTime toTimeZone(Date time) {
        return new DateTime(time, Dates.timeZone(this));
    }

    /**
     * 模拟人工方式修改 Listing 信息的地址
     *
     * @return
     */
    public static String listingEditPage(Selling sell) {
        //EU: https://catalog-sc.amazon.co.uk/abis/product/DisplayEditProduct?sku=71APNIP-BSLPU&asin=B007LE3Y88
        //US: https://catalog.amazon.com/abis/product/DisplayEditProduct?sku=71KDFHD7-BHSPU%2C656605389363&asin=B009A5E1DI&marketplaceID=ATVPDKIKX0DER
        String msku = sell.merchantSKU;
        if("68-MAGGLASS-3X75BG,B001OQOK5U".equalsIgnoreCase(sell.merchantSKU)) {
            msku = "68-MAGGLASS-3x75BG,B001OQOK5U";
        } else if("80-qw1a56-be,2".equalsIgnoreCase(sell.merchantSKU)) {
            msku = "80-qw1a56-be,2";
        } else if("80-qw1a56-be".equalsIgnoreCase(sell.merchantSKU)) {
            msku = "80-qw1a56-be";
        }
        switch(sell.market) {
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return String
                        .format("https://catalog-sc.amazon.de/abis/product/DisplayEditProduct?sku=%s&asin=%s",
                                msku, sell.asin);
            case AMAZON_UK:
                return String
                        .format("https://catalog-sc.%s/abis/product/DisplayEditProduct?sku=%s&asin=%s",
                                sell.account.type.toString()/*更新的链接需要账号所在地的 URL*/, msku, sell.asin);
            case AMAZON_US:
                return String
                        .format("https://catalog.%s/abis/product/DisplayEditProduct?sku=%s&asin=%s",
                                sell.account.type.toString()/*更新的链接需要账号所在地的 URL*/, msku, sell.asin);
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }


    public static String listingPostPage(M market, String jsessionId) {
        //EU: https://catalog-sc.amazon.co.uk/abis/product/ProcessEditProduct
        //US: https://catalog.amazon.co.uk/abis/product/ProcessEditProduct
        switch(market) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return "https://catalog-sc." + market.toString() +
                        "/abis/product/ProcessEditProduct" +
                        (StringUtils.isNotBlank(jsessionId) ? ";" + jsessionId : "");
            case AMAZON_US:
                return "https://catalog." + market.toString() + "/abis/product/ProcessEditProduct" +
                        (StringUtils.isNotBlank(jsessionId) ? ";" + jsessionId : "");
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    public static M val(String str) {
        String s = str.toLowerCase();
        if(s.equals("auk") || s.equals("amazon_uk") || s.equals("amazon.co.uk") ||
                s.equals("www.amazon.co.uk")) {
            return AMAZON_UK;
        } else if(s.equals("afr") || s.equals("amazon_fr") || s.equals("amazon.fr") ||
                s.equals("www.amazon.fr")) {
            return AMAZON_FR;
        } else if(s.equals("aes") || s.equals("amazon_es") || s.equals("amazon.es") ||
                s.equals("www.amazon.es")) {
            return AMAZON_ES;
        } else if(s.equals("ade") || s.equals("amazon_de") || s.equals("amazon.de") ||
                s.equals("www.amazon.de")) {
            return AMAZON_DE;
        } else if(s.equals("ait") || s.equals("amazon_it") || s.equals("amazon.it") ||
                s.equals("www.amazon.it")) {
            return AMAZON_IT;
        } else if(s.equals("aus") || s.equals("amazon_us") || s.equals("amazon.com") ||
                s.equals("www.amazon.com")) {
            return AMAZON_US;
        } else if(s.equals("euk") || s.equals("ebay_uk") || s.equals("ebay.co.uk") ||
                s.equals("www.ebay.co.uk")) {
            return EBAY_UK;
        } else {
            return null;
        }
    }
}
