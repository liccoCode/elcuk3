package models.view.dto;

import com.alibaba.fastjson.JSONObject;
import helper.Dates;
import helper.Webs;
import models.market.M;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.*;

/**
 * SKU 月度日均销量报表数据对象
 * Created by IntelliJ IDEA.
 * User: mac
 * Date: 15-4-23
 * Time: PM2:54
 */
public class DailySalesReportsDTO implements Serializable {
    private static final long serialVersionUID = -7307350998886959041L;

    public String category;
    public String sku;
    public String market;

    /**
     * 准确记录每个月的总销量
     * <p>
     * PS: 临时使用, 计算完日销量和汇总日销量后需要改为 NULL
     */
    public Map<Date, Integer> saleMap = new HashMap<>();

    /**
     * key: 月份
     * value: 日均销量
     */
    public HashMap<Integer, Float> sales = new HashMap<>();

    public DailySalesReportsDTO() {
    }

    public DailySalesReportsDTO(String category, String sku, String market) {
        this.category = category;
        this.sku = sku;
        this.market = market;
    }

    /**
     * 计算每日销量
     *
     * @return
     */
    public DailySalesReportsDTO processDailySales() {
        if(this.sales.isEmpty()) {
            for(Date date : this.saleMap.keySet()) {
                this.sales.put(new DateTime(date).getMonthOfYear(),
                        Webs.scalePointUp(0, (float) this.saleMap.get(date) / Dates.getDays(date)));
            }
        }
        this.saleMap = null;//计算完成后清除掉 saleMap 对象, 避免序列化的时候存储了 sales 和 saleMap 两分数据
        return this;
    }

    /**
     * 生成计算 SKU 的汇总对象
     */
    public static DailySalesReportsDTO buildSumDTO(List<DailySalesReportsDTO> dtos) {
        if(dtos.isEmpty()) return null;

        DailySalesReportsDTO sumDTO = new DailySalesReportsDTO();
        for(DailySalesReportsDTO dto : dtos) {
            for(Date date : dto.saleMap.keySet()) {
                if(sumDTO.saleMap.containsKey(date)) {
                    sumDTO.saleMap.put(date, sumDTO.saleMap.get(date) + dto.saleMap.get(date));
                } else {
                    sumDTO.saleMap.put(date, dto.saleMap.get(date));
                }
            }
        }
        return sumDTO;
    }

    /**
     * 1. 构造 DailySalesReportsDTO 对象(单个 SKU)
     * 2. 填充 saleMap 属性
     */

    public static DailySalesReportsDTO buildFromJSONObject(JSONObject skuBucket, M market) {
        String sku = skuBucket.getString("key");
        if(StringUtils.isNotBlank(sku)) {
            DailySalesReportsDTO dto = new DailySalesReportsDTO(sku.substring(0, 2), sku, market.name());
            Optional.ofNullable(skuBucket.getJSONObject("monthly_avg"))
                    .map(monthlyAvg -> monthlyAvg.getJSONArray("buckets"))
                    .ifPresent(buckets -> buckets.stream()
                            .map(bucket -> (JSONObject) bucket)
                            .forEach(bucket -> dto.saleMap.put(
                                    Dates.date2JDate(bucket.getDate("key")),
                                    bucket.getJSONObject("sum_sales").getIntValue("value")
                            )));
            return dto;
        }
        return null;
    }
}
