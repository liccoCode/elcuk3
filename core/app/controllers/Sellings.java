package controllers;

import ext.LinkHelper;
import helper.Constant;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.embedded.AmazonProps;
import models.market.Listing;
import models.market.Selling;
import models.view.Ret;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;

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
@With({GlobalExceptionHandler.class, Secure.class, GzipFilter.class})
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

    public static void imageUpload(final Selling s, final String imgs) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ")" + "不存在!"));
        if(StringUtils.isBlank(s.aps.imageName) && StringUtils.isBlank(imgs)) renderJSON(new Ret("图片信息不能为空!"));
        // 10s 给上传时间
        await("10s", new F.Action0() {
            @Override
            public void invoke() {
                try {
                    s.uploadAmazonImg(imgs, false);
                } catch(Exception e) {
                    renderJSON(new Ret(Webs.E(e)));
                }
                renderJSON(new Ret(true));
            }
        });

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
        renderJSON(J.G(s));
    }

    /**
     * 从 Amazon 上将 Selling 信息同步回来
     */
    public static void syncAmazon(final String sid) {
        // play status 检查平均耗时 2.5s , 开放线程时间 3s 后回掉
        await("3s", new F.Action0() {
            @Override
            public void invoke() {
                Selling selling = Selling.findById(sid);
                selling.syncFromAmazon();
                renderJSON(new Ret());
            }
        });
    }

    /**
     * 下载 Selling 的 FBA_LABEL
     *
     * @param id sellingId
     */
    public static void sellingLabel(String id) {
        Selling selling = Selling.findById(id);
        byte[] bytes = selling.downloadFnSkuLabel();
        String fileName = String.format("%s.pdf", id);
        File file = new File(Constant.LABEL_PATH, fileName);
        file.delete(); // 删除原来的, 再写新的
        try {
            FileUtils.writeByteArrayToFile(file, bytes);
        } catch(IOException e) {
            // ignore
        }
        renderBinary(file, id + ".pdf");
    }

}
