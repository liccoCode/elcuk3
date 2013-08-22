package models.view.dto;

import org.apache.commons.lang.StringUtils;
import play.utils.FastRuntimeException;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 对客户端使用 HighChart 的数据的补充与传递
 * <p/>
 * 现在只提供 Line 类型的参数, 如果还需要其他再做重构
 * User: wyatt
 * Date: 1/21/13
 * Time: 10:52 AM
 */
public class HighChart implements Serializable {
    private static final long serialVersionUID = 8112933425792176924L;

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
     * 所有 serise 的名称
     *
     * @return
     */
    public List<String> lineNames() {
        List<String> names = new ArrayList<String>();
        for(Object obj : this.series) {
            Line line = (Line) obj;
            names.add(line.name);
        }
        return names;
    }

    public void sort() {
        for(Object obj : this.series) {
            Line line = (Line) obj;
            line.sort();
        }
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
    public class Line implements Serializable {
        private static final long serialVersionUID = 27276048153447664L;

        Line(String name) {
            this.name = name;
        }

        // line name
        public String name;
        public List<Object[]> data = new ArrayList<Object[]>();
        public Marker marker = new Marker();
        public int yAxis = 0;

        public Line add(Date date, Float y) {
            boolean add = true;
            for(Object[] d : this.data) {
                if(d[0].equals(date.getTime())) {
                    d[1] = (Float) d[1] + y;
                    add = false;
                }
            }
            if(add)
                this.data.add(new Object[]{date.getTime(), y});
            return this;
        }

        public Line sort() {
            Collections.sort(this.data, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] o1, Object[] o2) {
                    long diff = (Long) o1[0] - (Long) o2[0];
                    // 避免 long 差值大于 Integer 的上限而因类型强转导致排序失败
                    if(diff >= Integer.MAX_VALUE || diff <= Integer.MIN_VALUE)
                        diff /= 1000;
                    return (int) diff;
                }
            });
            return this;
        }

        public Line yAxis(int i) {
            this.yAxis = i;
            return this;
        }
    }

    /**
     * 饼图的数据; 数据 + 名称; 百分比会由 HighChart 自行计算
     */
    public class Pie implements Serializable {
        private static final long serialVersionUID = -5409000856476815150L;
        public String name;
        public Float data;
        public Marker marker = new Marker();
    }

    public class Marker implements Serializable {
        private static final long serialVersionUID = -5061528374647700719L;
        public boolean enabled = true;
        public float radius = 2;
    }
}
