package models.view;

import play.libs.F;

/**
 * Created by IntelliJ IDEA.
 * User: wyattpan
 * Date: 8/31/12
 * Time: 6:03 PM
 */
public class AnalyzeDTO {

    /**
     * 用来表示 SKU/Msku
     */
    public String fid;

    /**
     * 计划中的数量
     */
    public int plan;
    /**
     * 正在制作的数量j
     */
    public int work;
    /**
     * 运输中的数量
     */
    public int way;

    /**
     * 库存
     */
    public int qty;

    /**
     * 计算出来的 PS
     */
    public float ps_cal;

    /**
     * 设置的 PS
     */
    public float ps;

    /**
     * Selling 的 TurnOver<br/>
     * ._1: 根据系统计算出的 ps 计算的这个产品现在(在库 + 在途)的货物还能够周转多少天<br/>
     * ._2: 根据人工设置的 ps 计算的这个产品现在(在库 + 在途)的货物还能够周转多少天<br/>
     * ._3: 根据系统计算出的 ps 计算的这个产品现在(在库 + 在途 + 在产)的货物还能够周转多少天<br/>
     * ._4: 根据人工设置的 ps 计算的这个产品现在(在库 + 在途 + 在产)的货物还能够周转多少天<br/>
     */
    public F.T4<Float, Float, Float, Float> turnOverT4 = new F.T4<Float, Float, Float, Float>(0f, 0f, 0f, 0f);

}
