package models.view.dto;

import com.google.gson.annotations.Expose;
import helper.Webs;
import models.market.M;
import models.market.Selling;
import models.view.post.AnalyzePost;
import org.hibernate.annotations.DynamicUpdate;
import play.db.jpa.GenericModel;
import play.libs.F;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/31/12
 * Time: 6:03 PM
 */
@Entity
@DynamicUpdate
public class AnalyzeDTO extends GenericModel implements Serializable {

    private static final long serialVersionUID = -6922565933590728789L;

    public AnalyzeDTO(String fid) {
        this.fid = fid;
    }

    public AnalyzeDTO(String fid, String state) {
        this.fid = fid;
        this.state = state;
    }

    /**
     * 用来表示 SKU/Msku
     */
    @Id
    @Column(length = 80)
    @Expose
    public String fid;

    /**
     * Selling 的 ASIN (Option)
     */
    public String asin;

    /**
     * Selling 的 AccountId (Option)
     */
    public String aid;

    /**
     * 计划中的数量
     */
    public int plan = 0;
    /**
     * 正在制作的数量
     */
    public int working = 0;

    /**
     * 交货了的数量, 也可当做是剩余没有运输出去的数量
     */
    public int worked = 0;

    /**
     * 运输中的数量, 也可认为是运输出去了的数量
     */
    public int way = 0;

    /**
     * 剩余入库中的数量
     */
    public int inbound = 0;

    /**
     * 库存
     */
    public int qty = 0;

    /**
     * 欧洲仓库存
     */
    public int eurQty = 0;

    /**
     * 计算出来的 PS
     */
    public float ps_cal = 0;

    /**
     * 设置的 PS
     */
    public float ps = 0;


    /**
     * 昨天到今天的销量
     */
    public int day1 = 0;

    /**
     * 过去 7 天总销量
     */
    public int day7 = 0;

    /**
     * 过去 30 天总销量
     */
    public int day30 = 0;

    /**
     * day1 与 day7平均值的差值
     */
    public float difference = 0;


    /**
     * Selling Review 的数量
     */
    public int reviews = 0;

    /**
     * review 的分数
     */
    public float rating = 0;

    /**
     * 最新的评分与时间
     */
    public Float lastRating = 0f;
    public Date lastRatingDate;

    public float reviewRatio = 0f;

    /**
     * 市场
     */
    @Enumerated(EnumType.STRING)
    public M market;

    /**
     * 状态(NEW、SELLING、DOWN)
     */
    public String state;

    /**
     * 断货天数
     */
    public float outday;

    /**
     * 实时价格
     */
    public float displayPrice;

    /**
     * 主要运输方式
     */
    public String main_ship_type;

    /**
     * 生命周期
     */
    @Enumerated(EnumType.STRING)
    public Selling.SC sellingCycle;

    /**
     * 保留库存
     */
    public int reserved = 0;

    /**
     * 断货天数颜色区分
     */
    public String step;

    /**
     * 退货数
     */
    public int returns = 0;

    /**
     * 正常销量数
     */
    public int sales = 0;

    /**
     * 退货率
     */
    public float returnRates = 0;

    @Enumerated(EnumType.STRING)
    public TYPE type;

    public enum TYPE {
        SELLING,
        SKU
    }


    public static String[] indexColor = {"progress-bar-aqua", "progress-bar-red", "progress-bar-green",
            "progress-bar-yellow"};

    //BEGIN GENERATED CODE
    public float getPs_cal() {
        if(this.ps_cal <= 0) {
            float ps = this.day7 / 7f;
            this.ps_cal = ps <= 0 ? 0.1f : ps;
        }
        this.ps_cal = Webs.scale2PointUp(this.ps_cal);
        return this.ps_cal;
    }

    public float getDis_Price() {
        try {
            Selling sell = Selling.findById(this.fid);
            if(sell != null) {
                if(sell.aps != null && sell.aps.salePrice != null) {
                    return sell.aps.salePrice;
                }
            }
        } catch(Exception e) {
        }
        return 0;
    }


