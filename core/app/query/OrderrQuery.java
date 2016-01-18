package query;

import helper.DBUtils;
import helper.Dates;
import models.market.M;
import models.market.Orderr;
import org.apache.commons.lang.StringUtils;
import play.db.helper.SqlSelect;
import query.vo.OrderrVO;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 1/23/13
 * Time: 11:21 AM
 */
public class OrderrQuery {
    /**
     * 前端需要加载的订单数据
     *
     * @param from
     * @return
     */
    public List<OrderrVO> dashBoardOrders(Date from, Date to, M market, Connection conn) {
        SqlSelect sql = new SqlSelect()
                .select("o.createDate", "o.market", "o.orderId", "o.account_id", "o.state")
                .from("Orderr o")
                .where("o.market=?").param(market.name())
                .where("o.createDate>=?").param(from)
                .where("o.createDate<=?").param(to);
        List<Map<String, Object>> rows = DBUtils.rows(conn, sql.toString(), sql.getParams().toArray());
        List<OrderrVO> vos = new ArrayList<OrderrVO>();
        for(Map<String, Object> row : rows) {
            OrderrVO vo = new OrderrVO();
            vo.orderId = row.get("orderId").toString();
            vo.createDate = (Date) row.get("createDate");
            vo.market = M.val(row.get("market").toString());
            vo.account_id = ((Number) row.get("account_id")).longValue();
            vo.state = Orderr.S.valueOf(row.get("state").toString());
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 查询出符合条件的订单 ID
     *
     * @param from
     * @param to
     * @param market
     * @param state
     * @param esStyle 是否需要将 - 替换成 _
     * @return
     */
    public static List<String> orderIds(Date from, Date to, M market, Orderr.S state, boolean esStyle) {
        SqlSelect sql = new SqlSelect();
        if(esStyle) {
            sql.select("DISTINCT REPLACE(o.orderId, '-', '_') AS orderId");
        } else {
            sql.select("DISTINCT o.orderId AS orderId");
        }
        sql.from("Orderr o")
                .where("o.market=?").param(market.name())
                .where("o.createDate>=?").param(market.withTimeZone(Dates.morning(from)).toDate())
                .where("o.createDate<=?").param(market.withTimeZone(Dates.morning(to)).toDate());
        if(state != null) sql.where("o.state=?").param(state.name());

        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), sql.getParams().toArray());

        List<String> orderIds = new ArrayList<String>();
        for(Map<String, Object> row : rows) {
            if(row != null) {
                String orderId = row.get("orderId").toString();
                if(StringUtils.isNotBlank(orderId)) {
                    orderIds.add(orderId);
                }
            }
        }
        return orderIds;
    }
}
