package models.market;

import exception.NotSupportChangeRegionFastException;
import helper.Currency;
import helper.Dates;
import models.OperatorConfig;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 不同的 Market place
 * ps:
 * 添加一个新市场需要:
 * 1. M 中的 Market Type
 * 2. KeepSessionJob 保持网站登陆的任务
 */
public enum M {
    AMAZON_UK {
        @Override
        public String label() {
            return "英国亚马逊";
        }

        @Override
        public String countryName() {
            return "英国";
        }

        @Override
        public String pic() {
            return "flag-icon-gb";
        }
    }, AMAZON_DE {
        @Override
        public String label() {
            return "德国亚马逊";
        }

        @Override
        public String countryName() {
            return "德国";
        }

        @Override
        public String pic() {
            return "flag-icon-de";
        }
    }, AMAZON_FR {
        @Override
        public String label() {
            return "法国亚马逊";
        }

        @Override
        public String countryName() {
            return "法国";
        }

        @Override
        public String pic() {
            return "flag-icon-fr";
        }
    }, AMAZON_IT {
        @Override
        public String label() {
            return "意大利亚马逊";
        }

        @Override
        public String countryName() {
            return "意大利";
        }

        @Override
        public String pic() {
            return "flag-icon-it";
        }
    }, AMAZON_ES {
        @Override
        public String label() {
            return "西班牙亚马逊";
        }

        @Override
        public String countryName() {
            return "西班牙";
        }

        @Override
        public String pic() {
            return "flag-icon-es";
        }
    }, AMAZON_US {
        @Override
        public String label() {
            return "美国亚马逊";
        }

        @Override
        public String countryName() {
            return "美国";
        }

        @Override
        public String pic() {
            return "flag-icon-us";
        }
    }, EBAY_UK {
        @Override
        public String label() {
            return "英国 Ebay";
        }

        @Override
        public String countryName() {
            return "英国";
        }

        @Override
        public String pic() {
            return "flag-icon-gb";
        }
    }, AMAZON_JP {
        @Override
        public String label() {
            return "日本亚马逊";
        }

        @Override
        public String countryName() {
            return "日本";
        }

        @Override
        public String pic() {
            return "flag-icon-jp";
        }
    }, AMAZON_CA {
        @Override
        public String label() {
            return "加拿大亚马逊";
        }

        @Override
        public String countryName() {
            return "加拿大";
        }

        @Override
        public String pic() {
            return "flag-icon-ca";
        }
    }, AMAZON_MX {
        @Override
        public String label() {
            return "墨西哥亚马逊";
        }

        @Override
        public String countryName() {
            return "墨西哥";
        }

        @Override
        public String pic() {
            return "flag-icon-mx";
        }
    }, AMAZON_AU {
        @Override
        public String label() {
            return "澳大利亚亚马逊";
        }

        @Override
        public String countryName() {
            return "澳大利亚";
        }

        @Override
        public String pic() {
            return "flag-icon-au";
        }
    }, AMAZON_IN {
        @Override
        public String label() {
            return "印度亚马逊";
        }

        @Override
        public String countryName() {
            return "印度";
        }

        @Override
        public String pic() {
            return "flag-icon-in";
        }
    };

    public abstract String label();

    public abstract String countryName();

