package models.view.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对客户端使用 HighChart 的数据的补充与传递
 * <p/>
 * 现在只提供 Line 类型的参数, 如果还需要其他再做重构
 * User: wyatt
 * Date: 1/21/13
 * Time: 10:52 AM
 */
public class HighChartLines {
    // 图形的 title
    public String title;

    // x 轴为时间间隔
    public long pointStart = System.currentTimeMillis();
    public long pointInterval = TimeUnit.DAYS.toMillis(1);

    // 曲线
    public List<Line> series = new ArrayList<Line>();

    public HighChartLines startAt(long datetimeMillions) {
        this.pointStart = datetimeMillions;
        return this;
    }

    /**
     * 获取一根曲线或者自动创建一根曲线然后再返回
     *
     * @param name
     * @return
     */
    public Line line(String name) {
        for(Line line : this.series) {
            if(name.equalsIgnoreCase(line.name))
                return line;
        }
        Line line = new Line(name);
        this.series.add(line);
        return line;
    }


    public class Line {
        Line(String name) {
            this.name = name;
        }

        // line name
        public String name;
        public List<Float> data = new ArrayList<Float>();

        public Line add(Float y) {
            this.data.add(y);
            return this;
        }
    }
}
