package models;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Arrays;
import java.util.Date;

/**
 * 系统内的操作日志的记录;
 * <p/>
 * User: cay
 * Date: 1/16/15
 * Time: 11:50 AM
 */
@Entity
public class ReportRecord extends Model {

    @Expose
    public Integer year;
    @Expose
    public Integer month;

    /**
     * 创建时间, 也就是记录时间
     */
    @Expose
    public Date createAt = new Date();

    @Expose
    public int downloadcount = 0;

    @Expose
    public String filename;

    @Expose
    public String filepath;

    @Expose
    public String categoryid;

    @Expose
    @Enumerated(EnumType.STRING)
    public RT reporttype;


    /*
  * 报表类型
  */
    public enum RT {
        /**
         * 月度销售报表汇总
         */
        SKUMONTHALL {
            @Override
            public String label() {
                return "月度销售报表_汇总";
            }
        },
        /**
         * 月度销售报表产品线
         */
        SKUMONTHCATEGORY {
            @Override
            public String label() {
                return "月度销售报表_产品线";
            }
        },
        /**
         * 库存报表汇总
         */
        SKUINVTOTAL {
            @Override
            public String label() {
                return "库存报表汇总";
            }
        },

        /**
         * 库存报表明细
         */
        SKUINVSELLING {
            @Override
            public String label() {
                return "库存报表明细";
            }
        },

        /**
         * 定期销售报表汇总
         */
        SALEYEARTOTAL {
            @Override
            public String label() {
                return "定期销售报表汇总";
            }
        },
        /**
         * 定期销售报表产品线
         */
        SALEYEARCATEGORY {
            @Override
            public String label() {
                return "定期销售报表产品线";
            }
        },
        /**
         * 库存合理性报表
         */
        INVENTORYRATIANALITY {
            @Override
            public String label() {
                return "库存合理性报表";
            }
        };

        public abstract String label();
    }


    public String calUrl() {
        return String.format("http://rock.easya.cc:4567/sku_month_profit_repeat?year=%s"
                + "&month=%s", this.year, this.month);
    }

    /**
     * 判断是否能够重新计算
     * @return
     */
    public boolean canBeRecalculated() {
        return !Arrays.asList(RT.SKUINVSELLING, RT.SKUINVTOTAL, RT.INVENTORYRATIANALITY).contains(this.reporttype);
    }
}
