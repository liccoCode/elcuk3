package controllers;

import com.alibaba.fastjson.JSON;
import ext.LinkExtensions;
import helper.GTs;
import helper.Webs;
import jobs.SellingRecordCheckJob;
import models.Ret;
import models.embedded.AmazonProps;
import models.market.Listing;
import models.market.Selling;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.helper.Validate;
import play.data.binding.As;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
@With({Secure.class, GzipFilter.class})
public class Sellings extends Controller {


    /**
     * 将指定 merchantSKU 的 Selling 与指定的 listingId 进行关联
     *
     * @param msku
     * @param listingId
     */
    public static void assoListing(String msku, String listingId) {
        validation.required(msku);
        validation.required(listingId);

        Selling selling = Selling.find("merchantSKU=?", msku).first();
        Validate.notNull(selling);
        Listing listing = Listing.find("listingId=?", listingId).first();
        Validate.notNull(listing);
        selling.listing = listing;
        selling.save();
    }

    public static void selling(String sid) {
        Selling s = Selling.findById(sid);
        s.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(s.merchantSKU);
        renderArgs.put("sids", JSON.toJSONString(sellingAndSellingIds._2));
        render(s);
    }

    /**
     * 加载 Techical, SearchTerms, ProductDesc 三块信息的 JSON 格式给前台
     */
    public static void tsp(String sid) {
        Selling s = Selling.findById(sid);// 利用 hibernate 二级缓存, Play 的 JavaBean 填充的查询语句含有 limit 语句
        s.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
        renderJSON(JSON.toJSONString(GTs.MapBuilder
                .map("t", s.aps.keyFeturess)
                .put("s", s.aps.searchTermss)
                .put("p", Arrays.asList(s.aps.productDesc))
                .build()));
    }

    public static void imageUpload(Selling s, String imgs) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ")" + "不存在!"));
        if(StringUtils.isBlank(s.aps.imageName) && StringUtils.isBlank(imgs)) renderJSON(new Ret("图片信息不能为空!"));
        try {
            s.uploadAmazonImg(imgs, false);
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret(true, LinkExtensions.asinLink(s)));
    }

    /*Play 在绑定内部的 Model 的时候与 JPA 想法不一致, TODO 弄清理 Play 怎么处理 Model 的*/
    public static void update(Selling s, boolean remote) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ") 不存在!"));
        try {
            if(!remote) { // 非远程, 本地更新
                s.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
                s.save();
            } else { // 远程更新
                s.deploy();
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(Webs.G(s));
    }

    /**
     * 从 Amazon 上将 Selling 信息同步回来
     */
    public static void syncAmazon(String sid) {
        Selling selling = Selling.findById(sid);
        try {
            selling.syncFromAmazon();
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
        renderJSON(new Ret());
    }

    public static void record(@As("yyyy-MM-dd") Date date, int t) {
        SellingRecordCheckJob job = new SellingRecordCheckJob();
        DateTime dt = new DateTime(date);
        for(int i = 0; i < t; i++) {
            job.fixTime = dt.plusDays(i);
            try {
                job.now().get(60, TimeUnit.SECONDS);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
}
