package controllers;

import com.alibaba.fastjson.JSONObject;
import controllers.api.SystemOperation;
import helper.*;
import models.ElcukRecord;
import models.embedded.AmazonProps;
import models.market.*;
import models.product.Category;
import models.product.Product;
import models.view.Ret;
import models.view.post.SellingAmzPost;
import models.view.post.SellingPost;
import models.whouse.Whouse;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.DateTime;
import play.data.validation.Error;
import play.data.validation.Validation;
import play.jobs.Job;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.With;
import play.utils.FastRuntimeException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 控制 Selling
 * User: Wyatt
 * Date: 12-1-7
 * Time: 上午11:41
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Sellings extends Controller {

    public static void selling(String id) {
        Selling s = Selling.findById(id);
        s.aps.arryParamSetUP(AmazonProps.T.STR_TO_ARRAY);
        F.T2<List<Selling>, List<String>> sellingAndSellingIds = Selling.sameFamilySellings(s.merchantSKU);
        renderArgs.put("sids", J.json(sellingAndSellingIds._2));
        renderArgs.put("feeds", s.feeds());

        List<ElcukRecord> logs =
                ElcukRecord.find("fid=? AND (action=? or action=? or action=?) ORDER BY createAt DESC", id,
                        "selling.image", "selling.sync.back", "selling.update").fetch(4);
        renderArgs.put("records", logs);
        SellingAmzPost p = new SellingAmzPost();
        render(s, p);
    }

    /**
     * 加载指定 Sid 下的所有的 SellingId
     *
     * @param sid
     */
    public static void sameSidSellings(String sid) {
        List<Selling> sellings = Selling.find("sellingId like '" + sid + "%'").fetch();
        List<String> sids = sellings.stream().map(s -> s.sellingId).collect(Collectors.toList());
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
                .put("rbn", Arrays.asList(s.aps.RBN))
                .build()));
    }

    /**
     * 添加 selling 页面
     */
    public static void blank() {
        render();
    }

    public static void create(String sku, String upc, String asin, String market, Account acc,
                              Integer pirateBeginHour, Integer pirateEndHour) {
        try {
            if(StringUtils.isBlank(sku)) Webs.error("SKU 必须存在");
            if(StringUtils.isBlank(upc)) Webs.error("UPC 必须存在");
            if(StringUtils.isBlank(asin)) Webs.error("ASIN 必须存在");
            if(StringUtils.isBlank(market)) Webs.error("Market 必须存在");
            Selling selling = Selling.blankSelling(sku, asin, upc, acc, M.val(market));
            selling.pirateBeginHour = pirateBeginHour;
            selling.pirateEndHour = pirateEndHour;
            selling.save();
            renderJSON(new Ret(true, selling.sellingId));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void imageUpload(final String sid, final String imgs) {
        if(StringUtils.isBlank(imgs)) renderJSON(new Ret("图片信息不能为空!"));
        Selling s = Selling.findById(sid);
        List<Error> errors = new ArrayList<>();
        try {
            s.uploadFeedAmazonImg(imgs, false, Secure.Security.connected().toLowerCase());
        } catch(Exception e) {
            errors.add(new Error("", Webs.e(e), new String[]{}));
        }
        if(errors.size() > 0) {
            renderJSON(new Ret(false, errors.toString()));
        } else {
            renderJSON(new Ret(true, "正在处理,请查看更新日志"));
        }
    }

    public static void update(Selling s) {
        if(!s.isPersistent()) renderJSON(new Ret("Selling(" + s.sellingId + ") 不存在!"));
        try {
            s.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
            s.save();
            renderJSON(new Ret(true, s.sellingId));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 将部分信息同步到 Amazon
     *
     * @param s
     * @param p
     */
    public static void partialUpdate(Selling s, SellingAmzPost p) {
        if(p == null) {
            renderJSON(new Ret(false, "请勾选Selling信息更新!"));
        }
        try {
            s.aps.arryParamSetUP(AmazonProps.T.ARRAY_TO_STR);
            s.partialUpdate(p);
            s.save();
            renderJSON(new Ret(true));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }


    @Check("sellings.delete")
    public static void destroy(String id) {
        try {
            Selling s = Selling.findById(id);
            s.remove();
            renderJSON(new Ret(true, "成功删除"));
        } catch(FastRuntimeException e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 从 Amazon 上将 Selling 信息同步回来
     */
    public static void syncAmazon(final String sid) {
        try {
            Selling selling = Selling.findById(sid);
            selling.syncAmazonInfoFromApi();
            Map<String, Object> map = new HashMap<>();
            map.put("jobName", "sellingInfoSyncJob");  //  任务ID
            map.put("args", GTs.newMap("sellingId", selling.sellingId).build());  //sellingId
            String message = JSONObject.toJSONString(map);
            AmazonSQS.sendMessage(message);
            renderJSON(new Ret());
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 下载 Selling 的 FBA_
     *
     * @param id sellingId
     * @deprecated 已转移到 ProcureUnits.fnSkuLable
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
            selling.recordingListingState(DateTime.now().toDate(), selling.state.name());
            renderJSON(new Ret(true, sellingId));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 修改 Selling 在系统内的状态
     */
    public static void changePirateHour(Selling s) {
        try {
            Selling selling = Selling.findById(s.sellingId);
            selling.pirateBeginHour = s.pirateBeginHour;
            selling.pirateEndHour = s.pirateEndHour;
            selling.save();
            renderJSON(new Ret(true, s.sellingId));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 批量导入 Selling
     */
    public static void bulkImport(File sellingFile) {
        List<String> lines = new ArrayList<>();
        StringBuilder msg = new StringBuilder();
        // 文件基本属性校验(是否存在、格式、标题行)
        try {
            if(sellingFile == null) Webs.error("文件为空!");
            String fileName = sellingFile.getName();
            if(!(fileName.substring(fileName.lastIndexOf(".") + 1)).equalsIgnoreCase("txt")) //文件类型校验
                Webs.error("不支持的文件格式! 请使用 TXT 文档.");
            lines = FileUtils.readLines(sellingFile);
            if(lines.size() == 0 || !(lines.get(0).contains("SKU\tUPC\tASIN\tMarket\tAccount")))
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
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void index(SellingPost p) {
        if(p == null) p = new SellingPost();
        List<String> products = Product.skus(true);
        String username = Login.currentUserName();
        List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                .collect(Collectors.toList());
        renderArgs.put("products", J.json(products));
        renderArgs.put("categoryList", categoryList);
        List<Selling> sellings = p.query();
        render(sellings, p);
    }

    public static void pirateIndex(SellingPost p) {
        if(p == null) p = new SellingPost();
        String username = Login.currentUserName();
        List<String> categoryList = Category.categories(username).stream().map(category -> category.categoryId)
                .collect(Collectors.toList());
        renderArgs.put("categoryList", categoryList);
        p.pirateState = Selling.PS.PIRATE;
        List<Selling> sellings = p.query();
        render(p, sellings);
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

    public static void showSellingList(String sku) {
        List<Selling> sellings = Selling.find("product.sku=?", sku).fetch();
        render("Sellings/_selling_list.html", sellings);
    }

    public static void batchDownSelling(String[] sellingIds) {
        try {
            for(String sellingId : sellingIds) {
                Selling selling = Selling.findById(sellingId);
                selling.state = Selling.S.DOWN;
                selling.save();
                //修改 Product 在系统内的状态
                Product.changeProductType(selling.merchantSKU);
                //存储 Listing 状态变更记录
                selling.recordingListingState(DateTime.now().toDate(), selling.state.name());
            }
            renderJSON(new Ret(true, Arrays.toString(sellingIds)));
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    public static void deleteImage(String sku, String fileName) {
        try {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("sku", sku));
            params.add(new BasicNameValuePair("pic_name", fileName));
            String message = HTTP.post("https://e.easyacc.com:8081/index.php?explorer/erpRemovePicApi", params);
            if(message.equals("true")) {
                renderJSON(new Ret(true));
            } else {
                renderJSON(new Ret(false, message));
            }
        } catch(Exception e) {
            renderJSON(new Ret(Webs.e(e)));
        }
    }

    /**
     * 调用 Rockend 来重新上架
     */
    public static void rePushFeedsToAmazon(String sellingId) {
        Selling s = Selling.findById(sellingId);
        notFoundIfNull(s, "未找到相关 Selling!");
        s.rePushFeedsToAmazon();
        Webs.errorToFlash(flash);
        if(!Validation.hasErrors()) flash.success("成功提交请求到 Rockend, 请等待 2~5 分钟后查看执行结果!");
        selling(sellingId);
    }

    public static void findSellingBySkuAndMarket(String sku, String market, Long id) {
        Whouse whouse = Whouse.findById(id);
        List<Selling> list = Selling.find("product.sku=? AND market=? AND state <>? AND account.id = ? ",
                sku, M.valueOf(market), Selling.S.DOWN, whouse.account.id).fetch();
        List<String> sids = new ArrayList<>();
        for(Selling s : list) sids.add(s.sellingId);
        renderJSON(J.json(sids));
    }
}