    public abstract String pic();

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
        EBAY_UK,
        /**
         * JP
         */
        A1VC38T7YXB528,
        /**
         * CA
         */
        A2EUQ1WTGCTBG2,
        /**
         * MX
         */
        A1AM78C64UM0Y8,
        /**
         * AU
         */
        A39IBJ37TRP1C6,
        /**
         * IN
         */
        A21TJRUUN4KGV;

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
                case A1VC38T7YXB528:
                    return AMAZON_JP;
                case A2EUQ1WTGCTBG2:
                    return AMAZON_CA;
                case A1AM78C64UM0Y8:
                    return AMAZON_MX;
                case A39IBJ37TRP1C6:
                    return AMAZON_AU;
                case A21TJRUUN4KGV:
                    return AMAZON_IN;
                case EBAY_UK:
                default:
                    return M.EBAY_UK;
            }

        }
    }

    public String toMerchantIdentifier() {
        switch(this) {
            case AMAZON_CA:
                return "AE1AM2BP4WMK9";
            case AMAZON_US:
                return "M_EASYACCAST_1160595";
            case AMAZON_DE:
                return "M_EASYACCDE_11449864";
            case AMAZON_ES:
                return "M_EASYACCDE_11449864";
            case AMAZON_FR:
                return "M_EASYACCFST_1493129";
            case AMAZON_JP:
                return "M_EASYACCJST_2396604";
            case AMAZON_IT:
                return "M_YEPINGXIE_14931284";
            case AMAZON_UK:
                return "M_EASYACC_104529564";
            default:
                return "";
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
            case AMAZON_JP:
                return MID.A1VC38T7YXB528;
            case AMAZON_CA:
                return MID.A2EUQ1WTGCTBG2;
            case AMAZON_MX:
                return MID.A1AM78C64UM0Y8;
            case AMAZON_AU:
                return MID.A39IBJ37TRP1C6;
            case AMAZON_IN:
                return MID.A21TJRUUN4KGV;
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
            case AMAZON_CA:
                return "CAD";
            case AMAZON_US:
                return "$";
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_JP:
                return "JPY¥;";
            case AMAZON_IT:
                return "&euro;";
            case AMAZON_UK:
            case EBAY_UK:
            default:
                return "&pound;";
        }
    }

    public Currency currency() {
        switch(this) {
            case AMAZON_CA:
                return Currency.CAD;
            case AMAZON_US:
                return Currency.USD;
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return Currency.EUR;
            case AMAZON_JP:
                return Currency.JPY;
            case AMAZON_MX:
                return Currency.MXN;
            case AMAZON_AU:
                return Currency.AUD;
            case AMAZON_IN:
                return Currency.INR;
            case AMAZON_UK:
            case EBAY_UK:
            default:
                return Currency.GBP;
        }
    }

    public String toString() {
        switch(this) {
            case AMAZON_CA:
                return "amazon.ca";
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
            case AMAZON_JP:
                return "amazon.co.jp";
            case AMAZON_MX:
                return "amazon.com.mx";
            case AMAZON_AU:
                return "amazon.com.au";
            case AMAZON_IN:
                return "amazon.in";
            default:
                return "amazon.co.uk";
        }
    }

    public String nickName() {
        switch(this) {
            case AMAZON_CA:
                return "A_CA";
            case AMAZON_UK:
                return "A_UK";
            case AMAZON_DE:
                return "A_DE";
            case AMAZON_FR:
                return "A_FR";
            case AMAZON_IT:
                return "A_IT";
            case AMAZON_JP:
                return "A_JP";
            case AMAZON_ES:
                return "A_ES";
            case AMAZON_US:
                return "A_US";
            case EBAY_UK:
                return "E_UK";
            case AMAZON_MX:
                return "A_MX";
            default:
                return "A_UK";
        }
    }

    public String sortName() {
        switch(this) {
            case AMAZON_CA:
                return "ca";
            case AMAZON_UK:
                return "uk";
            case AMAZON_DE:
                return "de";
            case AMAZON_FR:
                return "fr";
            case AMAZON_IT:
                return "it";
            case AMAZON_JP:
                return "jp";
            case AMAZON_ES:
                return "es";
            case AMAZON_US:
                return "us";
            case AMAZON_MX:
                return "mx";
            case EBAY_UK:
                return "uk";
            default:
                return "uk";
        }
    }

    /**
     * 账户对应的网站的后台首页
     *
     * @return
     */
    public String sellerCentralHomePage() {
        switch(this) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return "https://sellercentral." + this.toString();
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
        /*
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
        String baseUrl = "https://www.%s/ap/signin?_encoding=UTF8" //一个是域名
                + "&openid.assoc_handle=%sflex"   //一个是区域
                + "&openid.mode=checkid_setup"
                + "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select"
                + "&openid.ns=http://specs.openid.net/auth/2.0"
                + "&openid.pape.max_auth_age=0"
                + "&openid.ns.pape=http://specs.openid.net/extensions/pape/1.0"
                + "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select";
        switch(this) {
            case AMAZON_CA:
                return String.format(baseUrl, this.toString(), "ca");
            case AMAZON_UK:
                return String.format(baseUrl, this.toString(), "gb");
            case AMAZON_DE:
                return String.format(baseUrl, this.toString(), "de");
            case AMAZON_ES:
                return String.format(baseUrl, this.toString(), "es");
            case AMAZON_FR:
                return String.format(baseUrl, this.toString(), "fr");
            case AMAZON_JP:
                return String.format(baseUrl, this.toString(), "jp");
            case AMAZON_IT:
                return String.format(baseUrl, this.toString(), "it");
            case AMAZON_US:
                return String.format(baseUrl, this.toString(), "us");
            case AMAZON_MX:
                return String.format(baseUrl, this.toString(), "mx");
            case AMAZON_AU:
                return String.format(baseUrl, this.toString(), "au");
            case AMAZON_IN:
                return String.format(baseUrl, this.toString(), "in");
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return String.format("http://www.%s/gp/registry/wishlist", this.toString());
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    public String amazonNewWishList() {
        switch(this) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return String.format("https://www.%s/gp/registry/wishlist/ref=cm_wl_rl-create-pub-list",
                        this.toString());
            case EBAY_UK:
                return "unknow..";
            default:
                return "Not Support.";
        }
    }

    public String amazonAsinLink(String asin) {
        switch(this) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return String.format("http://www.%s/dp/%s", this.toString(), asin);
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
        //https://sellercentral.amazon.co.uk/gp/feedback-manager/view-all-feedback.html?ie=UTF8
        // &sortType=sortByDate&pageSize=50&dateRange=&descendingOrder=1&currentPage=1
        switch(this) {
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return "https://sellercentral." + this.toString()
                        + "/gp/feedback-manager/view-all-feedback.html?ie=UTF8&sortType=sortByDate&pageSize=50"
                        + "&dateRange=&descendingOrder=1&currentPage=" + page;
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_IT:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return "https://sellercentral." + this.toString()
                        + "/gp/utilities/set-rainier-prefs.html?ie=UTF8&marketplaceID=" + marketplaceID;
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_IT: // TODO IT 与 FR 会有过渡阶段
            case AMAZON_FR:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return "https://sellercentral." + this.toString() + "/gp/orders-v2/details?orderID=" + oid;
            case AMAZON_DE:
            case AMAZON_ES:
                return "https://sellercentral.amazon.de/gp/orders-v2/details?orderID=" + oid;
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_US:
            case AMAZON_JP:
            case AMAZON_IT: // TODO IT 与 FR 会有过渡阶段
            case AMAZON_FR:
            case AMAZON_MX:
            case AMAZON_AU:
            case AMAZON_IN:
                return "https://sellercentral." + this.toString() + "/gp/orders-v2/remote-actions/action.html";
            case AMAZON_DE:
            case AMAZON_ES:
                return "https://sellercentral." + AMAZON_DE.toString() + "/gp/orders-v2/remote-actions/action.html";
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                // 在 DE 使用了 英文页面以后与 UK 一样了. 可 US 得另外设置日期字符串格式 - -||
                return String
                        .format("https://sellercentral.%s/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?"
                                        + "fromDate=%s&toDate=%s&reportID=102:DetailSalesTrafficBySKU&currentPage=%s",
                                this.toString(),
                                Dates.listingUpdateFmt(AMAZON_UK, from),
                                Dates.listingUpdateFmt(AMAZON_UK, to), currentPage);
            case AMAZON_MX:
            case AMAZON_US:
                return String
                        .format("https://sellercentral.%s/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?"
                                        + "fromDate=%s&toDate=%s&reportID=102:DetailSalesTrafficBySKU&currentPage=%s",
                                this.toString(),
                                Dates.listingUpdateFmt(AMAZON_US, from),
                                Dates.listingUpdateFmt(AMAZON_US, to), currentPage);
            case AMAZON_JP:
                return String
                        .format("https://sellercentral.%s/gp/site-metrics/load-report-JSON.html/ref=au_xx_cont_sitereport?"
                                        + "fromDate=%s&toDate=%s&reportID=102:DetailSalesTrafficBySKU&currentPage=%s",
                                this.toString(),
                                Dates.listingUpdateFmt(AMAZON_JP, from),
                                Dates.listingUpdateFmt(AMAZON_JP, to), currentPage);
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_MX:
            case AMAZON_JP:
            case AMAZON_AU:
            case AMAZON_IN:
                return String
                        .format("https://sellercentral.%s/gp/ssof/product-label.pdf/ref=ag_xx_cont_xx", this.toString());
            //https://sellercentral.amazon.it/gp/ssof/product-label.pdf/ref=ag_xx_cont_xx
            //https://sellercentral.amazon.it/gp/ssof/product-label.pdf/ref=ag_xx_cont_fbaprntlab?ie=UTF8&ascending=1&sortAttribute=MerchantSKU
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
            case AMAZON_US:
            case AMAZON_MX:
            case AMAZON_JP:
            case AMAZON_AU:
            case AMAZON_IN:
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
            case AMAZON_CA:
            case AMAZON_UK:
            case AMAZON_US:
            case AMAZON_MX:
            case AMAZON_JP:
            case AMAZON_IT:
            case AMAZON_FR:
                return String.format(
                        "https://sellercentral.%s/gp/payments-account/view-transactions.html?orderId=%s&view=search&range=all",
                        this.toString(), orderId);
            case AMAZON_DE:
            case AMAZON_ES:
                return String.format(
                        "https://sellercentral.amazon.de/gp/payments-account/view-transactions.html?orderId=%s&view=search&range=all",
                        orderId);
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
        return new F.T2<>(withTimeZone(from), withTimeZone(to));
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
     * 根据市场返回相对于北京时间的时差;
     * 例如:
     * DE: +2,  CN: +8 , -> 2 - 8 = -6
     * UK: +1,  CN: +8 , -> 1 - 8 = -7
     * US: -7,  CN: +8 , -> -7 - 8 = -15
     *
     * @return
     */
    public int timeZoneOffset() {
        switch(this) {
            case AMAZON_MX:
                return -14;
            case AMAZON_US:
                return -15;
            case AMAZON_CA:
                return -16;
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return -7;
            case EBAY_UK:
            case AMAZON_UK:
                return -8;
            case AMAZON_IN:
                return -3;
            default:
                return 0;
        }
    }

    public static M val(String str) {
        if(StringUtils.isBlank(str)) return null;
        String s = str.toLowerCase();
        if(s.equals("aca") || s.equals("amazon_ca") || s.equals("amazon.ca") || s.equals("ca")
                || s.equals("www.amazon.ca") || s.equals("fba_ca")) {
            return AMAZON_CA;
        } else if(s.equals("auk") || s.equals("amazon_uk") || s.equals("amazon.co.uk") || s.equals("gb")
                || s.equals("www.amazon.co.uk") || s.equals("fba_uk")) {
            return AMAZON_UK;
        } else if(s.equals("afr") || s.equals("amazon_fr") || s.equals("amazon.fr") || s.equals("fr")
                || s.equals("www.amazon.fr") || s.equals("fba_fr")) {
            return AMAZON_FR;
        } else if(s.equals("aes") || s.equals("amazon_es") || s.equals("amazon.es") || s.equals("es")
                || s.equals("www.amazon.es") || s.equals("fba_es")) {
            return AMAZON_ES;
        } else if(s.equals("ade") || s.equals("amazon_de") || s.equals("amazon.de") || s.equals("de")
                || s.equals("www.amazon.de") || s.equals("fba_de")) {
            return AMAZON_DE;
        } else if(s.equals("ait") || s.equals("amazon_it") || s.equals("amazon.it") || s.equals("it")
                || s.equals("www.amazon.it") || s.equals("fba_it")) {
            return AMAZON_IT;
        } else if(s.equals("ajp") || s.equals("amazon_jp") || s.equals("amazon.jp") || s.equals("jp")
                || s.equals("www.amazon.co.jp") || s.equals("fba_jp") || s.equals("amazon.co.jp")) {
            return AMAZON_JP;
        } else if(s.equals("aus") || s.equals("amazon_us") || s.equals("amazon.com") || s.equals("us")
                || s.equals("www.amazon.com") || s.equals("fba_us")) {
            return AMAZON_US;
        } else if(s.equals("amx") || s.equals("amazon_mx") || s.equals("amazon.com.mx") || s.equals("mx")
                || s.equals("www.amazon.com.mx") || s.equals("fba_mx")) {
            return AMAZON_MX;
        } else if(s.equals("euk") || s.equals("ebay_uk") || s.equals("ebay.co.uk")
                || s.equals("www.ebay.co.uk")) {
            return EBAY_UK;
        } else {
            return null;
        }
    }

    /**
     * 仓库与 Market 映射
     *
     * @return
     */
    public String marketAndWhouseMapping() {
        switch(this) {
            case AMAZON_CA:
                return "FBA_CA";
            case AMAZON_DE:
                return "FBA_DE";
            case AMAZON_US:
                return "FBA_US";
            case AMAZON_UK:
                return "FBA_UK";
            case AMAZON_IT:
                return "FBA_IT";
            case AMAZON_JP:
                return "FBA_JP";
            case AMAZON_FR:
                return "FBA_FR";
            case AMAZON_ES:
                return "FBA_ES";
            case AMAZON_MX:
                return "FBA_MX";
            case AMAZON_AU:
                return "FBA_AU";
            case AMAZON_IN:
                return "FBA_IN";
            default:
                return null;
        }
    }

    public String marketTransferEUR() {
        switch(this) {
            case AMAZON_CA:
                return "'FBA_CA'";
            case AMAZON_DE:
                return "'FBA_DE'";
            case AMAZON_US:
                return "'FBA_US'";
            case AMAZON_UK:
                return "'FBA_UK','EUR_UK'";
            case AMAZON_IT:
                return "'FBA_IT','EUR_IT'";
            case AMAZON_JP:
                return "'FBA_JP'";
            case AMAZON_FR:
                return "'FBA_FR','EUR_FR'";
            case AMAZON_ES:
                return "'FBA_ES','EUR_ES'";
            default:
                return null;
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
            case AMAZON_JP:
            case AMAZON_CA:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/product/ProcessEditProduct%s", market.toString(),
                        jsessionId);
            case AMAZON_MX:
            case AMAZON_US:
                return String.format("https://catalog.%s/abis/product/ProcessEditProduct%s", market.toString(),
                        jsessionId);
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
            case AMAZON_JP:
            case AMAZON_CA:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/image/AddImage.ajax", this.toString());
            case AMAZON_MX:
            case AMAZON_US:
                return String.format("https://catalog.%s/abis/image/AddImage.ajax", this.toString());
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
            case AMAZON_JP:
            case AMAZON_CA:
            case AMAZON_IT:
                return String.format("https://catalog-sc.%s/abis/image/RemoveImage.ajax", this.toString());
            case AMAZON_MX:
            case AMAZON_US:
                return String.format("https://catalog.%s/abis/image/RemoveImage.ajax", this.toString());
            case EBAY_UK:
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    public static M[] amazonVals() {
        return (M[]) ArrayUtils.removeElement(M.values(), M.EBAY_UK);
    }


    public static M toM(String market) {
        if(market.equals("amazon.ca")) {
            return AMAZON_CA;
        } else if(market.equals("amazon.co.uk")) {
            return AMAZON_UK;
        }
        if(market.equals("amazon.de")) {
            return AMAZON_DE;
        }
        if(market.equals("amazon.fr")) {
            return AMAZON_FR;
        }
        if(market.equals("amazon.it")) {
            return AMAZON_IT;
        }
        if(market.equals("amazon.es")) {
            return AMAZON_ES;
        }
        if(market.equals("amazon.com")) {
            return AMAZON_US;
        }
        if(market.equals("amazon.com.mx")) {
            return AMAZON_MX;
        }
        if(market.equals("ebay.co.uk")) {
            return EBAY_UK;
        }
        if(market.equals("amazon.co.jp")) {
            return AMAZON_JP;
        }
        if(market.equals("amazon.com.au")) {
            return AMAZON_AU;
        }
        if(market.equals("amazon.in")) {
            return AMAZON_IN;
        }
        return AMAZON_UK;
    }

    public String fulfillmentCenterID() {
        switch(this) {
            case AMAZON_JP:
                return "AMAZON_JP";
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return "AMAZON_EU";
            case AMAZON_CA:
            case AMAZON_US:
            case AMAZON_MX:
                return "AMAZON_NA";
            default:
                throw new NotSupportChangeRegionFastException();
        }
    }

    /**
     * CreateInboundShipmentPlan 时的 ShipToCountryCode 参数值
     * <p>
     * PS:
     * 只支持 北美 和 欧洲 市场
     * 印度市场请使用 ShipToCountrySubdivisionCode 参数
     * <p>
     * 详见:
     * http://docs.developer.amazonservices.com/en_US/fba_inbound/FBAInbound_CreateInboundShipmentPlan.html
     *
     * @return
     */
    public String country() {
        switch(this) {
            case AMAZON_CA:
                return "CA";
            case AMAZON_DE:
                return "DE";
            case AMAZON_ES:
                return "ES";
            case AMAZON_FR:
                return "FR";
            case AMAZON_IT:
                return "IT";
            case AMAZON_JP:
                return null;
            case AMAZON_UK:
                return "GB";
            case AMAZON_US:
                return "US";
            case AMAZON_MX:
                return "MX";
            case AMAZON_AU:
                return "AU";
            case AMAZON_IN:
                return "IN";
            default:
                return null;
        }
    }

    /**
     * 简写
     *
     * @return
     */
    public String shortHand() {
        switch(this) {
            case AMAZON_CA:
                return "CA";
            case AMAZON_DE:
                return "DE";
            case AMAZON_ES:
                return "ES";
            case AMAZON_FR:
                return "FR";
            case AMAZON_IT:
                return "IT";
            case AMAZON_JP:
                return "JP";
            case AMAZON_UK:
                return "UK";
            case AMAZON_US:
                return "US";
            case AMAZON_MX:
                return "MX";
            case AMAZON_AU:
                return "AU";
            case AMAZON_IN:
                return "IN";
            default:
                return null;
        }
    }

    public String uropeShow() {
        switch(this) {
            case AMAZON_UK:
            case AMAZON_DE:
            case AMAZON_ES:
            case AMAZON_FR:
            case AMAZON_IT:
                return "EU";
            case AMAZON_JP:
                return "JP";
            case AMAZON_CA:
                return "CA";
            case AMAZON_US:
                return "US";
            case AMAZON_MX:
                return "MX";
            case AMAZON_AU:
                return "AU";
            case AMAZON_IN:
                return "IN";
            default:
                return null;
        }
    }

    public String earChannel() {
        return OperatorConfig.getVal(this.sortName() + "earchannelid");
    }

    public static List<M> europeMarkets() {
        return Arrays.asList(M.AMAZON_DE, M.AMAZON_UK, M.AMAZON_IT, M.AMAZON_FR, M.AMAZON_ES);
    }
}
