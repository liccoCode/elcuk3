package models.market;

import helper.AWS;
import org.apache.commons.lang.StringUtils;
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
        ALL_FBA_ORDER_SHIPPED;

        public String toString() {
            switch(this) {
                case ALL_FBA_ORDER_SHIPPED:
                    return "_GET_AMAZON_FULFILLED_SHIPMENTS_DATA_";
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
        END
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

    public String path;

    /*这份 Report 请求的数据的时间段*/
    public Date startDate;
    public Date endDate;


    /**
     * 根据 Account 检查是否需要有不同类型的 Job 创建
     *
     * @param acc
     * @param type
     * @return
     */
    public static JobRequest checkJob(Account acc, T type) {
        switch(type) {
            case ALL_FBA_ORDER_FETCH:
                return newJob(3, T.ALL_FBA_ORDER_FETCH, acc);
            case ALL_FBA_ORDER_SHIPPED:
                return newJob(8, T.ALL_FBA_ORDER_SHIPPED, acc);
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
    private static JobRequest newJob(int interval, T type, Account acc) {
        if(!acc.type.name().startsWith("AMAZON"))
            throw new FastRuntimeException("Only Amazon Account can have ALL_FBA_ORDER_SHIPPED JOB!");
        JobRequest job = JobRequest.find("account=? AND type=? ORDER BY requestDate DESC", acc, type).first();
        // 每 8 消失抓取一次发货
        if(job == null || (System.currentTimeMillis() - job.requestDate.getTime()) > TimeUnit.HOURS.toMillis(interval)) {
            JobRequest njob = new JobRequest();
            njob.account = acc;
            njob.requestDate = njob.lastUpdateDate = new Date();
            njob.state = S.NEW;
            njob.type = type;
            return njob;
        }
        return null;
    }

    /**
     * 发出请求
     */
    public void request() {
        if(this.type == T.ALL_FBA_ORDER_FETCH ||
                this.type == T.ALL_FBA_ORDER_SHIPPED) {
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
        if(this.type == T.ALL_FBA_ORDER_FETCH ||
                this.type == T.ALL_FBA_ORDER_SHIPPED) {
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
        if(this.type == T.ALL_FBA_ORDER_FETCH ||
                this.type == T.ALL_FBA_ORDER_SHIPPED) {
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
        if(this.type == T.ALL_FBA_ORDER_FETCH ||
                this.type == T.ALL_FBA_ORDER_SHIPPED) {
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
                 * 2. 手动从数据库中加载出需要更新的 Order (managed),  然后再将这些处于被管理状态的 Order 进行更新;
                 * 3. 将数据库中没有加载到的 Order 给新保存
                 */
                orders = Orderr.parseAllOrderXML(new File(this.path), this.account.type); // 1. 解析出订单
                orderIds.clear();
                orderrMap.clear();
                oldOrderrMap.clear();
                for(Orderr or : orders) {
                    orderrMap.put(or.orderId, or);
                    orderIds.add(or.orderId);
                }
                List<Orderr> managedOrderrs = Orderr.find("orderId IN ('" + StringUtils.join(orderIds, "','") + "')").fetch();
                for(Orderr or : managedOrderrs) { // 2. 手动从数据库中加载出需要更新的 Order (managed),  然后再将这些处于被管理状态的 Order 进行更新;
                    Orderr newOrder = orderrMap.get(or.orderId);
                    or.updateOrderInfo(newOrder);
                    oldOrderrMap.put(or.orderId, or);
                }
                for(Orderr newOrd : orders) { // 3. 将数据库中没有加载到的 Order 给新保存
                    if(oldOrderrMap.containsKey(newOrd.orderId)) continue;
                    // 由于 Account 在 XML 文件中解析不出来, 所以在创建的时候需要讲讲这个 Order 的 Account 与对应申请 JobRequest 的关联上
                    newOrd.account = this.account;
                    newOrd.save();
                    Logger.info("Save Order: " + newOrd.orderId);
                }
                break;
            case ALL_FBA_ORDER_SHIPPED:
                /**
                 * 1. 将需要更新的数据从 csv 文件中提取出来
                 * 2. 加载出需要进行更新的 Order(managed), 然后将这些处于被管理的 Order 对象进行更新;
                 * 3. 如果在更新过程中出现系统中没有出现的订单, 那么则输出日志
                 */
                orders = Orderr.parseUpdateOrderXML(new File(this.path), this.account.type);
                orderIds.clear();
                orderrMap.clear();
                oldOrderrMap.clear();
                for(Orderr or : orders) {
                    orderrMap.put(or.orderId, or);
                    orderIds.add(or.orderId);
                }

                List<Orderr> managedOrderrs_2 = Orderr.find("orderId IN ('" + StringUtils.join(orderIds, "','") + "')").fetch();
                for(Orderr or : managedOrderrs_2) {
                    Orderr newOrder = orderrMap.get(or.orderId);
                    or.updateOrderInfo(newOrder);
                    oldOrderrMap.put(or.orderId, or);
                }
                for(Orderr newOrd : orders) {
                    if(oldOrderrMap.containsKey(newOrd.orderId)) continue;
                    Logger.warn("Update Order [" + newOrd.orderId + "] is not exist.");
                }
                break;
        }
    }
}
