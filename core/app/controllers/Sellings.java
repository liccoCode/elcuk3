package controllers;

import controllers.api.SystemOperation;
import helper.Constant;
import helper.GTs;
import helper.J;
import helper.Webs;
import models.embedded.AmazonProps;
import models.market.*;
import models.product.Family;
import models.product.Product;
import models.view.Ret;
import models.view.post.SellingAmzPost;
import models.view.post.SellingPost;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.jsoup.helper.Validate;
import play.jobs.Job;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import play.data.validation.Error;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
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
        SellingAmzPost p = new SellingAmzPost();
        render(s, p);
    }

    /**
     * 加载指定 Product 所属的 Family 下的所有的 SellingId
     *
     * @param msku
     */
    public static void sameFamilySellings(String msku) {
        List<Selling> sellings = Selling
                .find("listing.product.family=?", Product.findByMerchantSKU(msku).family).fetch();
        List<String> sids = new ArrayList<String>();
        for(Selling s : sellings) sids.add(s.sellingId);
        renderJSON(J.json(sids));
    }


    /**
     * 加载指定 Sid 下的所有的 SellingId
     *
     * @param sid
     */
    public static void sameSidSellings(String sid) {
        List<Selling> sellings = Selling
                .find("sellingId like '" + sid + "%'").fetch();
        List<String> sids = new ArrayList<String>();
        for(Selling s : sellings) sids.add(s.sellingId);
        renderJSON(J.json(sids));
    }

    /**
     * 加载 Techical, SearchTerms, ProductDesc 三块信息的 JSON 格式给前台
     */
    public static void tsp(String sid) {
        Selling s = Selling.findById(sid);// 利用 hibernate 二级缓存, Play 的 JavaBean 填充的查询语句含有 limit 语句
        if(s == null) {
            flash.error("找不到selling:" + sid);
            renderJSON(new Ret("找不到selling:" + sid));
        }
        s.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
        renderJSON(J.json(GTs.MapBuilder
                .map("t", s.aps.keyFeturess)
                .put("s", s.aps.searchTermss)
                .put("p", Arrays.asList(s.aps.productDesc))
                .put("man", Arrays.asList(s.aps.manufacturer))
                .put("brand", Arrays.asList(s.aps.brand))
                .put("price", Arrays.asList(s.aps.standerPrice.toString()))
                .put("type", Arrays.asList(s.aps.itemType))
                .put("title", Arrays.asList(s.aps.title))
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
            renderJSON(new Ret(true, selling.sellingId));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));
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
        } catch(FastRuntimeException e) {
            flash.error(e.getMessage());
            Sellings.selling(s.sellingId);
        }
    }


    public static void imageUpload(final String sid, final String imgs) {
        if(StringUtils.isBlank(imgs)) renderJSON(new Ret("图片信息不能为空!"));
        List<Error> errors = await(new Job<List<play.data.validation.Error>>() {
            @Override
            public List<Error> doJobWithResult() throws Exception {
                List<Error> errors = new ArrayList<Error>();
                Selling s = Selling.findById(sid);
                try {
                    s.uploadAmazonImg(imgs, false);
                } catch(Exception e) {
                    errors.add(new Error("", Webs.E(e), new String[]{}));
                }
                return errors;
            }
        }.now());
        if(errors.size() > 0) {
            renderJSON(new Ret(false, errors.toString()));
        } else {
            renderJSON(new Ret(true));
        }
    }


    /*Play 在绑定内部的 Model 的时候与 JPA 想法不一致, TODO 弄清理 Play 怎么处理 Model 的*/

    public static void update(Selling s) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ") 不存在!"));
        try {
            s.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
            s.save();
            renderJSON(new Ret(true, s.sellingId));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void deploy(String id) {
        //10SMI9300-2200S|A_UK|1
        Selling s = Selling.findById(id);
        try {
            Feed feed = s.deploy();
            renderJSON(feed);
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 将部分信息同步到AMAZON
     *
     * @param s
     * @param p
     */
    public static void amazon_update(Selling s, SellingAmzPost p) {
        if(p == null) {
            renderJSON(new Ret(false, "请勾选Selling信息更新!"));
        }
        try {
            s.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
            s.syncAndUpdateAmazon(p);
            s.save();
            renderJSON(new Ret(true));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }


    @Check("sellings.delete")
    public static void destroy(String id) {
        try {
            Selling s = Selling.findById(id);
            s.remove();
            renderJSON(new Ret(true, "成功删除"));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 从 Amazon 上将 Selling 信息同步回来
     */
    public static void syncAmazon(final String sid) {
        try {
            Selling selling = Selling.findById(sid);
            selling.syncFromAmazon();
            renderJSON(new Ret());
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.E(e)));
        }
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
                } catch(IOException e) {
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

    /**
     * 修改 Selling 在系统内的状态
     */
    public static void changeSellingType(String sellingId, boolean flag) {
        try {
            Selling selling = Selling.findById(sellingId);
            if(flag) {
                selling.state = Selling.S.SELLING;
            } else {
                selling.state = Selling.S.DOWN;
            }
            selling.save();
            //修改 Product 在系统内的状态
            Product.changeProductType(selling.merchantSKU);
            //存储 Listing 状态变更记录
            selling.listing.recordingListingState(DateTime.now().toDate());
            renderJSON(new Ret(true, sellingId));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    /**
     * 批量导入 Selling
     */
    public static void bulkImport(File sellingFile) {
        List<String> lines = new ArrayList<String>();
        StringBuffer msg = new StringBuffer();
        // 文件基本属性校验(是否存在、格式、标题行)
        try {
            if(sellingFile == null) Webs.error("文件为空!");
            String fileName = sellingFile.getName();
            if(!(fileName.substring(fileName.lastIndexOf(".") + 1)).equalsIgnoreCase("txt")) //文件类型校验
                Webs.error("不支持的文件格式! 请使用 TXT 文档.");
            lines = FileUtils.readLines(sellingFile);
            if(lines.size() == 0 || !(lines.get(0).toString().contains("SKU\tUPC\tASIN\tMarket\tAccount")))
                Webs.error("文件校验失败, 内容为空或标题行不存在!");
        } catch(Exception e) {
            renderText(e.getMessage());
        }

        //开始解析 Selling
        lines.remove(0); // 删除第一行的标题
        for(int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            try {
                Selling.createWithArgs(line);
            } catch(FastRuntimeException e) {
                msg.append(String.format("ERROR:[%s], 位置: [第 %s 行], 内容: [%s] ", e.getMessage(), i + 1, line));
            }
        }
        if(StringUtils.isNotBlank(msg.toString())) {
            renderText(String.format("解析过程中出现错误: %s", msg.toString()));
        }
        renderText("恭喜, 文件解析成功!请刷新 Listing 首页查看 Selling 新数据.");
    }

    /**
     * 下载 Selling 导入模板
     */
    public static void sellingTemplate() {
        File template = new File(String.format("%s/%s", System.getProperty("application.path"),
                "app/views/Sellings/uploadTemplate.xls"));
        renderBinary(template);
    }

    /**
     * 修改 Selling 的生命周期,同时还要更新缓存
     */
    public static void changeSellingCycle(String sellingId, Selling.SC cycle) {
        try {
            Selling selling = Selling.findById(sellingId);
            if(selling == null || !selling.isPersistent()) throw new FastRuntimeException("Selling 不合法.");
            selling.sellingCycle(cycle);
            renderJSON(new Ret(true, sellingId));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

    public static void index(SellingPost p) {
        if(p == null) p = new SellingPost();
        List<String> products = Product.skus(true);
        renderArgs.put("products", J.json(products));
        List<Selling> sellings = p.query();
        render(sellings, p);
    }

    public static void createSelling(Selling s) {

        render(s);
    }

    public static void saleAmazon(String id) {
        Product product = Product.findByMerchantSKU(id);
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(product.sku);
        Selling s = new Selling();
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
        renderArgs.put("accs", Account.openedSaleAcc());
        render("Sellings/_saleAmazon.html", product, s);
    }

    public static void batchDownSelling(String[] sellingIds) {
        try {
            for(int i = 0; i < sellingIds.length; i++) {
                Selling selling = Selling.findById(sellingIds[i]);
                selling.state = Selling.S.DOWN;
                selling.save();
                //修改 Product 在系统内的状态
                Product.changeProductType(selling.merchantSKU);
                //存储 Listing 状态变更记录
                selling.listing.recordingListingState(DateTime.now().toDate());
            }
            renderJSON(new Ret(true, sellingIds.toString()));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.E(e)));
        }
    }

}
