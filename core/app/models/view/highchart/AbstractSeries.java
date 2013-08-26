package models.view.highchart;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 8/24/13
 * Time: 11:52 PM
 */
public abstract class AbstractSeries implements Serializable {

    public AbstractSeries(String name) {
        this.name = name;
    }

    public AbstractSeries(String name, String type) {
        this(name);
        this.type = type;
    }

    /**
     * Series 的数据为 [[key, value], [key, value]...] 的形式
     */
    public List<Object[]> data = new ArrayList<Object[]>();

    /**
     * Series 的名称
     */
    public String name;

    /**
     * Series 的类型;
     * 常用: line, pie, bar, column
     */
    public String type;

    /**
     * 有左右两条 y 轴时, 匹配哪一条
     */
    public Integer yAxis;

    /**
     * 对曲线中的点的设置
     */
    public Marker marker;

    public <T extends AbstractSeries> T add(Date date, Float y) {
        return add(y, date);
    }

    public <T extends AbstractSeries> T add(String key, Float y) {
        return add(y, key);
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractSeries> T add(Float y, Object key) {
        boolean add = true;
        Object k = key;
        if(key.getClass().equals(Date.class))
            k = ((Date) key).getTime();

        for(Object[] d : this.data) {
            if(d[0].equals(k)) {
                d[1] = (Float) d[1] + y;
                add = false;
            }
        }
        if(add)
            this.data.add(new Object[]{k, y});
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends AbstractSeries> T yAxis(int i) {
        this.yAxis = i;
        return (T) this;
    }

    /**
     * 可在 series: [{name:'', data:[], marker:{xxx}}]
     * 也可在 plotOptions:{series:{marker:{xxx}}}
     * 在这, 直接设到每一条曲线上
     */
    public class Marker implements Serializable {
        private static final long serialVersionUID = -5061528374647700719L;
        public boolean enabled = true;
        public float radius = 2;
    }
}
