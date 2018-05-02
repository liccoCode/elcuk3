package models.view.post;

import com.alibaba.fastjson.JSON;
import helper.*;
import jobs.analyze.SellingSaleAnalyzeJob;
import models.OperatorConfig;
import models.market.M;
import models.market.Selling;
import models.procure.ProcureUnit;
import models.view.dto.AnalyzeDTO;
import models.view.dto.TimelineEventSource;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import play.libs.F;
import play.utils.FastRuntimeException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 分析页面的 Post 请求
 * User: wyattpan
 * Date: 10/8/12
 * Time: 10:16 AM
 */
public class AnalyzePost extends Post<AnalyzeDTO> {
    private static final long serialVersionUID = -1015281567545340148L;

    public Date from = super.from;
    public Date to = super.to;

    public AnalyzePost() {
        this.from = DateTime.now().minusMonths(1).toDate();
        this.to = new Date();
        this.perSize = 20;
    }

    public AnalyzePost(String type) {
        this.type = type;
    }

    public String type = "sid";
    public String aid;

    public String orderBy = "day7";
    public Boolean desc = true;

    // 是否过滤掉含有 ,2 的 sid/sku; 默认过滤
    public boolean filterDot2 = true;
    public String categoryId;

    public String flag = "1";

    /**
     * 根据 type 指定是 msku 还是 sku
     */
    public String val;

    /**
     * val 会通过setVal方法截取成msku
     * 所以需要保存sellingId
     */
    public String sellingId;

    public String market;

    public String state = "Active";


    public int ismoveing;

    @Override
    public F.T2<String, List<Object>> params() {
        // no use
        throw new UnsupportedOperationException("AnalyzePost 不需要调用 params()");
    }

    @SuppressWarnings("unchecked")
    public List<AnalyzeDTO> analyzes() {
        String cacke_key = "sid".equals(this.type) ? "analyze_post_sid" : "analyze_post_sku";

        List<AnalyzeDTO> dtos = null;
        String cache_str = Caches.get(cacke_key);
        if(StringUtils.isNotBlank(cache_str)) dtos = JSON.parseArray(cache_str, AnalyzeDTO.class);
        // 从数据库抓取
        if(StringUtils.isBlank(cache_str) || dtos == null || dtos.isEmpty()) {
            if(this.type.equals("sid")) dtos = AnalyzeDTO.find(" type='SELLING'").fetch();
            else dtos = AnalyzeDTO.find(" type='SKU'").fetch();
        }
        return dtos;
    }

    @Override
    public List<AnalyzeDTO> query() {
        List<AnalyzeDTO> dtos = this.analyzes();

        if(this.type.equals("sid")) {
            setOutDayColor(dtos, null);
            calculationEurInventory(dtos);
        }

        // 过滤各种条件
        if(StringUtils.isNotBlank(this.categoryId))
            CollectionUtils.filter(dtos, new SearchPredicate("^" + this.categoryId));
        if(StringUtils.isNotBlank(this.search))
            CollectionUtils.filter(dtos, new SearchPredicate(this.search));
        if(StringUtils.isNotBlank(this.orderBy))
            Collections.sort(dtos, new FieldComparator(this.orderBy, this.desc));
        if(StringUtils.isNotBlank(this.aid) && "sid".equalsIgnoreCase(this.type))
            CollectionUtils.filter(dtos, new AccountIdPredicate(this.aid));
        if(this.filterDot2) CollectionUtils.filter(dtos, new UnContainsPredicate(",2"));
        if(StringUtils.isNotBlank(this.market))
            CollectionUtils.filter(dtos, new MarketPredicate(M.val(this.market)));
        if(StringUtils.isNotBlank(this.state) && !this.state.equals("All"))
            CollectionUtils.filter(dtos, new StatePredicate(this.state));
        if(Objects.equals(this.flag, "1")) {
            return dtos.stream().filter(dto -> (dto.day1 > 0 || dto.ps > 0 || dto.qty > 0)).collect(Collectors.toList());
        }
        return dtos;
    }


