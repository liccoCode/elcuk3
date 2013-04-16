package models.view.dto;

import helper.Webs;
import models.market.Selling;
import models.view.post.AnalyzePost;
import play.libs.F;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/31/12
 * Time: 6:03 PM
 */
public class AnalyzeDTO {

    public AnalyzeDTO(String fid) {
        this.fid = fid;
    }

    public AnalyzeDTO(Selling sell) {
        this.fid = sell.sellingId;
        this.asin = sell.asin;
        if(sell.account != null) this.aid = sell.account.id + "";
        this.ps = sell.ps;
    }

    /**
     * 用来表示 SKU/Msku
     */
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
     * 入库中的数量
     */
    public int inbound = 0;

    /**
     * 库存
     */
    public int qty = 0;

    /**
     * 计算出来的 PS
     */
    public float ps_cal = 0;

    /**
     * 设置的 PS
     */
    public float ps = 0;

    /**
     * 今天的销量
     */
    public int day0 = 0;

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
     * Selling Review 的数量
     */
    public int reviews = 0;

    /**
     * review 的分数
     */
    public float rating = 0;

    /**
     * 最新的评分
     */
    public float lastRating;

    public float reviewRatio = 0f;

    public float getPs_cal() {
        if(this.ps_cal <= 0) {
            float ps = this.day7 / 7f;
            this.ps_cal = ps <= 0 ? 0.1f : ps;
        }
        return this.ps_cal;
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
     * @return ._1: 根据系统计算出的 ps 计算的这个产品现在(在库 + 在途)的货物还能够周转多少天<br/>
     *         ._2: 根据人工设置的 ps 计算的这个产品现在(在库 + 在途)的货物还能够周转多少天<br/>
     *         ._3: 根据系统计算出的 ps 计算的这个产品现在(在库 + 在途 + 在产)的货物还能够周转多少天<br/>
     *         ._4: 根据人工设置的 ps 计算的这个产品现在(在库 + 在途 + 在产)的货物还能够周转多少天<br/>
     */
    public F.T4<Float, Float, Float, Float> getTurnOverT4() {
        float _ps = this.getPs_cal();
        float ps = this.ps;
        return new F.T4<Float, Float, Float, Float>(
                Webs.scale2PointUp(this.qty / _ps),
                Webs.scale2PointUp(this.qty / (ps == 0 ? _ps : ps)),
                Webs.scale2PointUp((this.qty + this.way + this.working + this.worked) / _ps),
                Webs.scale2PointUp(
                        (this.qty + this.way + this.working + this.worked) / (ps == 0 ? _ps : ps))
        );
    }

    /**
     * 今天和昨天的销量的差别
     *
     * @return ._1 销量差据个数; ._2: 今天是昨天销量的 %; ._3: 颜色; ._4: 上升/下降
     */
    public F.T4<Integer, Float, String, Boolean> day1Differ() {
        /**
         * ._1: 销量差据个数
         * ._2: 今天是昨天销量 %
         * ._3: 颜色
         */
        int yesterDaySales = this.day1 - this.day0;
        int differ = this.day0 - yesterDaySales;
        float ratio = this.day0 / (float) yesterDaySales;
        String color = differ >= 0 ? "267B2F" : "E45652";
        return new F.T4<Integer, Float, String, Boolean>(differ, ratio, color, differ >= 0);
    }

    /**
     * 比较此 Selling 中自行设计的 ps 与计算出来的 _ps 之间的差值
     *
     * @return .1: 差据的大小
     *         .2: 前台使用的颜色代码
     */
    public F.T2<Float, String> psDiffer() {
        float _ps = this.getPs_cal();
        if(_ps >= 5) {
            float diff = Math.abs(_ps - this.ps) /
                    (Math.max(_ps, this.ps) <= 0 ? 1f : Math.max(_ps, this.ps));
            String color = "";
            if(diff >= 0.4)
                color = "E45652";
            else if(diff >= 0.2 && diff < 0.4)
                color = "FAAB3B";
            return new F.T2<Float, String>(diff, color);
        } else {
            return new F.T2<Float, String>(0f, "fff");
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
}
