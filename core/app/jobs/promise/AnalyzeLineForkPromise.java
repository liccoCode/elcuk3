package jobs.promise;

import models.market.M;
import org.joda.time.DateTime;
import play.jobs.Job;
import play.libs.F;
import query.OrderItemQuery;
import query.vo.AnalyzeVO;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/17/13
 * Time: 6:03 PM
 */
public class AnalyzeLineForkPromise extends Job<List<AnalyzeVO>> {
    private DateTime from;
    private DateTime to;
    private M market;
    public F.T2<String, String> typeAndMSKU;
    private Long accId;

    public AnalyzeLineForkPromise(DateTime from, DateTime to, M market,
                                  F.T2<String, String> typeAndMSKU) {
        this.from = from;
        this.to = to;
        this.market = market;
        this.typeAndMSKU = typeAndMSKU;
    }

    public AnalyzeLineForkPromise(DateTime from, DateTime to, M market,
                                  F.T2<String, String> typeAndMSKU, Long accId) {
        this(from, to, market, typeAndMSKU);
        this.accId = accId;
    }

    @Override
    public List<AnalyzeVO> doJobWithResult() throws Exception {
        F.T2<DateTime, DateTime> dateRange = market.withTimeZone(from.toDate(), to.toDate());
        /**
         * 1. 加载所有的 SKU 的 OrderItem 无论哪个市场
         * 2. 加载指定 SKU 的所有 OrderItem 数据
         * 3. 加载某一个 MSku 的所有 OrderItem
         *  - 如果带有 Account 则增加 account 过滤
         */
        if("all".equalsIgnoreCase(typeAndMSKU._1))
            return new OrderItemQuery().allNormalSaleOrderItem(
                    dateRange._1.toDate(),
                    dateRange._2.toDate(),
                    market
            );

        if("sku".equalsIgnoreCase(typeAndMSKU._2))
            return new OrderItemQuery().skuNormalSaleOrderItem(
                    typeAndMSKU._1,
                    dateRange._1.toDate(),
                    dateRange._2.toDate(),
                    market
            );

        return new OrderItemQuery().mskuWithAccountNormalSaleOrderItem(
                typeAndMSKU._1,
                this.accId,
                dateRange._1.toDate(),
                dateRange._2.toDate(),
                market
        );
    }
}
