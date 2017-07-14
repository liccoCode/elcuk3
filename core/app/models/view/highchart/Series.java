package models.view.highchart;

import java.util.Collections;
import java.util.Comparator;

/**
 * 对 HighChart 的曲线的抽象
 * User: wyatt
 * Date: 8/24/13
 * Time: 11:53 PM
 */
public class Series {

    private Series() {
    }

    public static final String LINE = "line";
    public static final String COLUMN = "column";
    public static final String PIE = "pie";

    /**
     * 线图的数据, 线的名称 + 线的数据, x 轴用时间代替了.
     */
    public static class Line extends AbstractSeries {
        private static final long serialVersionUID = 27276048153447664L;

        public Line(String name) {
            super(name, LINE);
            this.marker = new Marker();
        }

        public Line(String name, boolean visible) {
            super(name, LINE, visible);
            this.marker = new Marker();
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
    }

    /**
     * Column 表
     */
    public static class Column extends AbstractSeries {

        public Column(String name) {
            super(name, COLUMN);
        }
    }

    /**
     * 饼图的数据; 数据 + 名称; 百分比会由 HighChart 自行计算
     */
    public static class Pie extends AbstractSeries {
        private static final long serialVersionUID = -5409000856476815150L;

        public Pie(String name) {
            super(name, PIE);
        }
    }

}
