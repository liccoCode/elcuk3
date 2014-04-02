package jobs.categoryInfo;

import com.sun.javafx.tools.packager.Log;
import helper.DBUtils;
import jobs.driver.BaseJob;
import models.product.Category;
import models.product.Product;
import models.view.dto.CategoryInfoDTO;
import org.apache.commons.lang.NumberUtils;
import org.joda.time.DateTime;
import play.Logger;
import play.db.helper.SqlSelect;
import query.ProductQuery;
import services.MetricProfitService;

import java.util.*;

/**
 * Category 信息界面数据准备
 * <p/>
 * User: mac
 * Date: 14-4-2
 * Time: PM2:56
 */
public class CategoryInfoFetchJob extends BaseJob {
    public static final String CategoryInfo_Cache = "categoryinfo";

    @SuppressWarnings("unchecked")
    @Override
    public void doit() {
        long begin = System.currentTimeMillis();
        categoryinfo();
        Logger.info("CategoryInfoFetchJob calculate.... [%sms]", System.currentTimeMillis() - begin);
    }

    /**
     * Category信息数据计算
     */
    public void categoryinfo() {
        List<CategoryInfoDTO> dtoList = new ArrayList<CategoryInfoDTO>();
        //所有的 Category
        List<Category> categorys = Category.findAll();
        DateTime now = new DateTime().now();
        MetricProfitService me = new MetricProfitService(now.toDate(), now.toDate(), null, null, null);

        for(Category category : categorys) {

            for(Product product : category.products) {
                //计算单个sku：
                CategoryInfoDTO dto = new CategoryInfoDTO(product.sku);
                //1、总销量(从ERP上线到今日)
                dto.total = total(product.sku);
                //2、本月销量(月初到月底)
                dto.day30 = day30(product.sku);
                //3、利润(今年)

                //4、利润率(今年)

                //5、上周销售额(上上周六到上周五)

                //6、上上周销售额(往上同期)

                //7、上周销量(上上周六到上周五)

                //8、上上周销量(往上同期)

            }
        }

    }

    /**
     * sku 总销量
     *
     * @return
     */
    public long total(String sku) {
        //从系统内第一笔订单产生的日期到最后一笔订单产生的日期
        SqlSelect sql = new SqlSelect().select("max(createDate) as max, min(createDate) as min").from("OrderItem");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        for(Map<String, Object> row : rows) {
            DateTime begin = DateTime.parse(row.get("max").toString());
            DateTime end = DateTime.parse(row.get("min").toString());
            MetricProfitService me = new MetricProfitService(begin.toDate(), end.toDate(), null, sku, null);
            return NumberUtils.createLong(me.esSaleQty().toString());
        }
        return (long) 0;
    }

    /**
     * 本月销量
     *
     * @param sku
     * @return
     */
    public int day30(String sku) {
        DateTime time = new DateTime().now();
        MetricProfitService me = new MetricProfitService(getFirstDayOfMonth(), getLastDayOfMonth(), null, sku, null);
        return NumberUtils.createInteger(me.esSaleQty().toString());
    }

    public float profit(String sku) {

    }


    /**
     * 获取本月第一天
     *
     * @return
     */
    public Date getFirstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        return calendar.getTime();
    }

    /**
     * 获取本月最后一天
     *
     * @return
     */
    public Date getLastDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);//设置为当前月1号
        calendar.add(Calendar.MONTH, 1);//加一个月变成下一月的1号
        calendar.add(Calendar.DATE, -1);//减去一天，变成当月最后一天
        return calendar.getTime();
    }
}