    public List<AnalyzeDTO> query(List<AnalyzeDTO> dtos) {
        if(this.type.equals("sid")) {
            setOutDayColor(dtos, null);
            calculationEurInventory(dtos);
        }

        // 过滤各种条件
        if(StringUtils.isNotBlank(this.categoryId))
            CollectionUtils.filter(dtos, new SearchPredicate("^" + this.categoryId));
        if(StringUtils.isNotBlank(this.search))
            CollectionUtils.filter(dtos, new SearchPredicate(this.search));
        if(StringUtils.isNotBlank(this.orderBy))
            Collections.sort(dtos, new FieldComparator(this.orderBy, this.desc));
        if(StringUtils.isNotBlank(this.aid) && "sid".equalsIgnoreCase(this.type))
            CollectionUtils.filter(dtos, new AccountIdPredicate(this.aid));
        if(this.filterDot2) CollectionUtils.filter(dtos, new UnContainsPredicate(",2"));
        if(StringUtils.isNotBlank(this.market))
            CollectionUtils.filter(dtos, new MarketPredicate(M.val(this.market)));
        if(StringUtils.isNotBlank(this.state) && !this.state.equals("All"))
            CollectionUtils.filter(dtos, new StatePredicate(this.state));
        if(Objects.equals(this.flag, "1")) {
            return dtos.stream().filter(dto -> (dto.day1 > 0 || dto.ps > 0 || dto.qty > 0)).collect(Collectors.toList());
        }
        return dtos;
    }


    public List<AnalyzeDTO> queryByPrivate(List<AnalyzeDTO> dtos, List<String> categories) {
        return dtos.stream().filter(dto -> dto.containsCategory(categories)).collect(Collectors.toList());
    }

    public List<AnalyzeDTO> queryOrderByDayOne() {
        List<AnalyzeDTO> dtos = this.analyzes();
        if(StringUtils.isNotBlank(this.orderBy))
            Collections.sort(dtos, new FieldComparator("day1", this.desc));
        if(StringUtils.isNotBlank(this.market))
            CollectionUtils.filter(dtos, new MarketPredicate(M.val(this.market)));
        return dtos;
    }

