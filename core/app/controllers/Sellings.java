package controllers;

import helper.Constant;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.embedded.AmazonProps;
import models.market.*;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;
import play.data.validation.Validation;
import play.jobs.Job;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
@With({GlobalExceptionHandler.class, Secure.class})
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

    public static void selling(String id) {
        Selling s = Selling.findById(id);
        s.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(s.merchantSKU);
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
        renderArgs.put("feeds", s.feeds());
        render(s);
    }

    /**
     * 加载 Techical, SearchTerms, ProductDesc 三块信息的 JSON 格式给前台
     */
    public static void tsp(String sid) {
        Selling s = Selling.findById(sid);// 利用 hibernate 二级缓存, Play 的 JavaBean 填充的查询语句含有 limit 语句
        s.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
        renderJSON(J.json(GTs.MapBuilder
                .map("t", s.aps.keyFeturess)
                .put("s", s.aps.searchTermss)
                .put("p", Arrays.asList(s.aps.productDesc))
                .build()));
    }

    /**
     * 添加 selling 页面
     */
    public static void blank() {
        render();
    }

    public static void create(String sku, String upc, String asin, String market, Account acc) {
        try {
            if(StringUtils.isBlank(sku)) Webs.error("SKU 必须存在");
            if(StringUtils.isBlank(upc)) Webs.error("UPC 必须存在");
            if(StringUtils.isBlank(asin)) Webs.error("ASIN 必须存在");
            if(StringUtils.isBlank(market)) Webs.error("Market 必须存在");

            String msku = String.format("%s,%s", sku.trim(), upc.trim());
            Selling selling = Selling.blankSelling(msku, asin, upc, acc, M.val(market));
            selling.patchToListing();
            flash.success("手动添加 Selling 成功.");
            Sellings.selling(selling.sellingId);
        } catch (FastRuntimeException e) {
            Validation.addError("", e.getMessage());
            render("Sellings/blank.html");
        }
    }

    public static void changeListing(Selling s, String listingId) {
        try {
            Listing lst = Listing.findById(listingId);
            if(lst == null) Webs.error("Listing " + listingId + "不存在");
            String oldListingId = s.listing.listingId;
            s.changeListing(lst);
            flash.success("成功将 Selling %s 从 %s 转移到 %s", s.sellingId, oldListingId, listingId);
            Sellings.selling(s.sellingId);
        } catch (FastRuntimeException e) {
            flash.error(e.getMessage());
            Sellings.selling(s.sellingId);
        }
    }

    /*Play 在绑定内部的 Model 的时候与 JPA 想法不一致, TODO 弄清理 Play 怎么处理 Model 的*/
    public static void update(Selling s) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ") 不存在!"));
        try {
            s.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
            s.save();
            renderJSON(new Ret(true, s.sellingId));
        } catch (Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void deploy(String id) {
        //10SMI9300-2200S|A_UK|1
        Selling s = Selling.findById(id);
        try {
            Feed feed = s.deploy();
            renderJSON(feed);
        } catch (Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 从 Amazon 上将 Selling 信息同步回来
     */
    public static void syncAmazon(final String sid) {
        // play status 检查平均耗时 2.5s , 开放线程时间 3s 后回掉
        await(new Job<Selling>() {
            @Override
            public Selling doJobWithResult() {
                Selling selling = Selling.findById(sid);
                selling.syncFromAmazon();
                return selling;
            }

        }.now());
        renderJSON(new Ret());
    }

    /**
     * 下载 Selling 的 FBA_LABEL
     *
     * @param id sellingId
     */
    public static void sellingLabel(final String id) {
        File file = await(new Job<File>() {
            @Override
            public File doJobWithResult() {
                Selling selling = Selling.findById(id);
                byte[] bytes = selling.downloadFnSkuLabel();
                String fileName = String.format("%s.pdf", id);
                File file = new File(Constant.LABEL_PATH, fileName);
                file.delete(); // 删除原来的, 再写新的
                try {
                    FileUtils.writeByteArrayToFile(file, bytes);
                } catch (IOException e) {
                    // ignore
                }
                return file;
            }
        }.now());
        renderBinary(file, id + ".pdf");
    }

    /**
     * 加载 Selling 所拥有的全部 Feed
     *
     * @param sellingId String
     */
    public static void feeds(String sellingId) {
        renderArgs.put("feeds", Feed.find("fid=? ORDER BY createdAt DESC", sellingId).fetch());
        render("Feeds/_feed.html");
    }
}
