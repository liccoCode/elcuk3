package models.view.highchart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对客户端使用 HighChart 的数据的补充与传递
 * <p/>
 * User: wyatt
 * Date: 1/21/13
 * Time: 10:52 AM
 */
public class HighChart implements Serializable {
    public HighChart() {
        this.type = Series.LINE;
    }

    public HighChart(String type) {
        this.type = type.toLowerCase();
    }

    private static final long serialVersionUID = 8112933425792176924L;

    // 图形的 title
    public String title;

    // x 轴为时间间隔
    public long pointStart = System.currentTimeMillis();
    public long pointInterval = TimeUnit.DAYS.toMillis(1);

    // 曲线
    public List<AbstractSeries> series = new ArrayList<AbstractSeries>();

    private String type = Series.LINE;

    public void setType(String type) {
        this.type = type.toLowerCase();
    }

    public HighChart startAt(long datetimeMillions) {
        this.pointStart = datetimeMillions;
        return this;
    }

    public AbstractSeries series(String name) {
        for(AbstractSeries s : this.series) {
            if(name.equalsIgnoreCase(s.name))
                return s;
        }

        AbstractSeries s = null;
        if(Series.LINE.equals(this.type)) {
            s = new Series.Line(name, this.type);
        } else if(Series.COLUMN.equals(this.type)) {
            s = new Series.Column(name, this.type);
        } else if(Series.PIE.equals(this.type)) {
            s = new Series.Pie(name, this.type);
        }
        this.series.add(s);
        return s;
    }

}