    public void calculationEurInventory(List<AnalyzeDTO> dtos) {
        StringBuffer sql = new StringBuffer("SELECT q.product_sku AS 'sku',  sum(q.qty) AS 'total' FROM SellingQTY q");
        sql.append(" LEFT JOIN Selling s ON s.sellingId = q.selling_sellingId ");
        sql.append(" WHERE s.market IN ('AMAZON_DE','AMAZON_IT','AMAZON_UK','AMAZON_FR','AMAZON_ES') ");
        sql.append(" GROUP BY q.product_sku ");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString());
        Map<String, Integer> map = new HashMap<>();
        rows.forEach(row -> map.put(row.get("sku").toString(), Integer.parseInt(row.get("total").toString())));
        dtos.forEach(dto -> {
            if(Arrays.asList("de", "it", "uk", "fr", "es").contains(dto.market.sortName())) {
                String sku = dto.fid.split(",")[0];
                dto.eurQty = map.get(sku) == null ? 0 : map.get(sku);
            }
        });
    }

    public static int setOutDayColor(List<AnalyzeDTO> dtos, Integer needCompare) {
        OperatorConfig outDays_config = OperatorConfig.find("name = ? ", "标准断货期天数").first();
        int outDay = Integer.parseInt(outDays_config.val);
        OperatorConfig out_day_area = OperatorConfig.find("name = ? ", "标准断货期天数区间").first();
        String area = out_day_area.val;

        int area_one_first = Integer.parseInt(area.split(",")[0].split("-")[0]);
        int area_one_second = Integer.parseInt(area.split(",")[0].split("-")[1]);

        int area_two_first = Integer.parseInt(area.split(",")[1].split("-")[0]);
        int area_two_second = Integer.parseInt(area.split(",")[1].split("-")[1]);

        int area_three = Integer.parseInt(area.split(",")[2]);
        if(needCompare == null) {
            for(AnalyzeDTO dto : dtos) {
                int temp = 0;
                if(dto.day30 != 0) {
                    float day30ave = new BigDecimal(dto.day30 / 30.0).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
                    if(day30ave > 0) {
                        temp = new BigDecimal((dto.working + dto.worked + dto.way + dto.inbound + dto.qty) / day30ave)
                                .setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                        if(temp < outDay) {
                            dto.step = "";
                        }
                        if(temp >= area_one_first && temp <= area_one_second) {
                            dto.step = "#FFDA68";
                        }
                        if(temp >= area_two_first && temp <= area_two_second) {
                            dto.step = "#CC9900";
                        }
                        if(temp >= area_three) {
                            dto.step = "#FF6868";
                        }
                    }
                }
            }
        } else {
            if(needCompare > outDay) {
                return 1;
            }
        }
        return 0;
    }


    @Override
    public Long getTotalCount() {
        return (long) this.analyzes().size();
    }

    private static class FieldComparator implements Comparator<AnalyzeDTO> {
        private Field field;
        private boolean desc = true;

        private FieldComparator(String fieldName, Boolean desc) {
            try {
                this.field = AnalyzeDTO.class.getField(fieldName);
            } catch(Exception e) {
                try {
                    this.field = AnalyzeDTO.class.getField("day7");
                } catch(Exception e1) {
                    //ignore
                }
            }
            this.desc = desc == null ? true : desc;
        }

        @Override
        public int compare(AnalyzeDTO o1, AnalyzeDTO o2) {
            try {
                Float differ = 0f;
                if(desc) differ = (this.field.getFloat(o2) - this.field.getFloat(o1));
                else differ = (this.field.getFloat(o1) - this.field.getFloat(o2));
                // 避免太小无法正确比较大小
                if(differ < 1) differ *= 100;
                else if(differ < 0.1) differ *= 1000;
                else if(differ < 0.01) differ *= 10000;
                return differ.intValue();
            } catch(Exception e) {
                // 错误,没有这个字段,无法排序
                return 0;
            }
        }
    }


    private static class SearchPredicate implements Predicate {
        private String str;

        private SearchPredicate(String containsString) {
            this.str = containsString;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            if(str.startsWith("^"))
                return dto.fid.toLowerCase()
                        .startsWith(StringUtils.replace(str.toLowerCase(), "^", ""));
            else
                return dto.fid.toLowerCase().contains(str.toLowerCase());
        }
    }

    private static class UnContainsPredicate implements Predicate {
        private String str;

        private UnContainsPredicate(String str) {
            this.str = str;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            return !dto.fid.toLowerCase().contains(str.toLowerCase());
        }
    }

    private static class AccountIdPredicate implements Predicate {
        private String aid;

        public AccountIdPredicate(String aid) {
            this.aid = aid;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            return this.aid.equals(dto.aid);
        }
    }

    private static class MarketPredicate implements Predicate {
        private M market;

        public MarketPredicate(M market) {
            this.market = market;
        }

        @Override
        public boolean evaluate(Object o) {
            AnalyzeDTO dto = (AnalyzeDTO) o;
            return this.market.equals(dto.market);
        }

    }

    /**
     * Selling 和 Product 状态(NEW、SELLING、DOWN)过滤
     */
    private static class StatePredicate implements Predicate {
        private String state;

        public StatePredicate(String state) {
            this.state = state;
        }

        @Override
        public boolean evaluate(Object object) {
            AnalyzeDTO dto = (AnalyzeDTO) object;
            if(StringUtils.equalsIgnoreCase(this.state, "Active")) {
                //只查询出活跃的(状态为 NEW、SELLING)
                return dto.state.equals("NEW") || dto.state.equals("SELLING");
            } else {
                //只查询出不活跃的(状态为 DOWN 的)
                return dto.state.equals("DOWN");
            }
        }
    }
    // ---------------- TimeLine ------------------------

    /**
     * 加载并且返回 Simile Timeline 的 Events
     * type 只允许为 sku, sid 两种类型; 如果 type 为空,默认为 sid
     */
    public static TimelineEventSource timelineEvents(String type, String val) {
        if(StringUtils.isBlank(type)) type = "sid";
        if("msku".equals(type)) type = "sid"; // 兼容
        if(!"sku".equals(type) && !"sid".equals(type))
            throw new FastRuntimeException("查看的数据类型(" + type + ")错误! 只允许 sku 与 sid.");

        DateTime dt = DateTime.now();
        List<ProcureUnit> units = ProcureUnit.find(String.format("createDate>=? AND createDate<=? AND %s=? "
                        + "AND planQty>0 AND projectName!='MengTop' ", type),
                Dates.morning(dt.minusMonths(12).toDate()), Dates.night(dt.toDate()), val).fetch();
        // 将所有与此 SKU/SELLING 关联的 ProcureUnit 展示出来.(前 9 个月~后3个月)
        TimelineEventSource eventSource = new TimelineEventSource();
        AnalyzeDTO analyzeDTO = AnalyzeDTO.findByValAndType(type, val);
        for(ProcureUnit unit : units) {
            TimelineEventSource.Event event = new TimelineEventSource.Event(analyzeDTO, unit);
            event.startAndEndDate(type).titleAndDesc().color(unit);
            eventSource.events.add(event);
        }

        // 将当前 Selling 的销售情况展现出来
        eventSource.events.add(TimelineEventSource.currentQtyEvent(analyzeDTO, type));
        return eventSource;
    }

    public void setVal(String val) {
        if(StringUtils.isNotBlank(val)) {
            this.sellingId = val;
            Selling selling = Selling.findById(val);
            if(selling != null) {
                val = selling.merchantSKU;
            } else {
                val = StringUtils.split(val, "|")[0];
            }
        }
        this.val = val;
    }

    @Override
    public AnalyzePost clone() throws CloneNotSupportedException {
        return (AnalyzePost) super.clone();
    }

    public String countryName(boolean sortName) {
        Selling selling = null;
        if(StringUtils.isNotBlank(this.sellingId)) {
            selling = Selling.findById(this.sellingId);
        }
        return selling != null ? sortName ? selling.market.sortName() : selling.market.countryName() : "";
    }

}
