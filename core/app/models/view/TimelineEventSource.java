package models.view;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import helper.Dates;
import helper.Webs;
import models.market.Selling;
import models.procure.ProcureUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.JVMRandom;
import org.joda.time.DateTime;
import play.utils.FastRuntimeException;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.List;

/**
 * 结合前台 Simile Timeline 使用的 pojo
 * User: wyattpan
 * Date: 7/11/12
 * Time: 4:22 PM
 */
public class TimelineEventSource {

    @SerializedName("wiki-url")
    @Expose
    public String wiki_url;

    @SerializedName("wiki-section")
    @Expose
    public String wiki_section;

    /**
     * 前台解析时间的格式, 拥有: iso8601 与 Gregorian 两种方式
     */
    @Expose
    public String dateTimeFormat = "iso8601";

    @Expose
    public List<Event> events = new ArrayList<Event>();


    /**
     * 内部的一个一个的 event, 也就是 Timeline 图表上的一个一个的节点
     */
    public static class Event {

        public Event() {
        }

        public Event(Selling selling, ProcureUnit unit) {
            this.selling = selling;
            this.unit = unit;
        }

        public Event(String start, String end, String description, String title) {
            this(start, end, description, title, true);
        }

        public Event(String start, String end, String description, String title, boolean durationEvent) {
            this.start = start;
            this.end = end;
            this.description = description;
            this.title = title;
            this.durationEvent = durationEvent;
        }

        /**
         * icon - url. This image will appear next to the title text in the timeline if (no end date) or (durationEvent = false). If a start and end date are supplied, and durationEvent is true, the icon is not shown. If icon attribute is not set, a default icon from the theme is used.
         */
        @Expose
        public String icon;

        /**
         * image - url to an image that will be displayed in the bubble
         */
        @Expose
        public String image;

        /**
         * link - url. The bubble's title text be a hyper-link to this address.
         */
        @Expose
        public String link;

        /**
         * color - color of the text and tape (duration events) to display in the timeline. If the event has durationEvent = false, then the bar's opacity will be applied (default 20%). See durationEvent, above.
         */
        @Expose
        public String color;

        /**
         * textColor - color of the label text on the timeline. If not set, then the color attribute will be used.
         */
        @Expose
        public String textColor;

        /**
         * 没看懂的两个参数
         * tapeImage and tapeRepeat Sets the background image and repeat style for the event's tape (or 'bar') on the Timeline. Overrides the color setting for the tape. Repeat style should be one of {repeat | repeat-x | repeat-y}, repeat is the default. See the Cubism example for a demonstration. Only applies to duration events.
         */

        /**
         * caption - additional event information shown when mouse is hovered over the Timeline tape or label. Uses the html title property. Looks like a tooltip. Plain text only. See the cubism example.
         */
        @Expose
        public String caption;

        /**
         * classname - added to the HTML classnames for the event's label and tape divs. Eg classname attribute 'hot_event' will result in div classes of 'timeline-event-label hot_event' and 'timeline-event-tape hot_event' for the event's Timeline label and tape, respectively.
         */
        @Expose
        public String classname;

        /**
         * description - will be displayed inside the bubble with the event's title and image.<br/>
         * - XML Format: the description is stored as the text content of the event element (see below). Note: the XML standard requires that an element's text content must be escaped/formatted HTML.<br/>
         * - JSON Format: the description key of the event hash<br/>
         */
        @Expose
        public String description;


        // --------- basic info --------

        /**
         * 事件开始时间
         * yyyy-MM-dd HH:mm:ss 格式
         */
        @Expose
        public String start;

        /**
         * 事件结束时间
         * yyyy-MM-dd HH:mm:ss 格式
         */
        @Expose
        public String end;

        /**
         * 显示在 Timeline 上的文字
         */
        @Expose
        public String title;

        /**
         * 是否为具有时间段性质的事件? 默认为有
         */
        @Expose
        public boolean durationEvent = true;

        // ---------------- 计算使用
        @Transient
        public volatile Selling selling;

        @Transient
        public volatile ProcureUnit unit;

        public Float lastDays;

        /**
         * 计算并设置 Start, End Date 与持续天数
         *
         * @return
         */
        public Event startAndEndDate(String type) {
            DateTime planDt = new DateTime(this.unit.plan.planArrivDate.getTime());
            this.lastDays = Webs.scale2PointUp((isEnsureQty() ? this.unit.delivery.ensureQty : this.unit.plan.planQty) / ("sku".equals(type) ? this.selling._ps() : this.selling.ps));
            this.start = Dates.date2Date(planDt.toDate());
            this.end = Dates.date2Date(planDt.plusHours((int) (this.lastDays * 24)).toDate());
            this.durationEvent = true;
            return this;
        }

        private boolean isEnsureQty() {
            return (this.unit.delivery != null && this.unit.delivery.ensureQty != null);
        }

        /**
         * 计算并且设置 title 与 Desction.
         * PS: 如果 lastDays 没有计算, 那么会抛出异常
         *
         * @return
         */
        public Event titleAndDesc() {
            if(this.lastDays == null) throw new FastRuntimeException("请先计算 LastDays");
            this.title = String.format("%s Days, %s(%s) %s(%s)",
                    this.lastDays, this.unit.plan.supplier,
                    this.unit.sid,
                    (isEnsureQty() ? this.unit.delivery.ensureQty : this.unit.plan.planQty),
                    (isEnsureQty() ? "EnsureQty" : "PlanQty"));
            this.description = "<h2>这里想看到什么数据??</h2>";
            return this;
        }

        /**
         * 计算并设置 event 颜色
         *
         * @return 没有 # 的颜色 fffeee
         */
        public Event color() {
            if(StringUtils.isNotBlank(this.color)) return this;
            this.color = "#" + getRandomColorCode();
            return this;
        }

        public Event color(String color) {
            this.color = "#" + color;
            return this;
        }
    }

    public static String getRandomColorCode() {
        //颜色代码位数  
        int colorLength = 6;

        //颜色代码数组  
        char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        // 需要线程安全, 所以使用 StringBuffer
        StringBuffer sb = new StringBuffer();
        JVMRandom random = new JVMRandom();
        for(int i = 0; i < colorLength; i++) {
            sb.append(codeSequence[random.nextInt(16)]);
        }
        return sb.toString();
    }

    /**
     * 根据 Selling 与指定的 type 类型, 创建当前 Selling 可卖长度的 Event
     *
     * @param selling
     * @param type
     * @return
     */
    public static Event currentQtyEvent(Selling selling, String type) {
        Event currenEvent = new Event();
        currenEvent.start = Dates.date2Date();
        float days = Webs.scale2PointUp(selling.qty / ("sku".equals(type) ? selling._ps : selling.ps));
        currenEvent.end = Dates.date2Date(DateTime.now().plusHours((int) (days * 24)).toDate());
        currenEvent.title = String.format("@QTY: %s(%s) 还可卖 %s Days", selling.qty, selling.ps, days);
        currenEvent.description = "No Desc.";
        currenEvent.color("267B2F");
        return currenEvent;
    }

}
