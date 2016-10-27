package models.view.post;

import helper.DBUtils;
import helper.Dates;
import models.finance.Payment;
import models.procure.ProcureUnit;
import models.view.dto.PurchasePaymentDTO;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by licco on 16/3/7.
 */
public class PurchaseOrderPost extends Post<ProcureUnit> {

    public Long cooperatorId;

    public ProcureUnit.STAGE stage;

    @Override
    public F.T2<String, List<Object>> params() {
        StringBuilder sql = new StringBuilder(" 1 = 1");
        List<Object> params = new ArrayList<>();
        if(from != null) {
            sql.append(" AND createDate>=? ");
            params.add(Dates.morning(this.from));
        }
        if(to != null) {
            sql.append(" AND createDate<=? ");
            params.add(Dates.night(this.to));
        }

        if(stage != null) {
            sql.append(" AND stage = ? ");
            params.add(stage);
        }
        if(cooperatorId != null) {
            sql.append(" AND cooperator.id = ? ");
            params.add(cooperatorId);
        }
        if(StringUtils.isNotEmpty(this.search)) {
            sql.append(" AND (sku = ? or deliveryment.id = ? )");
            params.add(this.search);
            params.add(this.search);
        }
        return new F.T2<>(sql.toString(), params);
    }

    public List<ProcureUnit> query() {
        F.T2<String, List<Object>> params = params();
        return ProcureUnit.find(params._1, params._2.toArray()).fetch();
    }

    public List<PurchasePaymentDTO> downloadReport() {
        List<PurchasePaymentDTO> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT c.name, u.deliveryment_id, u.id, u.sku, u.stage, u.price, u.currency, ");
        sql.append(" IFNULL(u.qty, u.planQty)  AS qty, ");
        sql.append(" round(sum(IFNULL(u.qty,  u.planQty) * u.price), 2) AS 'totalPurchases',");
        sql.append(" (SELECT IFNULL(round(sum(p.amount + p.fixValue),2), 0) FROM PaymentUnit p ");
        sql.append(" p .procureUnit_id = u.id AND p.state IN  ('APPROVAL', 'APPLY')) AS 'notPayAmount',");
        sql.append(" (SELECT IFNULL(round(sum(pu.amount + pu.fixValue),2), 0) FROM PaymentUnit pu ");
        sql.append(" WHERE pu.procureUnit_id = u.id AND pu.state = 'PAID' AND pu.remove=0) AS 'paidAmount' ");
        sql.append(" FROM  ProcureUnit u ");
        sql.append(" LEFT JOIN Deliveryment d ON d.id = u.deliveryment_id ");
        sql.append(" LEFT JOIN Cooperator c ON c.id = u.cooperator_id ");
        sql.append(" WHERE u.stage <> 'APPROVE'");
        sql.append(" AND d.createDate >= ? ");
        sql.append(" AND d.createDate <= ? ");
        params.add(from);
        params.add(to);
        if(stage != null) {
            sql.append(" AND u.stage = ? ");
            params.add(stage);
        }
        if(cooperatorId != null) {
            sql.append(" AND u.cooperator_id = ? ");
            params.add(cooperatorId);
        }
        sql.append(" GROUP BY u.id, u.deliveryment_id ");
        sql.append(" ORDER BY u.createDate DESC ");
        List<Map<String, Object>> rows = DBUtils.rows(sql.toString(), params.toArray());
        for(Map<String, Object> row : rows) {
            PurchasePaymentDTO dto = new PurchasePaymentDTO();
            dto.cooperator = row.get("name").toString();
            dto.deliverymentId = row.get("deliveryment_id") == null ? "" : row.get("deliveryment_id").toString();
            dto.unitId = row.get("id").toString();
            dto.sku = row.get("sku").toString();
            dto.stage = ProcureUnit.STAGE.valueOf(row.get("stage").toString());
            dto.price = Float.valueOf(row.get("price").toString());
            dto.currency = row.get("currency").toString();
            dto.qty = Integer.valueOf(row.get("qty").toString());
            dto.totalPurchases = Float.valueOf(row.get("totalPurchases").toString());
            dto.paidAmount = Float.valueOf(row.get("paidAmount").toString());
            dto.notPayAmount = Float.valueOf(row.get("notPayAmount").toString());
            dto.leftAmount = dto.totalPurchases - dto.paidAmount;

            dto.payment = returnPayment(dto.deliverymentId);
            list.add(dto);
        }
        return list;
    }

    public String returnPayment(String id) {

        StringBuilder sql = new StringBuilder("SELECT p FROM Payment p ");
        sql.append(" LEFT JOIN p.pApply a ");
        sql.append(" LEFT JOIN a.deliveryments s ");
        sql.append(" WHERE s.id = ? ");
        List<Payment> list = Payment.find(sql.toString(), id).fetch();
        if(list != null && list.size() > 0) {
            String temp = "";
            for(Payment p : list) {
                temp += p.paymentNumber + " " + p.currency.symbol() + " " + p.actualPaid + ";  ";
            }
            return temp;
        }
        return "";
    }
}
