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
    public List<AbstractSeries> series = new ArrayList<>();

    private String type = Series.LINE;

    public void setType(String type) {
        this.type = type.toLowerCase();
    }

    public HighChart startAt(long datetimeMillions) {
        this.pointStart = datetimeMillions;
        return this;
    }

    public String highestMarket;

    public AbstractSeries series(String name) {
        for(AbstractSeries s : this.series) {
            if(name.equalsIgnoreCase(s.name))
                return s;
        }

        AbstractSeries s = null;
        if(Series.LINE.equals(this.type)) {
            s = new Series.Line(name);
        } else if(Series.COLUMN.equals(this.type)) {
            s = new Series.Column(name);
        } else if(Series.PIE.equals(this.type)) {
            s = new Series.Pie(name);
        }
        this.series.add(s);
        return s;
    }

    public boolean series(AbstractSeries serie) {
        if(serie.type.equals(this.type)) {
            this.series.add(serie);
            return true;
        }
        return false;
    }

    /**
     * 生成一个汇总的曲线
     *
     * @return
     */
    public AbstractSeries sumSeries(String name) {
        AbstractSeries s = null;
        if(Series.LINE.equals(this.type)) {
            s = new Series.Line(name + " 汇总");
        } else if(Series.COLUMN.equals(this.type)) {
            s = new Series.Column(name + "汇总");
        } else if(Series.PIE.equals(this.type)) {
            s = new Series.Pie(name + "汇总");
        }

        for(AbstractSeries abstractSeries : this.series) {
            for(Object[] data : abstractSeries.data) {
                s.add((Float) data[1], data[0]);
            }
        }
        if(s.type.equals(Series.LINE)) {
            ((Series.Line) s).sort();
        }
        return s;
    }

    /**
     * 生成一个汇总的曲线(BigDecimal 高精度, float 会出现一些奇怪的精度问题)
     *
     * @return
     */
    public AbstractSeries sumSeriesWithBigDecimal(String name) {
        AbstractSeries s = null;
        if(Series.LINE.equals(this.type)) {
            s = new Series.Line(name + " 汇总");
        } else if(Series.COLUMN.equals(this.type)) {
            s = new Series.Column(name + "汇总");
        } else if(Series.PIE.equals(this.type)) {
            s = new Series.Pie(name + "汇总");
        }

        for(AbstractSeries abstractSeries : this.series) {
            for(Object[] data : abstractSeries.data) {
                s.addWithBigDecimal((Float) data[1], data[0]);
            }
        }
        if(s.type.equals(Series.LINE)) {
            ((Series.Line) s).sort();
        }
        return s;
    }
}
