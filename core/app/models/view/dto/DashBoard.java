package models.view.dto;

import org.joda.time.DateTime;
import query.vo.OrderrVO;

import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/23/13
 * Time: 9:50 AM
 */
public class DashBoard implements Serializable {

    public Map<String, OrderInfo> infos = new LinkedHashMap<>();
    public Map<String, OrderInfo> accsInfos = new HashMap<>();

    private String accKey(String date, long aid) {
        return String.format("%s_%s", date, aid);
    }

    public DashBoard pending(String key, OrderrVO vo) {
        basePending(key, null, infos);
        basePending(key, vo, accsInfos);
        return this;
    }

    public DashBoard payments(String key, OrderrVO vo) {
        basePayment(key, null, infos);
        basePayment(key, vo, accsInfos);
        return this;
    }

    public DashBoard shippeds(String key, OrderrVO vo) {
        baseShipped(key, null, infos);
        baseShipped(key, vo, accsInfos);
        return this;
    }

    public DashBoard refundeds(String key, OrderrVO vo) {
        baseRefunded(key, null, infos);
        baseRefunded(key, vo, accsInfos);
        return this;
    }

    public DashBoard returnNews(String key, OrderrVO vo) {
        baseReturnNew(key, null, infos);
        baseReturnNew(key, vo, accsInfos);
        return this;
    }

    public DashBoard cancels(String key, OrderrVO vo) {
        baseCancel(key, null, infos);
        baseCancel(key, vo, accsInfos);
        return this;
    }


    // So boring

    private void basePending(String key, OrderrVO vo, Map<String, OrderInfo> map) {
        String _key = (vo == null ? key : accKey(key, vo.account_id));
        if(map.containsKey(_key)) {
            map.get(_key).pendings += 1;
        } else {
            OrderInfo info = new OrderInfo();
            info.pendings = 1;
            map.put(_key, info);
        }
    }

    private void basePayment(String key, OrderrVO vo, Map<String, OrderInfo> map) {
        String _key = (vo == null ? key : accKey(key, vo.account_id));
        if(map.containsKey(_key)) {
            map.get(_key).payments += 1;
        } else {
            OrderInfo info = new OrderInfo();
            info.payments = 1;
            map.put(_key, info);
        }
    }

    private void baseCancel(String key, OrderrVO vo, Map<String, OrderInfo> map) {
        String _key = (vo == null ? key : accKey(key, vo.account_id));
        if(map.containsKey(_key)) {
            map.get(_key).cancels += 1;
        } else {
            OrderInfo info = new OrderInfo();
            info.cancels = 1;
            map.put(_key, info);
        }
    }

    private void baseRefunded(String key, OrderrVO vo, Map<String, OrderInfo> map) {
        String _key = (vo == null ? key : accKey(key, vo.account_id));
        if(map.containsKey(_key)) {
            map.get(_key).refundeds += 1;
        } else {
            OrderInfo info = new OrderInfo();
            info.refundeds = 1;
            map.put(_key, info);
        }
    }

    private void baseReturnNew(String key, OrderrVO vo, Map<String, OrderInfo> map) {
        String _key = (vo == null ? key : accKey(key, vo.account_id));
        if(map.containsKey(_key)) {
            map.get(_key).returnNews += 1;
        } else {
            OrderInfo info = new OrderInfo();
            info.returnNews = 1;
            map.put(_key, info);
        }
    }

    private void baseShipped(String key, OrderrVO vo, Map<String, OrderInfo> map) {
        String _key = (vo == null ? key : accKey(key, vo.account_id));
        if(map.containsKey(_key)) {
            map.get(_key).shippeds += 1;
        } else {
            OrderInfo info = new OrderInfo();
            info.shippeds = 1;
            map.put(_key, info);
        }
    }

    /**
     * 对 OrderInfos 排序
     *
     * @return
     */
    public synchronized DashBoard sort() {
        // Make a copy
        List<String> keys = new ArrayList<>(infos.keySet());
        Collections.sort(keys, new InfoDateKeySort());
        LinkedHashMap<String, OrderInfo> afterSorted = new LinkedHashMap<>();
        for(String key : keys) {
            afterSorted.put(key, infos.get(key));
        }
        this.infos.clear();
        this.infos = afterSorted;
        return this;
    }


    /**
     * 订单的状态数据
     */
    public static class OrderInfo implements Serializable {
        public Date date;
        public Integer pendings = 0;
        public Integer payments = 0;
        public Integer shippeds = 0;
        public Integer refundeds = 0;
        public Integer returnNews = 0;
        public Integer cancels = 0;

        public Integer total() {
            return this.pendings + this.payments + this.shippeds
                    + this.refundeds + this.returnNews + this.cancels;
        }
    }

    class InfoDateKeySort implements Comparator<String> {
        @Override
        public int compare(String key1, String key2) {
            long part1 = DateTime.parse(key1).getMillis();
            long part2 = DateTime.parse(key2).getMillis();
            return (int) (part1 - part2);
        }
    }
}
