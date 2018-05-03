package controllers;


import com.alibaba.fastjson.JSON;
import controllers.api.SystemOperation;
import helper.Caches;
import helper.Dates;
import helper.J;
import jobs.analyze.ProfitInventorySearch;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.ElcukRecord;
import models.User;
import models.product.Product;
import models.view.Ret;
import models.view.dto.AnalyzeDTO;
import models.view.post.ProfitPost;
import models.view.post.SellingRecordPost;
import models.view.report.Profit;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 利润计算
 * User: cary
 * Date: 3/10/14
 * Time: 2:29 PM
 */
@With({GlobalExceptionHandler.class, Secure.class, SystemOperation.class})
public class Profits extends Controller {

    @Before(only = {"index"})
    public static void setUpIndexPage() {
        User user = User.findById(Login.current().id);
        renderArgs.put("categories", user.categories);
        F.T2<List<String>, List<String>> skusToJson = Product.fetchSkusJson();
        renderArgs.put("skus", J.json(skusToJson._2));
    }

    @Check("profits.index")
    public static void index(ProfitPost p) {
        if(p != null)
            new ElcukRecord("查询利润分析", J.json(p), Login.current().username).save();
        List<Profit> profits = Collections.emptyList();
        if(p == null) {
            p = new ProfitPost();
        } else {
            profits = p.fetch();
        }
        List<ElcukRecord> records = ElcukRecord.records(Collections.singletonList("查询利润分析"), 50);
        render(profits, p, records);
    }


    public static void inventory(ProfitPost p) {
        List<Profit> profits = new ArrayList<>();
        if(p == null) {
            p = new ProfitPost();
            render(profits, p);
        } else {
            p.end = Dates.night(p.end);
            String cacke_key = SellingSaleAnalyzeJob.AnalyzeDTO_SID_CACHE;
            // 这个地方有缓存, 但还是需要一个全局锁, 控制并发, 如果需要写缓存则锁住

            List<AnalyzeDTO> dtos = null;
            String cache_str = Caches.get(cacke_key);
            if(!StringUtils.isBlank(cache_str)) {
                dtos = JSON.parseArray(cache_str, AnalyzeDTO.class);
            }
            // 用于提示后台正在运行计算
            if(StringUtils.isBlank(cache_str) || dtos == null) {
                renderJSON(new Ret("Analyze后台事务正在执行中,请稍候..."));
            }
            new ProfitInventorySearch(p).now();
            renderJSON(new Ret("正在计算库存成本!"));
        }
    }

    @Check("profits.index")
    public static void indexv3(SellingRecordPost p) {
        List<Map<String, Object>> profits = Collections.emptyList();
        if(p == null) {
            p = new SellingRecordPost();
            p.from = DateTime.now().minusMonths(1).toDate();
        } else {
            profits = p.queryProfits();
        }
        render(profits, p);
    }

}
