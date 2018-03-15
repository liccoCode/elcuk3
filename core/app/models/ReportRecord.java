package models;

import com.google.gson.annotations.Expose;
import helper.Constant;
import helper.HTTP;
import org.joda.time.DateTime;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    private static final long serialVersionUID = 6902337215866962838L;
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


    /**
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
        },

        SELLINGCYCLE {
            @Override
            public String label() {
                return "Selling 状态汇总报表";
            }
        },

        INVRNTORYCOST {
            @Override
            public String label() {
                return "库存占用资金报表";
            }
        },

        REVENUEANDCOST {
            @Override
            public String label() {
                return "主营业务收入与成本报表";
            }
        },

        SALESFEELIST {
            @Override
            public String label() {
                return "销售费用明细报表";
            }
        },

        PAYBILLDETAIL {
            @Override
            public String label() {
                return "应付账款明细表";
            }
        },

        ANALYZEREPORT {
            @Override
            public String label() {
                return "销售分析报表";
            }
        },
        PROCURECOSTANALYSIS {
            @Override
            public String label() {
                return "采购成本报表";
            }
        },
        SHIPMENTMONTHLY {
            @Override
            public String label() {
                return "物流运输月度报表";
            }
        };

        public abstract String label();
        /**
         * Warning:
         * <p/>
         * 1、为了复用 ReportPost 类, 请务必新将 新添加的报表类型 按照 报表类型 添加到 models.view.post.ReportPost 类当中的
         * saleReportTypes(销售报表) 方法 或 applyReportTypes(财务) 方法内。
         *
         * 2、能够重新计算的报表类型请添加到下方的 canBeRecalculated 方法内。
         */
    }

    public ReportRecord() {
        this.createAt = new Date();
        this.downloadcount = 0;
    }

    /**
     * 判断是否能够重新计算
     *
     * @return
     */
    public boolean canBeRecalculated() {
        switch(this.reporttype) {
            case ANALYZEREPORT:
            case SKUMONTHALL:
            case SKUMONTHCATEGORY:
            case SALEYEARTOTAL:
            case SALEYEARCATEGORY:
            case SHIPMENTMONTHLY:
                return true;
            case INVRNTORYCOST:
                return this.year == DateTime.now().getYear();
            default:
                return false;
        }
    }

    public String recalculateURL() {
        switch(this.reporttype) {
            case ANALYZEREPORT:
            case SKUMONTHALL:
            case SKUMONTHCATEGORY:
            case SALEYEARTOTAL:
            case SALEYEARCATEGORY:
                return String.format("%s/sku_month_profit_repeat?year=%s&month=%s",
                        System.getenv(Constant.ROCKEND_HOST), this.year, this.month);
            case INVRNTORYCOST:
                return String.format("%s/inventory_costs_report?force=true", System.getenv(Constant.ROCKEND_HOST));
            default:
                return null;
        }
    }

    public void recalculate() {
        if(this.canBeRecalculated()) {
            HTTP.get(this.recalculateURL());
        } else {
            throw new FastRuntimeException("当前报表无法重新计算!");
        }
    }
}
