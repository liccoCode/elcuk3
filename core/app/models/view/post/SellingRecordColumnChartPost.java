package models.view.post;

import models.view.highchart.HighChart;
import org.joda.time.DateTime;
import play.libs.F;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 11/15/13
 * Time: 3:51 PM
 */
public class SellingRecordColumnChartPost extends Post<HighChart> {

    private static final long serialVersionUID = 9099644401516079900L;

    public Date from = DateTime.now().withTimeAtStartOfDay().minusMonths(1).toDate();
    public Date to = new Date();

    public String market;
    public String categoryId;

    /**
     * 搜索字词
     */
    public String val;

    /**
     * selling, sku, category 三个种类
     */
    public String type = "selling";

    @Override
    public F.T2<String, List<Object>> params() {
        return null;
    }

    @Override
    public List<HighChart> query() {
        throw new UnsupportedOperationException("功能还没完成");
    }
}
