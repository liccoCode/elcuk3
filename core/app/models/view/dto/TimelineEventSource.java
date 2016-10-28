package models.view.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ext.ShipmentsHelper;
import helper.GTs;
import helper.Webs;
import models.procure.ProcureUnit;
import models.procure.ShipItem;
import models.procure.Shipment;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.JVMRandom;
import org.joda.time.DateTime;
import play.utils.FastRuntimeException;

import javax.persistence.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    public List<Event> events = new ArrayList<>();


    /**
     * 内部的一个一个的 event, 也就是 Timeline 图表上的一个一个的节点
     */
    public static class Event {

        public Event() {
        }

        public Event(AnalyzeDTO analyzeDTO, ProcureUnit unit) {
            this.analyzeDTO = analyzeDTO;
            this.unit = unit;
        }

        public Event(String start, String end, String description, String title) {
            this(start, end, description, title, true);
        }

        public Event(String start, String end, String description, String title,
                     boolean durationEvent) {
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
        public volatile AnalyzeDTO analyzeDTO;

        @Transient
        public volatile ProcureUnit unit;

        public Float lastDays;

        /**
         * 计算并设置 Start, End Date 与持续天数
         *
         * @return
         */
        public Event startAndEndDate(String type) {
            Date predictShipFinishDate = null;
            List<Shipment> relateShipments = this.unit.relateShipment();

            // 检查运输单的是否可以从签收进入入库状态
            for(Shipment shipment : relateShipments) shipment.inboundingByComputor();
            for(Shipment shipment : relateShipments) shipment.endShipByComputer();


            if(predictShipFinishDate == null && relateShipments.size() > 0) {
                Shipment shipment = relateShipments.get(0);
                predictShipFinishDate = shipment.dates.planArrivDate;
                if(predictShipFinishDate == null && !Arrays.asList(Shipment.S.CANCEL, Shipment.S.PLAN,
                        Shipment.S.CONFIRM).contains(shipment.state))
                    predictShipFinishDate = ShipmentsHelper.predictArriveDate(shipment);
            }

            //如果有签收数量则用采购计划的入库时间
            // 修改新规则 现在开始时间都采用运输单的预计到库时间,快递没关联运输单的话,则采用采购计划上的预计到库时间
            if(predictShipFinishDate == null) {
                predictShipFinishDate = this.unit.attrs.planArrivDate;
            }

            this.lastDays = Webs.scale2PointUp((this.unit.qty() - this.unit.inboundingQty()) / this.ps(type));

            Float timeLineDays = this.lastDays;
            this.start = add8Hour(predictShipFinishDate);

            if(this.unit.stage == ProcureUnit.STAGE.INBOUND) {
                // 如果在入库中, 进度条自动缩短
                timeLineDays = (this.unit.qty() - this.unit.inboundingQty()) / this.ps(type);
            } else if(this.unit.stage == ProcureUnit.STAGE.CLOSE) {
                timeLineDays = 0f;
            }
            // 如果不够卖到第二天, 那么就省略
            this.end = add8Hour(new DateTime(predictShipFinishDate)
                    .plusDays(timeLineDays.intValue()).toDate());
            this.durationEvent = true;


            return this;
        }


        private boolean isEnsureQty() {
            return (this.unit.attrs != null && this.unit.attrs.qty != null);
        }

        public Float ps(String type) {
            return "sku".equals(type) ? this.skuPS() : this.sidPS();
        }

        private Float skuPS() {
            return this.analyzeDTO.getPs_cal();
        }

        private Float sidPS() {
            return (this.analyzeDTO.ps <= 0) ? 0.1f : this.analyzeDTO.ps;
        }

        /**
         * 自动为时间添加 8h 变为北京时间
         * PS: 不知为何, 按照 Simile Timeline 的文档, 如何设置时间格式都会少 8 个小时, 所以就如此修补了
         *
         * @return
         */
        private String add8Hour(Date date) {
            return new DateTime(date).plusHours(8).toString("yyyy-MM-dd HH:mm:ss");
        }

        /**
         * 计算并且设置 title 与 Desction.
         * PS: 如果 lastDays 没有计算, 那么会抛出异常
         *
         * @return
         */
        public Event titleAndDesc() {
            if(this.lastDays == null) throw new FastRuntimeException("请先计算 LastDays");

            if(this.unit.stage == ProcureUnit.STAGE.CLOSE) {
                this.title = String.format("#%s 计划 %s状态, 数量 %s 可销售 %s 天",
                        this.unit.id, getunitstage().label(), 0,
                        0);
            } else {
                this.title = String.format("#%s 计划 %s状态, 数量 %s 可销售 %s 天",
                        this.unit.id, getunitstage().label(), this.unit.attrs.planQty - this.unit.inboundingQty(),
                        this.lastDays);
            }
            this.description = GTs.render("event_desc", GTs.newMap("unit", this.unit).build());
            this.link = "/procureunits?p.search=id:" + this.unit.id;
            return this;
        }


        public ProcureUnit.STAGE getunitstage() {
            //如果是入库数量相等则是已入库
            ProcureUnit.STAGE unitstage = this.unit.stage;
            if(unitstage != ProcureUnit.STAGE.CLOSE) {
                int inboundingqty = this.unit.inboundingQty();
                int planqty = this.unit.attrs.planQty - inboundingqty;
                if(planqty == 0) {
                    unitstage = ProcureUnit.STAGE.CLOSE;
                } else if(inboundingqty > 0) {
                    unitstage = ProcureUnit.STAGE.INBOUND;
                }
            }
            return unitstage;
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

        public Event color(ProcureUnit unitunit) {
            String color = "999999";
            switch(getunitstage()) {
                case PLAN:
                    color = "A5B600";
                    break;
                case DELIVERY:
                    color = "C09853";
                    break;
                case DONE:
                    // warnning
                    color = "FE502A";
                    break;
                case SHIPPING:
                    // info
                    color = "3A87AD";
                    break;
                case SHIP_OVER:
                    //mute
                    color = "F9AB3A";
                    break;
                case INBOUND:
                    color = "CC6615";
                    break;
                case CLOSE:
                    color = "999999";
                    break;
                default:
                    // error
                    color = "B94A48";
            }
            color = fetchShipmentSate(unit, color);
            this.color = String.format("#%s", color);
            return this;
        }
    }

    /**
     * 采购计划的运输单状态
     */
    public static String fetchShipmentSate(ProcureUnit unit, String color) {
        List<ShipItem> shipItems = unit.shipItems;
        if(shipItems.size() == 1) {
            //正常情况下一个采购计划只有一个对应的运输单
            Shipment shipment = shipItems.get(0).shipment;
            switch(shipment.state) {
                case SHIPPING:
                    color = "3A87AD";
                    break;
                case CLEARANCE:
                case PACKAGE:
                case BOOKED:
                case DELIVERYING:
                    color = "3746B1";
                    break;
                case RECEIPTD:
                    color = "5437B1";
                    break;
            }
        } else if(shipItems.size() > 1) {
            //有多个对应的运输单，表示出现异常
            color = "ff0000";
        }
        return color;
    }

    public static String getRandomColorCode() {
        //颜色代码位数  
        int colorLength = 6;

        //颜色代码数组  
        char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        // 需要线程安全, 所以使用 StringBuffer
        StringBuilder sb = new StringBuilder();
        JVMRandom random = new JVMRandom();
        for(int i = 0; i < colorLength; i++) {
            sb.append(codeSequence[random.nextInt(16)]);
        }
        return sb.toString();
    }

    /**
     * 根据 Selling 与指定的 type 类型, 创建当前 Selling 可卖长度的 Event
     *
     * @param analyzeDTO
     * @param type
     * @return
     */
    public static Event currentQtyEvent(AnalyzeDTO analyzeDTO, String type) {
        Event currenEvent = new Event();
        currenEvent.start = currenEvent.add8Hour(new Date());
        float validPs = ("sku".equals(type) ? analyzeDTO.getPs_cal() : analyzeDTO.ps);
        float days = Webs
                .scale2PointUp(analyzeDTO.qty / (validPs == 0 ? Integer.MAX_VALUE : validPs));
        currenEvent.end = currenEvent
                .add8Hour(DateTime.now().plusHours((int) (days * 24)).toDate());
        currenEvent.title = String
                .format("@QTY: %s(%s) 还可卖 %s Days", analyzeDTO.qty, validPs, days);
        currenEvent.description = "No Desc.";
        currenEvent.color("267B2F");
        return currenEvent;
    }

}