    /**
     * 计算系数内的两个 Turnover 值<br/>
     * 前提:
     * - ps
     * - d7
     * - qty
     * - onway
     * - onwork
     *
     * @return ._1: 根据系统计算出的 ps 计算的这个产品现在(在库)的货物还能够周转多少天<br/>
     * ._2: 根据人工设置的 ps 计算的这个产品现在(在库)的货物还能够周转多少天<br/>
     * ._3: 根据系统计算出的 ps 计算的这个产品现在(在库 + 在途 + 入库 + 在产)的货物还能够周转多少天<br/>
     * ._4: 根据人工设置的 ps 计算的这个产品现在(在库 + 在途 + 入库 + 在产)的货物还能够周转多少天<br/>
     */
    public F.T4<Float, Float, Float, Float> getTurnOverT4() {
        float _ps = this.getPs_cal();
        float ps = this.ps;
        return new F.T4<>(
                Webs.scale2PointUp(this.qty / _ps),
                Webs.scale2PointUp(this.qty / (ps == 0 ? _ps : ps)),
                Webs.scale2PointUp((this.qty + this.way + this.inbound + this.working + this.worked) / _ps),
                Webs.scale2PointUp(
                        (this.qty + this.way + this.inbound + this.working + this.worked) / (ps == 0 ? _ps : ps))
        );
    }

    public F.T4<Float, Float, Float, Float> getSidTurnOverT4() {
        float _ps = this.getPs_cal();
        _ps = _ps < 1 ? 1f : _ps;
        float ps = this.ps;
        return new F.T4<>(
                Webs.scale2PointUp(this.qty / _ps),
                Webs.scale2PointUp(this.qty / (ps == 0 ? _ps : ps)),
                Webs.scale2PointUp((this.qty + this.way + this.inbound + this.working + this.worked) / _ps),
                Webs.scale2PointUp(
                        (this.qty + this.way + this.inbound + this.working + this.worked) / (ps == 0 ? _ps : ps))
        );
    }

    /**
     * 比较此 Selling 中自行设计的 ps 与计算出来的 _ps 之间的差值
     *
     * @return .1: 差据的大小
     * .2: 前台使用的颜色代码
     */
    public F.T2<Float, String> getPsDiffer() {
        float _ps = this.getPs_cal();
        if(_ps >= 5) {
            float diff = Math.abs(_ps - this.ps) / (Math.max(_ps, this.ps) <= 0 ? 1f : Math.max(_ps, this.ps));
            String color = "";
            if(diff >= 0.4)
                color = "E45652";
            else if(diff >= 0.2 && diff < 0.4)
                color = "FAAB3B";
            return new F.T2<>(Webs.scale2PointUp(diff), color);
        } else {
            return new F.T2<>(0f, "fff");
        }
    }

    /**
     * 返回 Analyzes 分析后的 DTO 的缓存值. 由于这个缓存是没有时间限制的, 所以就不需要重新计算了
     *
     * @param type sku/sid
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<AnalyzeDTO> cachedAnalyzeDTOs(String type) {
        return new AnalyzePost(type).analyzes();
    }

    /**
     * 在缓存中根据 type 与 val 寻找 AnalyzeDTO
     *
     * @param type
     * @param val
     * @return
     */
    public static AnalyzeDTO findByValAndType(String type, String val) {
        AnalyzePost post = new AnalyzePost(type);
        for(AnalyzeDTO dto : post.analyzes()) {
            if(val.equalsIgnoreCase(dto.fid)) {
                return dto;
            }
        }
        return null;
    }

    /**
     * float 在 Excel 中会出现自动进位的处理，所以采取转成 Double 的形式
     *
     * @return
     */
    public String prettyDisplayPrice() {
        DecimalFormat df = new DecimalFormat("#.00");
        return df.format(displayPrice);
    }

    public int totalfive() {
        return this.working + this.worked + this.way + this.inbound + this.qty;
    }

    public float day() {
        return new BigDecimal(this.day30 / 30).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }


    public boolean containsCategory(List<String> categories) {
        return categories.stream().anyMatch(category -> {
            int length = category.length();
            if(length >= this.fid.length()) {
                return false;
            }
            String temp = this.fid.substring(0, length);
            return Objects.equals(category, temp);
        });
    }

    private static boolean containsTemp(String category, String temp) {
        return Objects.equals(category, temp);
    }
}
