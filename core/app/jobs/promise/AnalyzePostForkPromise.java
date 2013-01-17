package jobs.promise;

import models.market.M;
import org.joda.time.DateTime;
import play.jobs.Job;
import play.libs.F;
import query.OrderItemQuery;
import query.vo.AnalyzeVO;

import java.util.List;

/**
 * 用来 Fork 计算加载不同市场的 AnalyzesVO 数据
 * User: wyatt
 * Date: 1/17/13
 * Time: 12:10 PM
 */
public class AnalyzePostForkPromise extends Job<List<AnalyzeVO>> {
    private DateTime from;
    private DateTime to;
    private M market;

    public AnalyzePostForkPromise(DateTime from, DateTime to, M market) {
        this.from = from;
        this.to = to;
        this.market = market;
    }

    @Override
    public List<AnalyzeVO> doJobWithResult() throws Exception {
        F.T2<DateTime, DateTime> fixedDateRange = market.withTimeZone(from.toDate(), to.toDate());
        return new OrderItemQuery().analyzeVos(
                fixedDateRange._1.toDate(), fixedDateRange._2.toDate(), market);
    }
}
