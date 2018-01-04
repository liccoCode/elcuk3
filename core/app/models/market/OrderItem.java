package models.market;

import com.alibaba.fastjson.JSONObject;
import helper.*;
import helper.Currency;
import models.product.Category;
import models.product.Product;
import models.view.dto.DailySalesReportsDTO;
import models.view.highchart.AbstractSeries;
import models.view.highchart.HighChart;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.annotations.DynamicUpdate;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.libs.F;
import query.OrderItemESQuery;
import query.ProductQuery;

import javax.persistence.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单的具体订单项
 * User: wyattpan
 * Date: 1/6/12
 * Time: 10:42 AM
 */
@Entity
@DynamicUpdate
public class OrderItem extends GenericModel {

    private static final long serialVersionUID = -6723970885712000807L;
    /**
     * 为保持更新的时候的唯一性, 所以将起 Id 设置为 orderId_sku
     */
    @Id
    public String id;

    @ManyToOne(fetch = FetchType.LAZY)
    public Orderr order;

    @OneToOne(fetch = FetchType.LAZY)
    public Selling selling;

    @OneToOne(fetch = FetchType.LAZY)
    public Product product;

    /**
     * 冗余字段, 产品名称
     */
    public String listingName;

    /**
     * 冗余字段, 订单项产生的时间
     */
    public Date createDate;

    /**
     * 这个商品的销售
     */
    public Float price;

    /**
     * 记录这个 OrderItem 记录的货币单位[price/discountPrice/..]
     */
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(10) DEFAULT ''")
    public Currency currency;

    /**
     * 统一为 USD 的金额
     */
    @Column(columnDefinition = "float DEFAULT 0")
    public Float usdCost;

    /**
     * 如果做促销的话, 具体折扣的价格
     */
    public Float discountPrice;

    /**
     * 促销的名称
     */
    public String promotionIDs;

    /**
     * 这个商品所承担的运输费用
     */
    public Float shippingPrice;

    /**
     * GiftWrap 费用
     */
    public Float giftWrap;

    /**
     * 对不同网站不同情况的 Fee 所产生费用的一个总计(经过 -/+ 计算后的) TODO 暂时没有启用
     */
    public Float feesAmaount;


    /**
     * 商品的数量; 一般情况下这个数量就为 1, 一个订单有多少个 Item 则需要有多少个 Item 项; 但为了避免批发形式的销售, 所以需要拥有一个 quantity 将
     * 批发形式的销售作为一个拥有数量的 OrderItem 来对待;
     */
    public Integer quantity;

