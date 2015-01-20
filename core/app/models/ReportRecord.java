package models;

import com.google.gson.annotations.Expose;
import play.db.jpa.Model;

import javax.persistence.Entity;
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
    public int year;
    @Expose
    public int month;

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
         * 库存报表
         */
        SKUINV {
            @Override
            public String label() {
                return "库存报表";
            }
        };

        public abstract String label();
    }


    public String calUrl() {
        return String.format("http://rock.easya.cc:4567/sku_month_profit_repeat?year=%s"
                + "&month=%s", this.year, this.month);
    }

}
