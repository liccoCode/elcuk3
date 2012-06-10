package models.market;

import helper.AWS;
import helper.Webs;
import models.product.Whouse;
import play.Logger;
import play.db.jpa.Model;
import play.utils.FastRuntimeException;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToOne;
import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 任何非同步的请求, 比如向 Amazon 的定时请求订单
 * User: wyattpan
 * Date: 1/23/12
 * Time: 5:29 PM
 */
@Entity
public class JobRequest extends Model {


    public enum T {
        /**
         * 用来发现 Amazon 上通过 FBA 新发现的订单
         */
        ALL_FBA_ORDER_FETCH,
        /**
         * 从 Amazon 上抓取会已经发送出去的订单
         */
        ALL_FBA_ORDER_SHIPPED,
        /**
         * 用来同步系统中库存与 FBA 库存的量
         */
        MANAGE_FBA_INVENTORY_ARCHIVED,

        /**
         * 在 Amazon 上活动的 Listing, 这份日志用于同步 SKU 与 Amazon 上的 Listing
         */
        ACTIVE_LISTINGS;

        public String toString() {
            switch(this) {
                case ACTIVE_LISTINGS:
                    return "_GET_MERCHANT_LISTINGS_DATA_";
                case ALL_FBA_ORDER_SHIPPED:
                    return "_GET_AMAZON_FULFILLED_SHIPMENTS_DATA_";
                case MANAGE_FBA_INVENTORY_ARCHIVED:
                    return "_GET_FBA_MYI_ALL_INVENTORY_DATA_";
                case ALL_FBA_ORDER_FETCH:
                default:
                    return "_GET_XML_ALL_ORDERS_DATA_BY_ORDER_DATE_";
            }
        }

    }

    /**
     * 当前这一项任务的处理状态
     */
    public enum S {
        /**
         * CheckJob 创建一个新的
         */
        NEW,
        /**
         * 向网站请求了 Report
         */
        REQUEST,
        /**
         * 网站在处理中, 还需要继续请求
         */
        PROCRESS,
        /**
         * Amazon 处理完成了, 可以进入请求 ReportId 了
         */
        DONE,
        /**
         * 拥有 ReportId 了, 可以进行下载了
         */
        DOWN,
        /**
         * 下载完成了, 结束;
         */
        END,
        /**
         * 这个任务接受了进行关闭
         */
        CLOSE
    }

    @OneToOne
    public Account account;

    public Date requestDate;

    public Date lastUpdateDate;


    @Enumerated(EnumType.STRING)
    public T type;

    @Enumerated(EnumType.STRING)
    public S state = S.NEW;

    // ----- logic relate ----
    public String requestId;

    public String reportId;

    public String procressState;

    @Enumerated(EnumType.STRING)
    public AWS.MID marketplaceId;

    public String path;

    /*这份 Report 请求的数据的时间段*/
    public Date startDate;
    public Date endDate;


    /**
     * 根据 Account 检查是否需要有不同类型的 Job 创建;
     *
     * @param acc
     * @param type
     * @param mid  需要使用哪一个市场的数据;
     * @return
     */
    public static JobRequest checkJob(Account acc, T type, AWS.MID mid) {
        switch(type) {
            // 每一个小时抓取一次新订单
            case ALL_FBA_ORDER_FETCH:
                return newJob(1, T.ALL_FBA_ORDER_FETCH, acc, mid);
            // 每 8 小时更新一次发货的订单
            case ALL_FBA_ORDER_SHIPPED:
                return newJob(8, T.ALL_FBA_ORDER_SHIPPED, acc, mid);
            // 每 8 小时进行一次 FBA 仓库同步
            case MANAGE_FBA_INVENTORY_ARCHIVED:
                return newJob(8, T.MANAGE_FBA_INVENTORY_ARCHIVED, acc, mid);
            case ACTIVE_LISTINGS:
                //一般情况下, 这个 Job 应该是关闭状态, 手动运行. 数据库中记录的这些数据也可以删除, 因为其需要最新的数据.
                return newJob(1, T.ACTIVE_LISTINGS, acc, mid);
        }
        return null;
    }

    /**
     * 具体的检查某个类型的 JobRequest 是否需要进行创建.
     *
     * @param interval 创建下一个 JobRequest 的时间间隔.
     * @param type     这个 JobRequest 的类型
     * @param acc      哪一个账户
     * @return
     */
    private static JobRequest newJob(int interval, T type, Account acc, AWS.MID mid) {
        if(!acc.type.name().startsWith("AMAZON"))
            throw new FastRuntimeException("Only Amazon Account can have ALL_FBA_ORDER_SHIPPED JOB!");
        JobRequest job = JobRequest.find("account=? AND type=? AND marketplaceId=? ORDER BY requestDate DESC", acc, type, mid).first();

        //先判断 Job 不为空的情况
        if(job == null || (System.currentTimeMillis() - job.requestDate.getTime()) > TimeUnit.HOURS.toMillis(interval)) {
            JobRequest njob = new JobRequest();
            njob.account = acc;
            njob.requestDate = njob.lastUpdateDate = new Date();
            njob.state = S.NEW;
            njob.type = type;
            njob.marketplaceId = mid;
            return njob;
        }
        return null;
    }

