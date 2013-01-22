package models.view.dto;

import org.apache.commons.lang.StringUtils;
import play.utils.FastRuntimeException;

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
public class HighChart {
    // 图形的 title
    public String title;

    // x 轴为时间间隔
    public long pointStart = System.currentTimeMillis();
    public long pointInterval = TimeUnit.DAYS.toMillis(1);

    // 曲线
    public List series = new ArrayList();
    private String type = "";


    public HighChart startAt(long datetimeMillions) {
        this.pointStart = datetimeMillions;
        return this;
    }

    private void check(String type) {
        if(StringUtils.isNotBlank(this.type) && !type.equalsIgnoreCase(this.type))
            throw new FastRuntimeException("One HighChart instance only handler one chart type."
                    + " You request " + type + " for " + this.type + " chart");
        if(StringUtils.isBlank(this.type))
            this.type = type;
    }

    /**
     * 获取一根曲线或者自动创建一根曲线然后再返回
     *
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public Line line(String name) {
        this.check("line");
        for(Object obj : this.series) {
            Line line = (Line) obj;
            if(name.equalsIgnoreCase(line.name))
                return line;
        }
        Line line = new Line(name);
        this.series.add(line);
        return line;
    }


    /**
     * 为 HighChart 增加一个 Pie 数据;
     * 不要重复添加, 如果 name 存在, 会叠加数据
     *
     * @param name
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    public Pie pie(String name, Float data) {
        this.check("pie");
        for(Object obj : this.series) {
            Pie pie = (Pie) obj;
            if(name.equalsIgnoreCase(pie.name)) {
                pie.data += data;
                return pie;
            }
        }
        Pie pie = new Pie();
        pie.name = name;
        pie.data = data;
        this.series.add(pie);
        return pie;
    }


    /**
     * 线图的数据, 线的名称 + 线的数据, x 轴用时间代替了.
     */
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

    /**
     * 饼图的数据; 数据 + 名称; 百分比会由 HighChart 自行计算
     */
    public class Pie {
        public String name;
        public Float data;
    }

}
