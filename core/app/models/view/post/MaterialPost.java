package models.view.post;

import helper.DBUtils;
import models.User;
import models.material.Material;
import org.apache.commons.lang.StringUtils;
import play.libs.F;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: licco
 * Date: 2017/5/16
 * Time: 下午4:16
 */
public class MaterialPost extends Post<Material> {

    private static final long serialVersionUID = -7336544616312488827L;
    public Material.T type;
    public Long cooperId;
    public String number;

    @Override
    public F.T2<String, List<Object>> params() {
        List<Object> params = new ArrayList<>();
        StringBuilder sbd = new StringBuilder("SELECT distinct m FROM Material m "
                + "LEFT JOIN m.cooperItems ci  LEFT JOIN m.boms bs WHERE m.isDel=? ");
        params.add(false);
        if(type != null) {
            sbd.append(" AND m.type = ? ");
            params.add(type);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (m.code LIKE ? ");
            sbd.append(" OR m.name LIKE ? OR m.specification LIKE ? ");
            sbd.append(" OR m.texture LIKE ? OR m.technology LIKE ? OR m.version LIKE ? )");
            for(int i = 0; i < 6; i++) params.add(this.word());
        }
        if(cooperId != null) {
            sbd.append(" AND ci.cooperator.id = ? ");
            params.add(cooperId);
        }
        if(StringUtils.isNotBlank(this.number)) {
            sbd.append(" AND bs.number = ? ");
            params.add(this.number);
        }
        return new F.T2<>(sbd.toString(), params);
    }


    public F.T2<String, List<Object>> planParams() {
        List<Object> params = new ArrayList<>();
        StringBuilder sbd = new StringBuilder("SELECT m.id, m.code, mb.number, m.name, m.type, c.name as cooperName,");
        sbd.append(" m.projectName, SUM(IF(p.state='CONFIRM', u.planQty, 0)) AS confirmQty, ");
        sbd.append(" IFNULL((SELECT sum(mu.qty) FROM MaterialPlanUnit mu LEFT JOIN  MaterialPlan mp ON ");
        sbd.append(" mp.id = mu.materialPlan_id WHERE mu.material_id = m.id AND mp.state='DONE'),0) AS planQty,");
        sbd.append(" SUM(IF(p.state='PENDING',u.planQty, 0)) AS pendingQty, m.version ");
        sbd.append(" FROM Material m LEFT JOIN MaterialBom_Material b ON b.materials_id = m.id ");
        sbd.append(" LEFT JOIN MaterialBom mb ON mb.id = b.boms_id ");
        sbd.append(" LEFT JOIN CooperItem i ON i.material_id = m.id ");
        sbd.append(" LEFT JOIN Cooperator c ON c.id = i.cooperator_id ");
        sbd.append(" LEFT JOIN MaterialUnit u ON m.id = u.material_id ");
        sbd.append(" LEFT JOIN MaterialPurchase p ON p.id = u.materialPurchase_id ");
        sbd.append(" WHERE m.isDel= ? ");
        params.add(false);
        if(type != null) {
            sbd.append(" AND m.type = ? ");
            params.add(type);
        }
        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (m.code LIKE ? ");
            sbd.append(" OR m.name LIKE ? OR m.specification LIKE ? OR m.texture LIKE ? ");
            sbd.append(" OR m.technology LIKE ? OR m.version LIKE ? )");
            for(int i = 0; i < 6; i++) params.add(this.word());
        }
        if(cooperId != null) {
            sbd.append(" AND c.id = ? ");
            params.add(cooperId);
        }
        if(StringUtils.isNotBlank(this.number)) {
            sbd.append(" AND b.number = ? ");
            params.add(this.number);
        }
        sbd.append(" GROUP BY m.id HAVING confirmQty>0 || pendingQty>0 ");
        return new F.T2<>(sbd.toString(), params);
    }

    public F.T2<String, List<Object>> availableQtyParams() {
        List<Object> params = new ArrayList<>();
        StringBuilder sbd = new StringBuilder("SELECT m.id, m.code, mb.number, m.name, m.type, c.name as cooperName,");
        sbd.append(" m.projectName, SUM(IF(p.state='DONE', IF(u.receiptQty>0, u.receiptQty, u.qty), 0)) AS total, ");
        sbd.append(" SUM(IF(o.status ='Outbound', ou.outQty, 0)) AS outQty, m.version ");
        sbd.append(" FROM Material m LEFT JOIN MaterialBom_Material b ON b.materials_id = m.id ");
        sbd.append(" LEFT JOIN MaterialBom mb ON mb.id = b.boms_id ");
        sbd.append(" LEFT JOIN CooperItem i ON i.material_id = m.id ");
        sbd.append(" LEFT JOIN Cooperator c ON c.id = i.cooperator_id ");
        sbd.append(" LEFT JOIN MaterialPlanUnit u ON u.material_id = m.id ");
        sbd.append(" LEFT JOIN MaterialPlan p ON (u.materialPlan_id = p.id AND p.receipt='WAREHOUSE') ");
        sbd.append(" LEFT JOIN MaterialOutboundUnit ou ON ou.material_id = m.id ");
        sbd.append(" LEFT JOIN MaterialOutbound o ON o.id = ou.materialOutbound_id ");
        sbd.append(" WHERE m.isDel=?");
        params.add(false);
        if(type != null) {
            sbd.append(" AND m.type = ? ");
            params.add(type);
        }

        if(StringUtils.isNotBlank(this.search)) {
            sbd.append(" AND (m.code LIKE ?  OR m.name LIKE ? OR m.specification LIKE ? ");
            sbd.append(" OR m.texture LIKE ? OR m.technology LIKE ? OR m.version LIKE ? )");
            for(int i = 0; i < 6; i++) params.add(this.word());
        }
        if(cooperId != null) {
            sbd.append(" AND c.id = ? ");
            params.add(cooperId);
        }
        if(StringUtils.isNotBlank(this.number)) {
            sbd.append(" AND b.number = ? ");
            params.add(this.number);
        }
        sbd.append(" GROUP BY m.id ");
        sbd.append(" HAVING total-outQty>0 ");
        return new F.T2<>(sbd.toString(), params);
    }

    @Override
    public List<Material> query() {
        F.T2<String, List<Object>> params = params();
        this.count = Material.find(params._1, params._2.toArray()).fetch().size();
        String sql = params._1 + " ORDER BY m.id DESC";
        return Material.find(sql, params._2.toArray()).fetch(this.page, this.perSize);
    }

    public List<Material> planQuery() {
        F.T2<String, List<Object>> params = planParams();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, params._2.toArray());
        List<Material> materials = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            Material material = new Material();
            material.id = Long.parseLong(row.get("id").toString());
            material.code = row.get("code").toString();
            material.name = row.get("name").toString();
            material.number = row.get("number").toString();
            material.type = Material.T.valueOf(row.get("type").toString());
            material.cooperName = row.get("cooperName").toString();
            material.projectName = User.COR.valueOf(row.get("projectName").toString());
            material.qty = Integer.parseInt(row.get("confirmQty").toString())
                    - Integer.parseInt(row.get("planQty").toString());
            material.pendingQty = Integer.parseInt(row.get("pendingQty").toString());
            material.version = row.get("version").toString();
            materials.add(material);
        }
        return materials;
    }

    public List<Material> outBoundQuery() {
        F.T2<String, List<Object>> params = availableQtyParams();
        List<Map<String, Object>> rows = DBUtils.rows(params._1, params._2.toArray());
        List<Material> materials = new ArrayList<>();
        for(Map<String, Object> row : rows) {
            Material material = new Material();
            material.id = Long.parseLong(row.get("id").toString());
            material.code = row.get("code").toString();
            material.name = row.get("name").toString();
            material.number = row.get("number").toString();
            material.version = row.get("version").toString();
            material.type = Material.T.valueOf(row.get("type").toString());
            material.cooperName = row.get("cooperName").toString();
            material.projectName = User.COR.valueOf(row.get("projectName").toString());
            material.availableQty = Integer.parseInt(row.get("total").toString())
                    - Integer.parseInt(row.get("outQty").toString());
            materials.add(material);
        }
        return materials;
    }

    public Long getTotalCount() {
        return this.count;
    }

    @Override
    public Long count(F.T2<String, List<Object>> params) {
        return this.count;
    }

}
