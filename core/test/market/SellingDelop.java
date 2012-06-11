package market;

import helper.Constant;
import helper.Dates;
import helper.HTTP;
import helper.Webs;
import models.market.Account;
import models.market.Selling;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import play.Logger;
import play.libs.IO;
import play.test.UnitTest;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 5/12/12
 * Time: 5:08 PM
 */
public class SellingDelop extends UnitTest {
    //    @Test
    public void testDelop() {
        // UK 账户与 UK 的 Selling
        Account.<Account>findById(1l).loginWebSite();

        Selling sell = Selling.findById("68-MAGGLASS-3X75BG,B001OQOK5U_amazon.co.uk");
        sell.deploy();
    }

    //    @Test
    public void testDelop2() {
        // UK 的账户与 DE 的 Selling
        Account.<Account>findById(1l).loginWebSite();
        Selling sell = Selling.findById("15SSI9100-MR2SP_amazon.de");
        sell.deploy();
    }

    //    @Test
    public void testDelop3() {
        // DE 的账户与 DE 的 Selling
        Account.<Account>findById(2l).loginWebSite();
        Selling sell = Selling.findById("72LNA1-C2P_amazon.de");
        sell.deploy();
    }

    //    @Test
    public void testParseSelectVal() {
        String select = "<select name=\"item_width-uom\" onchange=\"setFieldValues('item_length-uom,item_height-uom', this.value);\"><option value=\"\">- Select -</option><option value=\"IN\">Inches</option>\n" +
                "<option value=\"FT\">Feet</option>\n" +
                "<option value=\"MM\">Millimeters</option>\n" +
                "<option value=\"CM\" selected=\"selected\">Centimeters</option>\n" +
                "<option value=\"DM\">Decimeters</option>\n" +
                "<option value=\"M\">Meters</option></select>";
        Element sel = Jsoup.parse(select).select("select").first();
        System.out.println(sel.attr("name") + ":::" + sel.select("option[selecte]").val());
    }

    //    @Test
    public void testErrorMessage() throws IOException {
        Document doc = Jsoup.parse(new File("/Users/wyattpan/elcuk2-data/80-QW1A56-BE_B005JSG7GE_posted_2.html"), "UTF-8");
        Elements error = doc.select(".messageboxerror li");
        if(error.size() > 0)
            System.out.println(error.text());
    }

    //    @Test
    public void testParseHTML() throws IOException {
        Selling sell = Selling.findById("68-MAGGLASS-3X75BG,B001OQOK5U_amazon.co.uk");
        String body = FileUtils.readFileToString(new File("/Users/wyattpan/elcuk2-data/68-MAGGLASS-3X75BG,B001OQOK5U_B001OQOK5U.html"));
        Document doc = Jsoup.parse(body);
        Elements inputs = doc.select("form[name=productForm] input");
        if(inputs.size() == 0) {
            Logger.warn("Listing Update Page Error! Log to ....?");
            return;
        }
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        for(Element el : inputs) {
            String name = el.attr("name").toLowerCase().trim();
            if("our_price".equals(name) && sell.price != null && sell.price > 0) {
                params.add(new BasicNameValuePair(name, sell.price.toString()));
            } else if("discounted_price".equals(name) ||
                    "discounted_price_start_date".equals(name) ||
                    "discounted_price_end_date".equals(name)) {
                if(sell.aps.startDate != null && sell.aps.endDate != null && sell.aps.salePrice != null && sell.aps.salePrice > 0) {
                    params.add(new BasicNameValuePair("discounted_price", sell.aps.salePrice.toString()));
                    params.add(new BasicNameValuePair("discounted_price_start_date", Dates.listingUpdateFmt(sell.market, sell.aps.startDate)));
                    params.add(new BasicNameValuePair("discounted_price_end_date", Dates.listingUpdateFmt(sell.market, sell.aps.endDate)));
                }
            } else if("product_description".equals(name) && StringUtils.isNotBlank(sell.aps.productDesc)) {
                params.add(new BasicNameValuePair(name, sell.aps.productDesc));
            } else if(StringUtils.startsWith(name, "generic_keywords") && StringUtils.isNotBlank(sell.aps.searchTerms)) {
                String[] searchTermsArr = StringUtils.splitByWholeSeparatorPreserveAllTokens(sell.aps.searchTerms, Webs.SPLIT);
                for(int i = 0; i < searchTermsArr.length; i++) {
                    params.add(new BasicNameValuePair("generic_keywords[" + i + "]", searchTermsArr[i]));
                }
                // length = 3, 0~2, need 3,4
                int missingIndex = 5 - searchTermsArr.length; // missingIndex = 5 - 3 = 2
                if(missingIndex > 0) {
                    for(int i = 1; i <= missingIndex; i++) {
                        params.add(new BasicNameValuePair("generic_keywords[" + (searchTermsArr.length + i) + "]", ""));
                    }
                }
            } else {
                params.add(new BasicNameValuePair(name, el.val()));
            }
        }
        // 3. 提交
        String[] args = StringUtils.split(doc.select("form[name=productForm]").first().attr("action"), ";");
        ///abis/product/ProcessEditProduct;jsessionid=B8595C92B8A8C968BD2B3A1C6BDD3CAD
        //https://catalog-sc.amazon.co.uk/abis/product/ProcessEditProduct
        body = HTTP.post(sell.account.cookieStore(),
                Account.M.listingPostPage(sell.market, (args.length >= 2 ? args[1] : "")),
                params);

        IO.writeContent(body, new File(String.format("%s/%s_%s_posted.html", Constant.E_DATE, sell.merchantSKU, sell.asin)));
    }


    @Test
    public void tesSyncBullet() {
        Selling sell = Selling.findById("80QW84-2AECUPB|A_UK|1");
        Set<NameValuePair> params = new HashSet<NameValuePair>();
        sell.aps.bulletPointsCheck(params);
    }
}