    /**
     * 一个中性的记录消息的地方
     */
    public String memo = "";

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) DEFAULT 'AMAZON_UK'")
    public M market;


    public void updateAttr(OrderItem noi) {
        if(noi.createDate != null) this.createDate = noi.createDate;
        if(noi.discountPrice != null) this.discountPrice = noi.discountPrice;
        if(noi.feesAmaount != null) this.feesAmaount = noi.feesAmaount;
        if(noi.memo != null) this.memo = noi.memo;
        if(noi.price != null) this.price = noi.price;
        if(noi.listingName != null) this.listingName = noi.listingName;
        if(noi.quantity != null) this.quantity = noi.quantity;
        if(noi.shippingPrice != null) this.shippingPrice = noi.shippingPrice;
        if(noi.product != null) this.product = noi.product;
        if(noi.selling != null) this.selling = noi.selling;
        if(noi.currency != null && this.currency != this.currency) this.currency = noi.currency;
        if(noi.usdCost != null) this.usdCost = noi.usdCost;
        this.save();
    }

    public void calUsdCose() {
        if(this.currency != null && this.price != null) {
            this.usdCost = this.currency
                    .toUSD(this.price - (this.discountPrice == null ? 0 : this.discountPrice));
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        if(!super.equals(o)) return false;

        OrderItem orderItem = (OrderItem) o;

        if(!id.equals(orderItem.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + id.hashCode();
        return result;
    }

    /**
     * <pre>
     * 通过 OrderItem 计算指定的 skuOrMsku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param val 需要查询的 all, categoryId, sku, sid
     * @param to  @return {series_size, days, series_n}
     */
    @Cached("2h")
    public static HighChart ajaxHighChartUnitOrder(String val, String type, Date from, Date to) {
        String cache_key = Caches.Q.cacheKey("unit", val, type, from, to);
        HighChart lines = Cache.get(cache_key, HighChart.class);
        if(lines != null) return lines;
        synchronized(cache_key.intern()) {
            lines = Cache.get(cache_key, HighChart.class);
            if(lines != null) return lines;

            // 做内部参数的容错
            Date _from = Dates.morning(from);
            Date _to = Dates.night(to);

            HighChart highChart = new HighChart();
            OrderItemESQuery esQuery = new OrderItemESQuery();

            for(M market : Promises.MARKETS) {
                highChart.series(esQuery.salesFade(type, val, market, _from, _to));
            }
            highChart.series(highChart.sumSeries("销量"));
            if(type.equals("sid") && !StringUtils.isBlank(val) && !val.equals("all") && val.length() >= 6) {
                for(int i = 0; i < highChart.series.size(); i++) {
                    AbstractSeries serie = highChart.series.get(i);
                    if(!serie.name.contains("汇总")) {
                        serie.visible = false;
                        highChart.series.set(i, serie);
                    }
                }
            }

            Cache.add(cache_key, highChart, "2h");
        }
        return Cache.get(cache_key, HighChart.class);
    }


    public static HighChart ajaxHighChartMovinAvg(final String val, final String type, M m, Date from,
                                                  Date to) {
        String cacked_key = Caches.Q.cacheKey("moving_avg", m, val, type, from, to);
        HighChart lines = Cache.get(cacked_key, HighChart.class);
        if(lines != null) return lines;
        synchronized(cacked_key.intern()) {
            lines = Cache.get(cacked_key, HighChart.class);
            if(lines != null) return lines;

            // 做内部参数的容错
            final Date finalFrom = Dates.morning(from);
            final Date finalTo = Dates.night(to);

            HighChart highChart = new HighChart();
            OrderItemESQuery esQuery = new OrderItemESQuery();
            if(m == null) {
                HighChart tmphighChart = new HighChart();
                for(M market : Promises.MARKETS) {
                    tmphighChart.series(esQuery.movingAvgFade(type, val, market, finalFrom, finalTo));
                }
                highChart.series(tmphighChart.sumSeries("滑动平均"));
            } else {
                highChart.series(esQuery.movingAvgFade(type, val, m, finalFrom, finalTo));
            }
            Cache.add(cacked_key, highChart, "2h");
        }
        return Cache.get(cacked_key, HighChart.class);
    }

    /**
     * <pre>
     * 通过 OrderItem 计算指定的 skuOrMsku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param val 需要查询的 all, categoryId, sku, sid
     * @param to  @return {series_size, days, series_n}
     */
    @Cached("2h")
    public static HighChart ajaxSkusUnitOrder(final String val, final String type, Date from, Date to) {
        String cacked_key = Caches.Q.cacheKey("unit", val, type, from, to);
        HighChart lines = Cache.get(cacked_key, HighChart.class);
        if(lines != null) return lines;
        synchronized(cacked_key.intern()) {
            lines = Cache.get(cacked_key, HighChart.class);
            if(lines != null) return lines;

            // 做内部参数的容错
            final Date finalFrom = Dates.morning(from);
            final Date finalTo = Dates.night(to);

            final HighChart highChart = new HighChart();
            final OrderItemESQuery esQuery = new OrderItemESQuery();


            HighChart tmphighChart = new HighChart();

            Promises.forkJoin(new Promises.Callback<Object>() {
                @Override
                public Object doJobWithResult(Object param) {
                    highChart.series(esQuery.skusSearch("sku", "\"" + val + "\"", (M) param, finalFrom, finalTo, false));
                    return null;
                }

                @Override
                public String id() {
                    return "OrderItem.ajaxSkusUnitOrder(ES)";
                }
            });

            for(M market : Promises.MARKETS) {
                tmphighChart.series(esQuery.skusMoveingAve("sku", val, market, finalFrom, finalTo, false));
            }


            highChart.series(highChart.sumSeries("销量"));
            for(int i = 0; i < highChart.series.size(); i++) {
                AbstractSeries serie = highChart.series.get(i);
                if(!serie.name.contains("汇总")) {
                    serie.visible = false;
                    highChart.series.set(i, serie);
                }
            }

            highChart.series(tmphighChart.sumSeries("滑动平均"));
            Cache.add(cacked_key, highChart, "2h");
        }
        return Cache.get(cacked_key, HighChart.class);
    }


    /**
     * <pre>
     * 通过 OrderItem 计算指定的 skuOrMsku 在一个时间段内的销量情况, 并且返回的 Map 组装成 HightChart 使用的格式;
     * HightChart 的使用 http://jsfiddle.net/kSkYN/6937/
     * </pre>
     *
     * @param val 需要查询的 all, categoryId, sku, sid
     * @param to  @return {series_size, days, series_n}
     */
    @Cached("2h")
    public static HighChart ajaxSkusMarketUnitOrder(final String val, final String market, final String type,
                                                    Date from, Date to, int ismoveing) {
        String cacked_key = Caches.Q.cacheKey("unit", val, market, ismoveing, type, from, to);
        HighChart lines = Cache.get(cacked_key, HighChart.class);
        if(lines != null) return lines;
        synchronized(cacked_key.intern()) {
            lines = Cache.get(cacked_key, HighChart.class);
            if(lines != null) return lines;

            // 做内部参数的容错
            final Date finalFrom = Dates.morning(from);
            final Date finalTo = Dates.night(to);

            final HighChart highChart = new HighChart();
            final OrderItemESQuery esQuery = new OrderItemESQuery();
            String[] skus = val.replace("\"", "").split(",");

            if(market.equals("total")) {
                for(int i = 0; i < skus.length; i++) {
                    HighChart tmphighChart = new HighChart();
                    String sku = skus[i];
                    if(StringUtils.isNotBlank(skus[i])) {
                        if(ismoveing != 2) {
                            for(M m : Promises.MARKETS) {
                                tmphighChart.series(esQuery.skusSearch("sku", sku, m, finalFrom, finalTo, true));
                            }
                            highChart.series(tmphighChart.sumSeries(sku + "销量"));
                        } else {
                            for(M m : Promises.MARKETS) {
                                tmphighChart.series(esQuery.skusMoveingAve("sku", sku, m, finalFrom, finalTo, true));
                            }
                            highChart.series(tmphighChart.sumSeries(sku + "滑动平均"));
                        }
                    }
                }

            } else {
                final M m = M.val(market);

                for(int i = 0; i < skus.length; i++) {
                    if(StringUtils.isNotBlank(skus[i])) {
                        if(ismoveing != 2) {
                            highChart.series(esQuery.skusSearch("sku", skus[i], m, finalFrom, finalTo, true));
                        } else
                            highChart.series(esQuery.skusMoveingAve("sku", skus[i], m, finalFrom, finalTo, true));
                    }
                }
            }

            Cache.add(cacked_key, highChart, "2h");
        }
        return Cache.get(cacked_key, HighChart.class);
    }

    /**
     * 查询传入的 SKU 的销量信息
     */
    public static List<F.T4<String, Long, Long, Double>> querySalesBySkus(Date from, Date to, String val) {
        List<F.T4<String, Long, Long, Double>> sales = new ArrayList<>();

        List<String> selectedSkus = Arrays.asList(val.replace("\"", "").split(","));
        List<String> categories = new ProductQuery().loadCategoriesBySkus(selectedSkus);
        OrderItemESQuery service = new OrderItemESQuery();

        for(int i = 0; i < selectedSkus.size(); i++) {
            selectedSkus.set(i, StringUtils.join(selectedSkus.get(i).split("-"), "").toLowerCase());
        }

        //选择的 SKU 的销量汇总
        JSONObject skusResult = service.skuSales(from, to, selectedSkus, "sku");
        //Category 的销量汇总
        JSONObject catgoriesResult = service.skuSales(from, to, categories, "category_id");

        for(M m : M.values()) {
            if(m == M.EBAY_UK) continue;
            //SKU
            JSONObject marketResult = skusResult.getJSONObject(m.name());
            Long skuSales = marketResult.getJSONObject("sum_sales").getLongValue("value");
            //Category
            JSONObject categoryResult = catgoriesResult.getJSONObject(m.name());
            Long categorySales = categoryResult.getJSONObject("sum_sales").getLongValue("value");
            Float rate = categorySales == 0 ? 0 : ((float) skuSales / (float) categorySales);
            sales.add(new F.T4<>(m.name(), skuSales, categorySales,
                    Webs.scale2Double(rate * 100))
            );
        }

        //最后汇总 ALL 数据
        Long sumSkuSales = 0L;
        Long sumCategorySales = 0L;
        for(F.T4<String, Long, Long, Double> item : sales) {
            sumSkuSales += item._2;
            sumCategorySales += item._3;
        }
        Float sumRate = sumCategorySales == 0 ? 0 : ((float) sumSkuSales / (float) sumCategorySales);
        sales.add(0, new F.T4<>("ALL", sumSkuSales, sumCategorySales,
                Webs.scale2Double(sumRate * 100))
        );
        return sales;
    }

    public static void skuMonthlyDailySales(Date from, Date to, M market, String category,
                                            String val) {
        String cacheKey = Caches.Q.cacheKey("SkuMonthlyDailySales", from, to, category, market, val);
        String runningKey = String.format("%s_running", cacheKey);
        if(StringUtils.isNotBlank(Cache.get(runningKey, String.class))) return;

        if(Cache.get(cacheKey, List.class) != null) return;
        synchronized(cacheKey.intern()) {
            if(Cache.get(cacheKey, List.class) != null) return;

            try {
                Cache.add(runningKey, runningKey);
                List<String> selectedSkus = new ArrayList<>(Arrays.asList(val.replace("\"", "").split(",")));
                if(StringUtils.isNotBlank(category)) {
                    selectedSkus.addAll(Category.getSKUs(category));
                }
                List<M> markets = Optional.ofNullable(market)
                        .map(Collections::singletonList)
                        .orElse(Arrays.asList(M.amazonVals()));

                OrderItemESQuery service = new OrderItemESQuery();
                JSONObject esResult = service.skusMonthlyDailySale(from, to, selectedSkus, markets);

                List<DailySalesReportsDTO> dtos = new ArrayList<>();
                for(M m : markets) {
                    //aggregations.aggs_filters.AMAZON_UK.skus
                    Optional.ofNullable(J.dig(esResult, String.format("aggregations.aggs_filters.%s.skus", m.name())))
                            .map(skus -> skus.getJSONArray("buckets"))
                            .ifPresent(buckets -> buckets.stream()
                                    .map(bucket -> (JSONObject) bucket)
                                    .forEach(bucket -> dtos.add(DailySalesReportsDTO.buildFromJSONObject(bucket, m)))
                            );
                }
                for(String sku : selectedSkus) {
                    if(StringUtils.isBlank(sku)) continue;
                    List<DailySalesReportsDTO> skuDtos = dtos.stream()
                            .filter(dto -> dto != null && StringUtils.equalsIgnoreCase(dto.sku, ES.parseEsString(sku)))
                            .collect(Collectors.toList());
                    skuDtos.forEach(dto -> dto.sku = sku); //将 ES 化的 SKU 替换成正常的 SKU(70apipgpuenp -> 70APIP-GPUENP)
                    dtos.add(DailySalesReportsDTO.buildSumDTO(skuDtos, sku));//把计算好的 sku 汇总对象加入到 dtos 结果集
                }

                List<DailySalesReportsDTO> finalDtos = dtos.stream()
                        .filter(Objects::nonNull)
                        //计算日平均销量
                        .map(DailySalesReportsDTO::processDailySales)
                        //将相同的 SKU 的放到一起, 便于前端输出查看
                        .sorted((dto1, dto2) -> dto1.sku.compareTo(dto2.sku))
                        .collect(Collectors.toList());
                Cache.add(cacheKey, finalDtos, "4h");
                Cache.delete(runningKey);
            } finally {
                Cache.delete(runningKey);
            }
        }
    }


    //使用POI创建excel工作簿
    public static File createWorkBook(List<DailySalesReportsDTO> dtos, List<Integer> months,
                                      String cacheKey) throws IOException {
        //创建excel工作簿
        Workbook wb = new HSSFWorkbook();
        //创建第一个sheet（页），命名为 new sheet
        Sheet sheet = wb.createSheet("new sheet");
        Row row = sheet.createRow((short) 0);
        row.createCell(1).setCellValue("Category");
        row.createCell(2).setCellValue("SKU");
        row.createCell(3).setCellValue("Market");
        int cell = 3;
        for(int m = 0; m < months.size(); m++) {
            cell++;
            int month = months.get(m);
            row.createCell(cell).setCellValue(month + "月份");
        }

        for(int i = 0; i < dtos.size(); i++) {
            DailySalesReportsDTO dto = dtos.get(i);
            // 创建一行，在页sheet上
            int count = i + 1;
            row = sheet.createRow((short) count);
            // Or do it on one line.
            row.createCell(1).setCellValue(dto.category);
            row.createCell(2).setCellValue(dto.sku);
            row.createCell(3).setCellValue(dto.market);
            cell = 3;
            for(int m = 0; m < months.size(); m++) {
                cell++;
                int month = months.get(m);
                if(dto.sales != null && dto.sales.get(month) != null) {
                    row.createCell(cell).setCellValue(dto.sales.get(month));
                }
            }
        }

        //创建一个文件 命名为workbook.xls
        String path = "/root/elcuk2-report/TEMP/" + cacheKey + "sku.xls";
        FileOutputStream fileOut = new FileOutputStream(path);
        // 把上面创建的工作簿输出到文件中
        wb.write(fileOut);
        //关闭输出流
        fileOut.close();

        File file = new File(path);
        file.deleteOnExit();
        return file;
    }
}