    /**
     * 检查这些状态是否是位可以提交的任务
     *
     * @return
     */
    private boolean checkAvailableType() {
        switch(this.type) {
            case ALL_FBA_ORDER_FETCH:
            case ALL_FBA_ORDER_SHIPPED:
            case MANAGE_FBA_INVENTORY_ARCHIVED:
            case ACTIVE_LISTINGS:
                return true;
            default:
                return false;
        }
    }

    /**
     * 发出请求
     */
    public void request() {
        if(checkAvailableType()) {
            Logger.debug("(step1)JobRequest request " + this.type + " REQUEST Job.");
            try {
                AWS.requestReport_step1(this);
            } catch(Exception e) {
                Logger.warn("JobRequest Request Report Error. " + e.getMessage());
            }
        }
    }

    /**
     * 更新 Job 状态
     */
    public void updateState() {
        if(checkAvailableType()) {
            Logger.debug("(step2)JobRequest request " + this.type + " UPDATE_STATE Job.");
            try {
                AWS.requestState_step2(this);
            } catch(Exception e) {
                Logger.warn("JobRequest Update State Error. " + e.getMessage());
            }
        }
    }

    /**
     * 获取 ReportId
     */
    public void updateReportId() {
        if(checkAvailableType()) {
            Logger.debug("JobRequest request " + this.type + " UPDATE_REPORTID Job.");
            try {
                AWS.requestReportId_step3(this);
            } catch(Exception e) {
                Logger.warn("JobRequest Update Report Error. " + e.getMessage());
            }
        }
    }

    /**
     * 修在文件
     */
    public void downLoad() {
        if(this.state != S.DOWN) return;
        if(checkAvailableType()) {
            Logger.debug("JobRequest request " + this.type + " DOWNLOAD Job.");
            try {
                AWS.requestReportDown_step4(this);
            } catch(Exception e) {
                Logger.warn("JobRequest DownLoad Error. " + e.getMessage());
            }
        }
    }

    /**
     * 处理下载好的文件
     */
    public void dealWith() {
        Collection<Orderr> orders = null;
        List<String> orderIds = new ArrayList<String>();
        Map<String, Orderr> orderrMap = new HashMap<String, Orderr>();
        Map<String, Orderr> oldOrderrMap = new HashMap<String, Orderr>();
        switch(this.type) {
            case ALL_FBA_ORDER_FETCH:
                /**
                 * 1. 解析出文件中的所有 Orders.
                 * 2. 遍历所有的订单, 利用 hibernate 的二级缓存, 加载 Orderr 进行保存或者更新
                 */
                orders = Orderr.parseAllOrderXML(new File(this.path), this.account); // 1. 解析出订单

                for(Orderr order : orders) {
                    Orderr managed = Orderr.findById(order.orderId);
                    if(managed == null) { //保存
                        order.save();
                        Logger.info("Save Order: " + order.orderId);
                    } else { //更新
                        if(managed.state == Orderr.S.CANCEL) continue;
                        managed.updateAttrs(order);
                    }
                }
                break;
            case ALL_FBA_ORDER_SHIPPED:
                /**
                 * 1. 将需要更新的数据从 csv 文件中提取出来
                 * 2. 遍历所有的订单, 利用 hibernate 的二级缓存, 加载 Orderr 进行保存或者更新
                 */
                orders = Orderr.parseUpdateOrderXML(new File(this.path), this.account.type);
                for(Orderr order : orders) {
                    Orderr managed = Orderr.findById(order.orderId);
                    if(managed == null) {
                        Logger.error("Update Order [%s] is not exist.", order.orderId);
                    } else {
                        managed.updateAttrs(order);
                    }
                }
                break;
            case MANAGE_FBA_INVENTORY_ARCHIVED:
                Whouse wh = Whouse.find("account=?", this.account).first();
                List<SellingQTY> sqtys = wh.fbaCSVParseSQTY(new File(this.path));
                for(SellingQTY sqty : sqtys) {
                    // 解析出来的 SellingQTY, 如果系统中拥有则进行更新, 否则绑定到 Selling 身上
                    if(!sqty.isPersistent()) {
                        String sid = Selling.sid(sqty.msku(), this.account.type, this.account);
                        try {
                            sqty.attach2Selling(sqty.msku(), wh);
                        } catch(Exception e) {
                            String warmsg = "FBA CSV Report hava Selling[" + sid + "] that system can not be found!";
                            Logger.warn(warmsg);
                            Webs.systemMail(warmsg, warmsg + "<br/>\r\n" + Webs.E(e) +
                                    ";<br/>\r\n需要通过 Amazon 与系统内的 Selling 进行同步, 处理掉丢失的 Product 与 Selling, 然后再重新进行 FBA 库存的解析.");
                        }
                    } else {
                        sqty.save();
                    }
                }


                break;
            case ACTIVE_LISTINGS:
                Selling.dealSellingFromActiveListingsReport(new File(this.path), this.account, this.marketplaceId.market());
                break;
        }
        this.state = S.CLOSE;
        this.save();
    }
}
