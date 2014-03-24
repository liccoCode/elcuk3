package models.view.dto;

import java.io.Serializable;

/**
 * PM 首页异常信息
 * <p/>
 * User: mac
 * Date: 14-3-21
 * Time: PM2:22
 */
public class AbnormalDTO implements Serializable {
    /**
     * 今天的数据
     */
    public float today = 0;

    /**
     * 上个周期的数据
     */
    public float last = 0;

    /**
     * 当前数据与上个周期内的数据的差值
     */
    public float difference = 0;

    /**
     * 该属于哪个 对象
     */
    public String fid;

    public enum T {
        /**
         * review 信息异常
         */
        REVIEW,

        /**
         * 昨天销售额与同期对比
         */
        DAY1,

        /**
         * 上周销售额与上上周对比
         */
        LAST
    }

    /**
     * 异常信息的类型
     */
    public T abnormalType;

    public AbnormalDTO() {
    }

    public AbnormalDTO(float today, float last, String fid, T abnormalType) {
        this.today = today;
        this.last = last;
        this.fid = fid;
        this.abnormalType = abnormalType;
    }

    public AbnormalDTO(String fid, T abnormalType) {
        this.fid = fid;
        this.abnormalType = abnormalType;
    }
}
